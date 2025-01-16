package com.pfa.notificationservice.services.implementations;

import com.pfa.notificationservice.services.interfaces.UserServiceClient;
import com.pfa.notificationservice.dtos.DataUserResponse;
import com.pfa.notificationservice.dtos.Event;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service @RequiredArgsConstructor @Slf4j
public class NotificationService {
    private final UserServiceClient userServiceClient;
    private final EmailService emailService;

    @KafkaListener(topics = "${spring.kafka.topic.name}", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(@Payload Event event) {
        try {
            log.info("Received event with id: {}", event.id());
            DataUserResponse creator = userServiceClient.getUserById(event.creator());
            sendNotification(event, creator);
            log.info("Successfully processed event: {}", event.id());
        } catch (Exception e) {
            log.error("Error processing event: {}", e.getMessage(), e);
        }
    }

    private void sendNotification(Event event, DataUserResponse creator) {
        Set<String> followerEmails = userServiceClient.getFollowers(creator.id());

        String subject = String.format("New Event: %s", event.name());
        String body = String.format("""
            Hello!
            
            A new event '%s' has been created by %s.
            
            Location: %s
            Date: %s
            
            Best regards,
            Your Application Team
            """,
                event.name(),
                creator.fullName(),
                event.location(),
                event.date()
        );

        emailService.sendBulkEmails(followerEmails, subject, body);

        log.info("Notifications sent to {} followers", followerEmails.size());
    }
}
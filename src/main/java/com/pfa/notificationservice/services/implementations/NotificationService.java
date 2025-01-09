package com.pfa.notificationservice.services.implementations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pfa.notificationservice.dtos.TicketEvent;
import com.pfa.notificationservice.services.interfaces.UserServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {
    private final UserServiceClient userServiceClient;
    private final EmailService emailService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "ticket-events", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(String message) {
        try {
            // Parse the string message
            if (message.startsWith("Ticket Created:")) {
                // Extract values using string manipulation
                String[] parts = message.substring("Ticket Created:".length()).split(",");
                Long id = null;
                String eventId = null;
                String userId = null;

                for (String part : parts) {
                    part = part.trim();
                    if (part.startsWith("ID=")) {
                        id = Long.parseLong(part.substring(3));
                    } else if (part.startsWith("EventID=")) {
                        eventId = part.substring(8);
                    } else if (part.startsWith("UserID=")) {
                        userId = part.substring(7);
                    }
                }

                TicketEvent ticket = new TicketEvent(
                        id,
                        eventId,
                        userId,
                        LocalDateTime.now(),
                        false,
                        "QR-" + id
                );

                log.info("Processed ticket: {}", ticket);

                // Get followers from UserServiceClient
                // Extract numeric part from userId (removing "USR-" prefix)
                String userIdNumber = userId.replace("USR-", "");
                Set<String> followers = userServiceClient.getFollowers(UUID.fromString(userIdNumber));

                if (followers != null && !followers.isEmpty()) {
                    // Prepare email content
                    String subject = "New Ticket Purchase Notification";
                    String emailBody = buildEmailBody(ticket);

                    // Send emails to all followers
                    emailService.sendBulkEmails(followers, subject, emailBody);

                    log.info("Notification emails sent to {} followers for user: {}",
                            followers.size(), userId);
                } else {
                    log.info("No followers found for user: {}", userId);
                }

            } else {
                log.warn("Unexpected message format: {}", message);
            }
        } catch (Exception e) {
            log.error("Error processing ticket event: " + message, e);
        }
    }

    private String buildEmailBody(TicketEvent ticketEvent) {
        return String.format("""
            Hello!
            
            We wanted to let you know that %s has just purchased a ticket!
            
            Ticket Details:
            - Event ID: %s
            - Purchase Time: %s
            - Ticket ID: %s
            
            Stay connected to see more updates!
            
            Best regards,
            Your Event Team
            """,
                ticketEvent.userId(),
                ticketEvent.eventId(),
                ticketEvent.bookingTime().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")),
                ticketEvent.id()
        );
    }
}
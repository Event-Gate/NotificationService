package com.pfa.notificationservice.dtos;

import java.time.LocalDateTime;

public record TicketEvent(
    Long id,
    String eventId,
    String userId,
    LocalDateTime bookingTime,
    boolean validated,
    String qrCode
) {}

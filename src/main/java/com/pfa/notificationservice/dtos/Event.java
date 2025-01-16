package com.pfa.notificationservice.dtos;

import com.pfa.notificationservice.enums.Status;

import java.time.LocalDateTime;

public record Event(
    String id,
    String name,
    String location,
    LocalDateTime date,
    Status status,
    LocalDateTime createdAt,
    int capacity,
    String creator
) {}

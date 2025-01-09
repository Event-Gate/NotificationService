package com.pfa.notificationservice.dtos;

import java.util.UUID;

public record DataUserResponse(
        UUID id,
        String fullName,
        String email
) {}

package com.defecttracker.dto.response;

import java.time.LocalDateTime;

public record ClientSummary(
    Long id,
    String clientName,
    String phoneNumber,
    String email,
    String country,
    String state,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}

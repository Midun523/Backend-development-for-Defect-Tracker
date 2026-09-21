package com.defecttracker.dto.response;

import java.time.LocalDateTime;

public record EmailLogResponse(
    Long id,
    String recipientEmail,
    String subject,
    String status,
    String errorMessage,
    LocalDateTime sentAt
) {}

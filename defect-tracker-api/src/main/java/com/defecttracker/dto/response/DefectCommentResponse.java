package com.defecttracker.dto.response;

import java.time.LocalDateTime;

public record DefectCommentResponse(
    Long id,
    DefectResponse defect,
    UserSummary user,
    String comment,
    LocalDateTime createdAt
) {}

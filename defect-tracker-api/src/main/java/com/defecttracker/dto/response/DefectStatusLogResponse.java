package com.defecttracker.dto.response;

import java.time.LocalDateTime;

public record DefectStatusLogResponse(
    Long id,
    ProjectResponse project,
    ReleaseResponse release,
    DefectResponse defect,
    String status,
    LocalDateTime loggedAt
) {}

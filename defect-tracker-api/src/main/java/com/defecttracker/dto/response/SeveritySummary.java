package com.defecttracker.dto.response;

public record SeveritySummary(
    Long id,
    String name,
    String color,
    String description
) {}

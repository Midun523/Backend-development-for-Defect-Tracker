package com.defecttracker.dto.response;

public record PrioritySummary(
    Long id,
    String name,
    String color,
    String description
) {}

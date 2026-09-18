package com.defecttracker.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryResponse {
    private long totalDefects;
    private long openDefects;
    private long fixedDefects;
    private long closedDefects;
    private long reopenedDefects;
    private double defectDensity;
    private Map<String, Long> severityBreakdown;
    private Map<String, Long> priorityBreakdown;
    private Map<String, Long> typeBreakdown;
    private Map<String, Long> moduleBreakdown;
}

package com.defecttracker.service;

import com.defecttracker.dto.response.DashboardSummaryResponse;

import java.util.Map;

public interface DashboardService {
    DashboardSummaryResponse getDashboardSummary(Long projectId, Long releaseId);
    Map<String, Long> getSeverityBreakdown(Long projectId);
    Map<String, Long> getTypeBreakdown(Long projectId);
    Map<String, Long> getModuleBreakdown(Long projectId);
    Map<String, Object> getDefectDensity(Long projectId);
    Map<String, Object> getReopenedDefects(Long projectId);
    Map<String, Object> getTimeToFind(Long projectId, Long releaseId);
    Map<String, Object> getTimeToFix(Long projectId, Long releaseId);
}

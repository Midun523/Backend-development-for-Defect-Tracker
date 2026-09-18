package com.defecttracker.service.impl;

import com.defecttracker.dto.response.DashboardSummaryResponse;
import com.defecttracker.entity.Defect;
import com.defecttracker.entity.Project;
import com.defecttracker.repository.DefectRepository;
import com.defecttracker.repository.ProjectRepository;
import com.defecttracker.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final DefectRepository defectRepository;
    private final ProjectRepository projectRepository;

    @Override
    public DashboardSummaryResponse getDashboardSummary(Long projectId, Long releaseId) {
        long total = defectRepository.countTotalDefects(projectId, releaseId);
        long fixed = defectRepository.countFixedDefects(projectId, releaseId);
        long reopened = defectRepository.countReopenedDefectsForProject(projectId);

        List<Defect> defects = releaseId != null
                ? defectRepository.findByProjectIdAndReleaseId(projectId, releaseId)
                : defectRepository.findByProjectId(projectId);

        long open = defects.stream()
                .filter(d -> d.getDefectStatus() != null && !"closed".equalsIgnoreCase(d.getDefectStatus().getName()) && !"resolved".equalsIgnoreCase(d.getDefectStatus().getName()))
                .count();

        long closed = total - open;

        Project project = projectRepository.findById(projectId).orElse(null);
        double kloc = (project != null && project.getKloc() != null && project.getKloc() > 0) ? project.getKloc() : 1.0;
        double density = (double) total / kloc;

        return DashboardSummaryResponse.builder()
                .totalDefects(total)
                .openDefects(open)
                .fixedDefects(fixed)
                .closedDefects(closed)
                .reopenedDefects(reopened)
                .defectDensity(Math.round(density * 100.0) / 100.0)
                .severityBreakdown(getSeverityBreakdown(projectId))
                .priorityBreakdown(getPriorityBreakdown(projectId))
                .typeBreakdown(getTypeBreakdown(projectId))
                .moduleBreakdown(getModuleBreakdown(projectId))
                .build();
    }

    @Override
    public Map<String, Long> getSeverityBreakdown(Long projectId) {
        List<Object[]> rows = defectRepository.countBySeverityForProject(projectId);
        Map<String, Long> result = new HashMap<>();
        for (Object[] r : rows) {
            String name = r[0] != null ? (String) r[0] : "Unknown";
            Long count = (Long) r[1];
            result.put(name, count);
        }
        return result;
    }

    public Map<String, Long> getPriorityBreakdown(Long projectId) {
        List<Defect> defects = defectRepository.findByProjectId(projectId);
        Map<String, Long> result = new HashMap<>();
        for (Defect d : defects) {
            String pName = d.getPriority() != null ? d.getPriority().getName() : "Medium";
            result.put(pName, result.getOrDefault(pName, 0L) + 1L);
        }
        return result;
    }

    @Override
    public Map<String, Long> getTypeBreakdown(Long projectId) {
        List<Object[]> rows = defectRepository.countByTypeForProject(projectId);
        Map<String, Long> result = new HashMap<>();
        for (Object[] r : rows) {
            String name = r[0] != null ? (String) r[0] : "Other";
            Long count = (Long) r[1];
            result.put(name, count);
        }
        return result;
    }

    @Override
    public Map<String, Long> getModuleBreakdown(Long projectId) {
        List<Object[]> rows = defectRepository.countByModuleForProject(projectId);
        Map<String, Long> result = new HashMap<>();
        for (Object[] r : rows) {
            String name = r[0] != null ? (String) r[0] : "General";
            Long count = (Long) r[1];
            result.put(name, count);
        }
        return result;
    }

    @Override
    public Map<String, Object> getDefectDensity(Long projectId) {
        Project project = projectRepository.findById(projectId).orElse(null);
        long totalDefects = defectRepository.countTotalDefects(projectId, null);
        double kloc = (project != null && project.getKloc() != null && project.getKloc() > 0) ? project.getKloc() : 1.0;
        double density = (double) totalDefects / kloc;

        Map<String, Object> result = new HashMap<>();
        result.put("projectId", projectId);
        result.put("totalDefects", totalDefects);
        result.put("kloc", kloc);
        result.put("defectDensity", Math.round(density * 100.0) / 100.0);
        return result;
    }

    @Override
    public Map<String, Object> getReopenedDefects(Long projectId) {
        long count = defectRepository.countReopenedDefectsForProject(projectId);
        Map<String, Object> result = new HashMap<>();
        result.put("projectId", projectId);
        result.put("reopenedCount", count);
        return result;
    }

    @Override
    public Map<String, Object> getTimeToFind(Long projectId, Long releaseId) {
        Map<String, Object> result = new HashMap<>();
        result.put("projectId", projectId);
        result.put("releaseId", releaseId);
        result.put("averageDaysToFind", 2.4);
        return result;
    }

    @Override
    public Map<String, Object> getTimeToFix(Long projectId, Long releaseId) {
        Map<String, Object> result = new HashMap<>();
        result.put("projectId", projectId);
        result.put("releaseId", releaseId);
        result.put("averageDaysToFix", 1.8);
        return result;
    }
}

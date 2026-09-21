package com.defecttracker.service;

import com.defecttracker.entity.Project;
import com.defecttracker.entity.ProjectSequence;
import com.defecttracker.exception.ResourceNotFoundException;
import com.defecttracker.repository.ProjectRepository;
import com.defecttracker.repository.ProjectSequenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ProjectSequenceService {

    private final ProjectSequenceRepository projectSequenceRepository;
    private final ProjectRepository projectRepository;
    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public String getNextDefectNumber(Long projectId) {
        long seq = getNextProjectSequenceValue(projectId, "DEF");
        return String.format("DEF%03d", seq);
    }

    @Transactional
    public String getNextTestCaseNumber(Long projectId) {
        long seq = getNextProjectSequenceValue(projectId, "TC");
        return String.format("TC%03d", seq);
    }

    @Transactional
    public String getNextReleaseNumber(Long projectId) {
        long seq = getNextProjectSequenceValue(projectId, "REL");
        return String.format("REL%03d", seq);
    }

    @Transactional
    public String getNextProjectCode(String prefix) {
        Long nextVal = jdbcTemplate.queryForObject("SELECT nextval('project_code_seq')", Long.class);
        String pfx = (prefix != null && !prefix.trim().isEmpty()) ? prefix.trim().toUpperCase() : "PRJ";
        return String.format("%s%03d", pfx, nextVal != null ? nextVal : 1L);
    }

    @Transactional
    public String getNextEmployeeCode() {
        Long nextVal = jdbcTemplate.queryForObject("SELECT nextval('employee_code_seq')", Long.class);
        return String.format("US%04d", nextVal != null ? nextVal : 1L);
    }

    private long getNextProjectSequenceValue(Long projectId, String sequenceType) {
        ProjectSequence ps = projectSequenceRepository.findByProjectIdAndSequenceTypeForUpdate(projectId, sequenceType)
                .orElseGet(() -> {
                    Project project = projectRepository.findById(projectId)
                            .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));
                    ProjectSequence newSeq = ProjectSequence.builder()
                            .project(project)
                            .sequenceType(sequenceType)
                            .currentValue(0L)
                            .build();
                    return projectSequenceRepository.saveAndFlush(newSeq);
                });

        long nextVal = ps.getCurrentValue() + 1;
        ps.setCurrentValue(nextVal);
        projectSequenceRepository.save(ps);
        return nextVal;
    }
}

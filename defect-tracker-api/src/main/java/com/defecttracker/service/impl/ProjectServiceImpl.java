package com.defecttracker.service.impl;

import com.defecttracker.dto.request.ProjectCreateRequest;
import com.defecttracker.dto.response.PaginatedResponse;
import com.defecttracker.entity.Client;
import com.defecttracker.entity.Employee;
import com.defecttracker.entity.Project;
import com.defecttracker.exception.ResourceNotFoundException;
import com.defecttracker.repository.ClientRepository;
import com.defecttracker.repository.EmployeeRepository;
import com.defecttracker.repository.KlocMetricRepository;
import com.defecttracker.repository.ProjectRepository;
import com.defecttracker.repository.WorkflowPositionRepository;
import com.defecttracker.service.ProjectSequenceService;
import com.defecttracker.service.ProjectService;
import com.defecttracker.util.PageableUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final EmployeeRepository employeeRepository;
    private final ClientRepository clientRepository;
    private final KlocMetricRepository klocMetricRepository;
    private final WorkflowPositionRepository workflowPositionRepository;
    private final ProjectSequenceService projectSequenceService;

    @Override
    @Transactional
    public Project createProject(ProjectCreateRequest request) {
        Employee manager = null;
        if (request.getManager() != null) {
            manager = employeeRepository.findById(request.getManager())
                    .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", request.getManager()));
        }

        Client client = null;
        if (request.getClientId() != null) {
            client = clientRepository.findById(request.getClientId())
                    .orElseThrow(() -> new ResourceNotFoundException("Client", "id", request.getClientId()));
        }

        String prefix = (request.getPrefix() != null && !request.getPrefix().trim().isEmpty())
                ? request.getPrefix().toUpperCase()
                : "PRJ";
        String projectId = projectSequenceService.getNextProjectCode(prefix);

        Project project = Project.builder()
                .projectId(projectId)
                .name(request.getEffectiveName())
                .prefix(prefix)
                .projectType(request.getProjectType())
                .status(request.getStatus() != null ? request.getStatus() : (request.getProjectStatus() != null ? request.getProjectStatus() : "ACTIVE"))
                .startDate(request.getStartDate() != null ? request.getStartDate() : LocalDate.now())
                .endDate(request.getEndDate())
                .manager(manager)
                .client(client)
                .clientName(client != null ? client.getClientName() : request.getClientName())
                .clientCountry(client != null ? client.getCountry() : request.getClientCountry())
                .clientState(client != null ? client.getState() : request.getClientState())
                .clientEmail(client != null ? client.getEmail() : request.getClientEmail())
                .clientPhone(client != null ? client.getPhoneNumber() : request.getClientPhone())
                .address(request.getAddress())
                .description(request.getDescription())
                .kloc(request.getKloc() != null ? request.getKloc() : 0.0)
                .progress(0.0)
                .build();

        return projectRepository.save(project);
    }

    @Override
    @Transactional
    public Project updateProject(Long id, ProjectCreateRequest request) {
        Project project = getProjectById(id);

        if (request.getManager() != null) {
            Employee manager = employeeRepository.findById(request.getManager())
                    .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", request.getManager()));
            project.setManager(manager);
        }

        if (request.getClientId() != null) {
            Client c = clientRepository.findById(request.getClientId())
                    .orElseThrow(() -> new ResourceNotFoundException("Client", "id", request.getClientId()));
            project.setClient(c);
            project.setClientName(c.getClientName());
            project.setClientCountry(c.getCountry());
            project.setClientState(c.getState());
            project.setClientEmail(c.getEmail());
            project.setClientPhone(c.getPhoneNumber());
        }

        if (request.getEffectiveName() != null) project.setName(request.getEffectiveName());
        if (request.getPrefix() != null) project.setPrefix(request.getPrefix());
        if (request.getProjectType() != null) project.setProjectType(request.getProjectType());
        if (request.getStatus() != null) project.setStatus(request.getStatus());
        if (request.getProjectStatus() != null) project.setStatus(request.getProjectStatus());
        if (request.getStartDate() != null) project.setStartDate(request.getStartDate());
        if (request.getEndDate() != null) project.setEndDate(request.getEndDate());
        if (request.getClientName() != null) project.setClientName(request.getClientName());
        if (request.getClientCountry() != null) project.setClientCountry(request.getClientCountry());
        if (request.getClientState() != null) project.setClientState(request.getClientState());
        if (request.getClientEmail() != null) project.setClientEmail(request.getClientEmail());
        if (request.getClientPhone() != null) project.setClientPhone(request.getClientPhone());
        if (request.getAddress() != null) project.setAddress(request.getAddress());
        if (request.getDescription() != null) project.setDescription(request.getDescription());
        if (request.getKloc() != null) project.setKloc(request.getKloc());

        return projectRepository.save(project);
    }

    @Override
    @Transactional(readOnly = true)
    public Project getProjectById(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Project> getAllProjects() {
        return projectRepository.findAll(Sort.by("id").descending());
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<Project> searchProjects(String query, int page, int size) {
        Pageable pageable = PageableUtils.of(Math.max(0, page), Math.max(1, size), Sort.by("id").descending());
        Page<Project> projectPage;
        if (query != null && !query.trim().isEmpty()) {
            projectPage = projectRepository.searchProjects(query.trim(), pageable);
        } else {
            projectPage = projectRepository.findAll(pageable);
        }

        return PaginatedResponse.<Project>builder()
                .content(projectPage.getContent())
                .pageNumber(projectPage.getNumber())
                .pageSize(projectPage.getSize())
                .totalElements(projectPage.getTotalElements())
                .totalPages(projectPage.getTotalPages())
                .first(projectPage.isFirst())
                .last(projectPage.isLast())
                .build();
    }

    @Override
    @Transactional
    public void deleteProject(Long id) {
        Project project = getProjectById(id);
        klocMetricRepository.deleteAll(klocMetricRepository.findByProjectId(id));
        workflowPositionRepository.deleteAll(workflowPositionRepository.findByProjectId(id));
        projectRepository.delete(project);
    }

    @Override
    @Transactional
    public Project updateKloc(Long projectId, Double kloc) {
        Project project = getProjectById(projectId);
        project.setKloc(kloc != null ? kloc : 0.0);
        return projectRepository.save(project);
    }
}

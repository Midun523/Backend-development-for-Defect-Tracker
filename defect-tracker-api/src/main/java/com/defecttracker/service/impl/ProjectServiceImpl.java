package com.defecttracker.service.impl;

import com.defecttracker.dto.request.ProjectCreateRequest;
import com.defecttracker.dto.response.PaginatedResponse;
import com.defecttracker.entity.Employee;
import com.defecttracker.entity.Project;
import com.defecttracker.exception.ResourceNotFoundException;
import com.defecttracker.repository.EmployeeRepository;
import com.defecttracker.repository.ProjectRepository;
import com.defecttracker.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final EmployeeRepository employeeRepository;
    private final com.defecttracker.repository.ClientRepository clientRepository;
    private final com.defecttracker.repository.KlocMetricRepository klocMetricRepository;
    private final com.defecttracker.repository.WorkflowPositionRepository workflowPositionRepository;

    @Override
    public Project createProject(ProjectCreateRequest request) {
        Employee manager = null;
        if (request.getManager() != null) {
            manager = employeeRepository.findById(request.getManager()).orElse(null);
        }

        com.defecttracker.entity.Client client = null;
        if (request.getClientId() != null) {
            client = clientRepository.findById(request.getClientId()).orElse(null);
        }

        long count = projectRepository.count() + 1;
        String prefix = (request.getPrefix() != null && !request.getPrefix().trim().isEmpty())
                ? request.getPrefix().toUpperCase()
                : "PRJ";
        String projectId = String.format("%s%03d", prefix, count);

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
    public Project updateProject(Long id, ProjectCreateRequest request) {
        Project project = getProjectById(id);

        if (request.getManager() != null) {
            employeeRepository.findById(request.getManager()).ifPresent(project::setManager);
        }

        if (request.getClientId() != null) {
            clientRepository.findById(request.getClientId()).ifPresent(c -> {
                project.setClient(c);
                project.setClientName(c.getClientName());
                project.setClientCountry(c.getCountry());
                project.setClientState(c.getState());
                project.setClientEmail(c.getEmail());
                project.setClientPhone(c.getPhoneNumber());
            });
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
    public Project getProjectById(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));
    }

    @Override
    public List<Project> getAllProjects() {
        return projectRepository.findAll(Sort.by("id").descending());
    }

    @Override
    public PaginatedResponse<Project> searchProjects(String query, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size), Sort.by("id").descending());
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
    @org.springframework.transaction.annotation.Transactional
    public void deleteProject(Long id) {
        Project project = getProjectById(id);
        klocMetricRepository.deleteAll(klocMetricRepository.findByProjectId(id));
        workflowPositionRepository.deleteAll(workflowPositionRepository.findByProjectId(id));
        projectRepository.delete(project);
    }

    @Override
    public Project updateKloc(Long projectId, Double kloc) {
        Project project = getProjectById(projectId);
        project.setKloc(kloc != null ? kloc : 0.0);
        return projectRepository.save(project);
    }
}

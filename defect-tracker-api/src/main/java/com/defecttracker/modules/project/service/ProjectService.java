package com.defecttracker.modules.project.service;

import com.defecttracker.common.dto.PageResponse;
import com.defecttracker.common.exception.BusinessRuleException;
import com.defecttracker.common.exception.ResourceNotFoundException;
import com.defecttracker.modules.access.entity.Employee;
import com.defecttracker.modules.access.repository.EmployeeRepository;
import com.defecttracker.modules.project.dto.ProjectDto;
import com.defecttracker.modules.project.entity.Client;
import com.defecttracker.modules.project.entity.Project;
import com.defecttracker.modules.project.entity.ProjectModule;
import com.defecttracker.modules.project.entity.SubModule;
import com.defecttracker.modules.project.entity.SubModuleDeveloperAssignment;
import com.defecttracker.modules.project.repository.ClientRepository;
import com.defecttracker.modules.project.repository.ProjectModuleRepository;
import com.defecttracker.modules.project.repository.ProjectRepository;
import com.defecttracker.modules.project.repository.SubModuleDeveloperAssignmentRepository;
import com.defecttracker.modules.project.repository.SubModuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ClientRepository clientRepository;
    private final ProjectModuleRepository moduleRepository;
    private final SubModuleRepository subModuleRepository;
    private final SubModuleDeveloperAssignmentRepository assignmentRepository;
    private final EmployeeRepository employeeRepository;

    // --- Client Operations ---
    @Transactional(readOnly = true)
    public List<ProjectDto.ClientDto> getAllClients() {
        return clientRepository.findAll().stream().map(this::mapClient).collect(Collectors.toList());
    }

    @Transactional
    public ProjectDto.ClientDto createClient(ProjectDto.ClientDto dto) {
        if (clientRepository.existsByName(dto.getName())) {
            throw new BusinessRuleException("Client with name '" + dto.getName() + "' already exists");
        }
        Client client = Client.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .address(dto.getAddress())
                .contactPerson(dto.getContactPerson())
                .build();
        return mapClient(clientRepository.save(client));
    }

    @Transactional
    public ProjectDto.ClientDto updateClient(Long id, ProjectDto.ClientDto dto) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client", "id", id));
        client.setName(dto.getName());
        client.setEmail(dto.getEmail());
        client.setPhone(dto.getPhone());
        client.setAddress(dto.getAddress());
        client.setContactPerson(dto.getContactPerson());
        return mapClient(clientRepository.save(client));
    }

    @Transactional
    public void deleteClient(Long id) {
        clientRepository.deleteById(id);
    }

    // --- Project Operations ---
    @Transactional(readOnly = true)
    public PageResponse<ProjectDto.ProjectResponse> getAllProjects(Pageable pageable) {
        Page<Project> page = projectRepository.findAll(pageable);
        return PageResponse.of(page.map(this::mapProject));
    }

    @Transactional(readOnly = true)
    public PageResponse<ProjectDto.ProjectResponse> searchProjects(String query, Pageable pageable) {
        Page<Project> page = projectRepository.searchProjects(query, pageable);
        return PageResponse.of(page.map(this::mapProject));
    }

    @Transactional(readOnly = true)
    public ProjectDto.ProjectResponse getProjectById(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));
        return mapProject(project);
    }

    @Transactional
    public ProjectDto.ProjectResponse createProject(ProjectDto.ProjectCreateRequest request) {
        if (projectRepository.existsByName(request.getName())) {
            throw new BusinessRuleException("Project with name '" + request.getName() + "' already exists");
        }

        // Validate Project Manager eligibility
        Employee pm = employeeRepository.findById(request.getProjectManagerId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", request.getProjectManagerId()));

        validateProjectManagerEligibility(pm);

        Client client = null;
        if (request.getClientId() != null) {
            client = clientRepository.findById(request.getClientId())
                    .orElseThrow(() -> new ResourceNotFoundException("Client", "id", request.getClientId()));
        }

        Project project = Project.builder()
                .name(request.getName())
                .description(request.getDescription())
                .status(request.getStatus() != null ? request.getStatus() : "ACTIVE")
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .client(client)
                .projectManager(pm)
                .kloc(request.getKloc() != null ? request.getKloc() : BigDecimal.ZERO)
                .sourceControlUrl(request.getSourceControlUrl())
                .active(true)
                .build();

        return mapProject(projectRepository.save(project));
    }

    @Transactional
    public ProjectDto.ProjectResponse updateProject(Long id, ProjectDto.ProjectUpdateRequest request) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));

        // Validate Project Manager eligibility
        Employee pm = employeeRepository.findById(request.getProjectManagerId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", request.getProjectManagerId()));

        validateProjectManagerEligibility(pm);

        Client client = null;
        if (request.getClientId() != null) {
            client = clientRepository.findById(request.getClientId())
                    .orElseThrow(() -> new ResourceNotFoundException("Client", "id", request.getClientId()));
        }

        project.setName(request.getName());
        project.setDescription(request.getDescription());
        if (request.getStatus() != null) project.setStatus(request.getStatus());
        project.setStartDate(request.getStartDate());
        project.setEndDate(request.getEndDate());
        project.setClient(client);
        project.setProjectManager(pm);
        if (request.getKloc() != null) project.setKloc(request.getKloc());
        project.setSourceControlUrl(request.getSourceControlUrl());
        if (request.getActive() != null) project.setActive(request.getActive());

        return mapProject(projectRepository.save(project));
    }

    @Transactional
    public void deleteProject(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));
        project.setActive(false);
        projectRepository.save(project);
    }

    // --- Module Operations ---
    @Transactional(readOnly = true)
    public List<ProjectDto.ModuleResponse> getModulesByProject(Long projectId) {
        return moduleRepository.findByProjectId(projectId).stream()
                .map(this::mapModule)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProjectDto.ModuleResponse createModule(ProjectDto.ModuleCreateRequest request) {
        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", request.getProjectId()));

        if (moduleRepository.existsByProjectIdAndName(project.getId(), request.getName())) {
            throw new BusinessRuleException("Module with name '" + request.getName() + "' already exists in project " + project.getName());
        }

        Employee moduleLeader = null;
        if (request.getModuleLeaderId() != null) {
            moduleLeader = employeeRepository.findById(request.getModuleLeaderId())
                    .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", request.getModuleLeaderId()));
        }

        ProjectModule module = ProjectModule.builder()
                .name(request.getName())
                .description(request.getDescription())
                .project(project)
                .moduleLeader(moduleLeader)
                .build();

        return mapModule(moduleRepository.save(module));
    }

    @Transactional
    public ProjectDto.ModuleResponse updateModule(Long id, ProjectDto.ModuleUpdateRequest request) {
        ProjectModule module = moduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ProjectModule", "id", id));

        Employee moduleLeader = null;
        if (request.getModuleLeaderId() != null) {
            moduleLeader = employeeRepository.findById(request.getModuleLeaderId())
                    .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", request.getModuleLeaderId()));
        }

        module.setName(request.getName());
        module.setDescription(request.getDescription());
        module.setModuleLeader(moduleLeader);

        return mapModule(moduleRepository.save(module));
    }

    @Transactional
    public void deleteModule(Long id) {
        moduleRepository.deleteById(id);
    }

    // --- SubModule Operations ---
    @Transactional(readOnly = true)
    public List<ProjectDto.SubModuleResponse> getSubModulesByModule(Long moduleId) {
        return subModuleRepository.findByModuleId(moduleId).stream()
                .map(this::mapSubModule)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProjectDto.SubModuleResponse createSubModule(ProjectDto.SubModuleCreateRequest request) {
        ProjectModule module = moduleRepository.findById(request.getModuleId())
                .orElseThrow(() -> new ResourceNotFoundException("ProjectModule", "id", request.getModuleId()));

        if (subModuleRepository.existsByModuleIdAndName(module.getId(), request.getName())) {
            throw new BusinessRuleException("SubModule with name '" + request.getName() + "' already exists in module " + module.getName());
        }

        SubModule subModule = SubModule.builder()
                .name(request.getName())
                .description(request.getDescription())
                .module(module)
                .build();

        return mapSubModule(subModuleRepository.save(subModule));
    }

    @Transactional
    public ProjectDto.SubModuleResponse updateSubModule(Long id, ProjectDto.SubModuleUpdateRequest request) {
        SubModule subModule = subModuleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SubModule", "id", id));

        subModule.setName(request.getName());
        subModule.setDescription(request.getDescription());

        return mapSubModule(subModuleRepository.save(subModule));
    }

    @Transactional
    public void deleteSubModule(Long id) {
        subModuleRepository.deleteById(id);
    }

    // --- Developer SubModule Assignment (Single & Bulk) ---
    @Transactional
    public ProjectDto.SubModuleResponse bulkAssignDevelopers(ProjectDto.BulkDeveloperAssignmentRequest request) {
        SubModule subModule = subModuleRepository.findById(request.getSubModuleId())
                .orElseThrow(() -> new ResourceNotFoundException("SubModule", "id", request.getSubModuleId()));

        boolean isAssign = request.getAction() == null || request.getAction().equalsIgnoreCase("ASSIGN");

        for (Long empId : request.getEmployeeIds()) {
            Employee employee = employeeRepository.findById(empId)
                    .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", empId));

            if (isAssign) {
                if (!assignmentRepository.existsBySubModuleIdAndEmployeeId(subModule.getId(), employee.getId())) {
                    SubModuleDeveloperAssignment assignment = SubModuleDeveloperAssignment.builder()
                            .subModule(subModule)
                            .employee(employee)
                            .build();
                    assignmentRepository.save(assignment);
                }
            } else {
                assignmentRepository.deleteBySubModuleIdAndEmployeeId(subModule.getId(), employee.getId());
            }
        }

        return mapSubModule(subModule);
    }

    // --- Helper Validations & Mappers ---
    private void validateProjectManagerEligibility(Employee pm) {
        if (pm.getDesignation() == null || !pm.getDesignation().isProjectManagerEligible()) {
            String desigName = pm.getDesignation() != null ? pm.getDesignation().getName() : "None";
            throw new BusinessRuleException(String.format(
                    "Employee '%s' has designation '%s' which is not eligible for Project Manager role.",
                    pm.getFullName(), desigName
            ));
        }
    }

    private ProjectDto.ClientDto mapClient(Client c) {
        return ProjectDto.ClientDto.builder()
                .id(c.getId()).name(c.getName()).email(c.getEmail())
                .phone(c.getPhone()).address(c.getAddress()).contactPerson(c.getContactPerson())
                .build();
    }

    private ProjectDto.ProjectResponse mapProject(Project p) {
        return ProjectDto.ProjectResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .description(p.getDescription())
                .status(p.getStatus())
                .startDate(p.getStartDate())
                .endDate(p.getEndDate())
                .clientId(p.getClient() != null ? p.getClient().getId() : null)
                .clientName(p.getClient() != null ? p.getClient().getName() : null)
                .projectManagerId(p.getProjectManager() != null ? p.getProjectManager().getId() : null)
                .projectManagerName(p.getProjectManager() != null ? p.getProjectManager().getFullName() : null)
                .kloc(p.getKloc())
                .sourceControlUrl(p.getSourceControlUrl())
                .active(p.isActive())
                .moduleCount(p.getModules() != null ? p.getModules().size() : 0)
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }

    private ProjectDto.ModuleResponse mapModule(ProjectModule m) {
        return ProjectDto.ModuleResponse.builder()
                .id(m.getId())
                .name(m.getName())
                .description(m.getDescription())
                .projectId(m.getProject().getId())
                .projectName(m.getProject().getName())
                .moduleLeaderId(m.getModuleLeader() != null ? m.getModuleLeader().getId() : null)
                .moduleLeaderName(m.getModuleLeader() != null ? m.getModuleLeader().getFullName() : null)
                .subModuleCount(m.getSubModules() != null ? m.getSubModules().size() : 0)
                .createdAt(m.getCreatedAt())
                .build();
    }

    private ProjectDto.SubModuleResponse mapSubModule(SubModule s) {
        List<SubModuleDeveloperAssignment> assignments = assignmentRepository.findBySubModuleId(s.getId());
        List<ProjectDto.DeveloperSummary> devs = assignments.stream().map(a -> {
            Employee e = a.getEmployee();
            return ProjectDto.DeveloperSummary.builder()
                    .employeeId(e.getId())
                    .employeeCode(e.getEmployeeId())
                    .fullName(e.getFullName())
                    .email(e.getEmail())
                    .designation(e.getDesignation() != null ? e.getDesignation().getName() : null)
                    .build();
        }).collect(Collectors.toList());

        return ProjectDto.SubModuleResponse.builder()
                .id(s.getId())
                .name(s.getName())
                .description(s.getDescription())
                .moduleId(s.getModule().getId())
                .moduleName(s.getModule().getName())
                .projectId(s.getModule().getProject().getId())
                .projectName(s.getModule().getProject().getName())
                .assignedDevelopers(devs)
                .createdAt(s.getCreatedAt())
                .build();
    }
}

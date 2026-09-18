package com.defecttracker.service.impl;

import com.defecttracker.dto.request.ModuleCreateRequest;
import com.defecttracker.entity.Employee;
import com.defecttracker.entity.Module;
import com.defecttracker.entity.ModuleLeaderAllocation;
import com.defecttracker.entity.Project;
import com.defecttracker.exception.ResourceNotFoundException;
import com.defecttracker.repository.EmployeeRepository;
import com.defecttracker.repository.ModuleLeaderAllocationRepository;
import com.defecttracker.repository.ModuleRepository;
import com.defecttracker.repository.ProjectRepository;
import com.defecttracker.service.ModuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ModuleServiceImpl implements ModuleService {

    private final ModuleRepository moduleRepository;
    private final ProjectRepository projectRepository;
    private final EmployeeRepository employeeRepository;
    private final ModuleLeaderAllocationRepository moduleLeaderAllocationRepository;

    @Override
    @Transactional
    public Module createModule(Long projectId, ModuleCreateRequest request) {
        Long targetProjectId = projectId != null ? projectId : request.getProjectId();
        if (targetProjectId == null) {
            throw new com.defecttracker.exception.BadRequestException("Project ID is required to create a module");
        }
        Project project = projectRepository.findById(targetProjectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", targetProjectId));

        Employee leader = null;
        if (request.getLeaderId() != null) {
            leader = employeeRepository.findById(request.getLeaderId()).orElse(null);
        }

        Module module = Module.builder()
                .name(request.getName())
                .description(request.getDescription())
                .project(project)
                .leader(leader)
                .build();

        Module saved = moduleRepository.save(module);

        if (leader != null) {
            allocateModuleLeader(saved.getId(), leader.getId());
        }

        return saved;
    }

    @Override
    @Transactional
    public Module updateModule(Long projectId, Long moduleId, ModuleCreateRequest request) {
        if (moduleId == null) {
            throw new com.defecttracker.exception.BadRequestException("Module ID is required to update a module");
        }
        Module module = getModuleById(moduleId);
        module.setName(request.getName());
        module.setDescription(request.getDescription());

        if (request.getLeaderId() != null) {
            employeeRepository.findById(request.getLeaderId()).ifPresent(leader -> {
                module.setLeader(leader);
                allocateModuleLeader(moduleId, leader.getId());
            });
        }

        return moduleRepository.save(module);
    }

    @Override
    public Module getModuleById(Long moduleId) {
        return moduleRepository.findById(moduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Module", "id", moduleId));
    }

    @Override
    public List<Module> getModulesByProjectId(Long projectId) {
        return moduleRepository.findByProjectId(projectId);
    }

    @Override
    @Transactional
    public void deleteModule(Long moduleId) {
        Module module = getModuleById(moduleId);
        moduleLeaderAllocationRepository.deleteByModuleId(moduleId);
        moduleRepository.delete(module);
    }

    @Override
    @Transactional
    public ModuleLeaderAllocation allocateModuleLeader(Long moduleId, Long employeeId) {
        Module module = getModuleById(moduleId);
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", employeeId));

        module.setLeader(employee);
        moduleRepository.save(module);

        ModuleLeaderAllocation allocation = ModuleLeaderAllocation.builder()
                .module(module)
                .employee(employee)
                .build();

        return moduleLeaderAllocationRepository.save(allocation);
    }

    @Override
    @Transactional
    public void deallocateModuleLeader(Long allocationId) {
        ModuleLeaderAllocation alloc = moduleLeaderAllocationRepository.findById(allocationId)
                .orElseThrow(() -> new ResourceNotFoundException("ModuleLeaderAllocation", "id", allocationId));
        if (alloc.getModule() != null) {
            alloc.getModule().setLeader(null);
            moduleRepository.save(alloc.getModule());
        }
        moduleLeaderAllocationRepository.delete(alloc);
    }

    @Override
    public ModuleLeaderAllocation getAllocatedLeader(Long moduleId) {
        return moduleLeaderAllocationRepository.findFirstByModuleIdOrderByAllocatedDateDesc(moduleId).orElse(null);
    }
}

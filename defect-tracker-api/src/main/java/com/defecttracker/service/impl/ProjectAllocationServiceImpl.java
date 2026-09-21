package com.defecttracker.service.impl;

import com.defecttracker.dto.request.ProjectAllocationRequest;
import com.defecttracker.entity.Employee;
import com.defecttracker.entity.Project;
import com.defecttracker.entity.ProjectAllocation;
import com.defecttracker.entity.Role;
import com.defecttracker.exception.BadRequestException;
import com.defecttracker.exception.ResourceNotFoundException;
import com.defecttracker.repository.EmployeeRepository;
import com.defecttracker.repository.ProjectAllocationRepository;
import com.defecttracker.repository.ProjectRepository;
import com.defecttracker.repository.RoleRepository;
import com.defecttracker.service.ProjectAllocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ProjectAllocationServiceImpl implements ProjectAllocationService {

    private final ProjectAllocationRepository projectAllocationRepository;
    private final ProjectRepository projectRepository;
    private final EmployeeRepository employeeRepository;
    private final RoleRepository roleRepository;

    @Override
    @Transactional
    public ProjectAllocation allocateEmployee(ProjectAllocationRequest request) {
        if (request.getProjectId() == null || request.getEmployeeId() == null) {
            throw new BadRequestException("Project ID and Employee ID are required");
        }

        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", request.getProjectId()));

        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", request.getEmployeeId()));

        String resolvedRole = request.getRole();
        if ((resolvedRole == null || resolvedRole.isBlank()) && request.getRoleId() != null) {
            resolvedRole = roleRepository.findById(request.getRoleId())
                    .map(Role::getRoleName)
                    .orElse(null);
        }
        if (resolvedRole == null || resolvedRole.isBlank()) {
            resolvedRole = roleRepository.findTopByOrderByIdAsc()
                    .map(Role::getRoleName)
                    .orElse("Role");
        }

        ProjectAllocation allocation = ProjectAllocation.builder()
                .project(project)
                .employee(employee)
                .startDate(request.getStartDate() != null ? request.getStartDate() : LocalDate.now())
                .endDate(request.getEndDate())
                .allocationPercentage(request.getAllocationPercentage() != null ? request.getAllocationPercentage() : 100)
                .role(resolvedRole)
                .status("ACTIVE")
                .build();

        int remaining = Math.max(0, employee.getAvailability() - allocation.getAllocationPercentage());
        employee.setAvailability(remaining);
        employeeRepository.save(employee);

        return projectAllocationRepository.save(allocation);
    }

    @Override
    @Transactional
    public void deallocateEmployee(Long id) {
        ProjectAllocation allocation = projectAllocationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ProjectAllocation", "id", id));

        allocation.setStatus("DEALLOCATED");
        Employee employee = allocation.getEmployee();
        if (employee != null) {
            int restored = Math.min(100, employee.getAvailability() + allocation.getAllocationPercentage());
            employee.setAvailability(restored);
            employeeRepository.save(employee);
        }
        projectAllocationRepository.save(allocation);
    }

    @Override
    @Transactional
    public ProjectAllocation extendAllocation(Long id, LocalDate newEndDate) {
        ProjectAllocation allocation = projectAllocationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ProjectAllocation", "id", id));

        allocation.setEndDate(newEndDate);
        allocation.setStatus("EXTENDED");
        return projectAllocationRepository.save(allocation);
    }

    @Override
    @Transactional
    public ProjectAllocation updateAllocation(Long id, ProjectAllocationRequest request) {
        ProjectAllocation allocation = projectAllocationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ProjectAllocation", "id", id));

        if (request.getEndDate() != null) {
            allocation.setEndDate(request.getEndDate());
        }
        if (request.getStartDate() != null) {
            allocation.setStartDate(request.getStartDate());
        }

        String role = request.getRole();
        if ((role == null || role.isBlank()) && request.getRoleId() != null) {
            role = roleRepository.findById(request.getRoleId())
                    .map(Role::getRoleName)
                    .orElse(null);
        }
        if (role != null && !role.isBlank()) {
            allocation.setRole(role);
        }

        if (request.getAllocationPercentage() != null) {
            int oldPercent = allocation.getAllocationPercentage() != null ? allocation.getAllocationPercentage() : 0;
            int newPercent = request.getAllocationPercentage();
            int diff = newPercent - oldPercent;
            Employee employee = allocation.getEmployee();
            if (employee != null) {
                int newAvailability = Math.max(0, Math.min(100, employee.getAvailability() - diff));
                employee.setAvailability(newAvailability);
                employeeRepository.save(employee);
            }
            allocation.setAllocationPercentage(newPercent);
        }

        return projectAllocationRepository.save(allocation);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectAllocation> getAllocationsByProject(Long projectId) {
        return projectAllocationRepository.findByProjectIdAndStatus(projectId, "ACTIVE");
    }

    @Override
    @Transactional(readOnly = true)
    public List<Employee> getEmployeesByProject(Long projectId) {
        List<ProjectAllocation> activeAllocations = projectAllocationRepository.findByProjectIdAndStatus(projectId, "ACTIVE");
        return activeAllocations.stream().map(ProjectAllocation::getEmployee).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectAllocation> getEmployeeAllocationHistory(Long projectId) {
        return projectAllocationRepository.findByProjectId(projectId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectAllocation> getAllocationsByEmployee(Long employeeId) {
        return projectAllocationRepository.findByEmployeeId(employeeId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectAllocation> getAllocations() {
        return projectAllocationRepository.findAll();
    }
}

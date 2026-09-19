package com.defecttracker.modules.allocation.service;

import com.defecttracker.common.dto.PageResponse;
import com.defecttracker.common.exception.BusinessRuleException;
import com.defecttracker.common.exception.ResourceNotFoundException;
import com.defecttracker.modules.access.entity.Employee;
import com.defecttracker.modules.access.repository.EmployeeRepository;
import com.defecttracker.modules.allocation.dto.AllocationDto;
import com.defecttracker.modules.allocation.entity.AllocationHistory;
import com.defecttracker.modules.allocation.entity.ProjectAllocation;
import com.defecttracker.modules.allocation.repository.AllocationHistoryRepository;
import com.defecttracker.modules.allocation.repository.ProjectAllocationRepository;
import com.defecttracker.modules.project.entity.Project;
import com.defecttracker.modules.project.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BenchAllocationService {

    private final ProjectAllocationRepository allocationRepository;
    private final AllocationHistoryRepository historyRepository;
    private final EmployeeRepository employeeRepository;
    private final ProjectRepository projectRepository;

    /**
     * Get employees currently on bench (available capacity > 0).
     */
    @Transactional(readOnly = true)
    public List<AllocationDto.BenchEmployeeResponse> getBenchEmployees() {
        return employeeRepository.findByActiveTrue().stream()
                .filter(e -> e.getCurrentCapacity().compareTo(BigDecimal.ZERO) > 0)
                .map(this::mapBenchEmployee)
                .collect(Collectors.toList());
    }

    /**
     * Allocate an employee to a project.
     * Consumes percentage of capacity; validates capacity cannot go negative;
     * records live allocation and writes to immutable history.
     */
    @Transactional
    public AllocationDto.ProjectAllocationResponse allocateEmployee(AllocationDto.AllocationRequest request) {
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", request.getEmployeeId()));

        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", request.getProjectId()));

        if (!project.isActive()) {
            throw new BusinessRuleException("Cannot allocate to an inactive or completed project");
        }

        // Validate capacity math
        BigDecimal availableCapacity = employee.getCurrentCapacity();
        BigDecimal requestedCapacity = request.getAllocationPercentage();

        if (availableCapacity.compareTo(requestedCapacity) < 0) {
            throw new BusinessRuleException(String.format(
                    "Capacity exceeded for employee '%s'. Available capacity is %s%%, but requested allocation is %s%%.",
                    employee.getFullName(), availableCapacity, requestedCapacity
            ));
        }

        // Deduct capacity
        BigDecimal newCapacity = availableCapacity.subtract(requestedCapacity);
        employee.setCurrentCapacity(newCapacity);
        employeeRepository.save(employee);

        // Check if employee already has active allocation in this project
        ProjectAllocation allocation = allocationRepository
                .findByEmployeeIdAndProjectIdAndActiveTrue(employee.getId(), project.getId())
                .orElse(null);

        if (allocation != null) {
            // Update existing allocation
            allocation.setAllocationPercentage(allocation.getAllocationPercentage().add(requestedCapacity));
            allocation.setEndDate(request.getEndDate());
        } else {
            allocation = ProjectAllocation.builder()
                    .employee(employee)
                    .project(project)
                    .allocationPercentage(requestedCapacity)
                    .startDate(request.getStartDate())
                    .endDate(request.getEndDate())
                    .roleInProject(request.getRoleInProject() != null ? request.getRoleInProject() : "Developer")
                    .active(true)
                    .build();
        }

        ProjectAllocation savedAllocation = allocationRepository.save(allocation);

        // Write immutable history record (independent of live record, survives deletion)
        AllocationHistory history = AllocationHistory.builder()
                .employeeId(employee.getId())
                .employeeCode(employee.getEmployeeId())
                .employeeName(employee.getFullName())
                .projectId(project.getId())
                .projectName(project.getName())
                .allocationPercentage(requestedCapacity)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .action("ALLOCATED")
                .note("Allocated " + requestedCapacity + "% to project " + project.getName())
                .build();
        historyRepository.save(history);

        log.info("Allocated employee {} to project {} at {}% (Remaining capacity: {}%)",
                employee.getEmail(), project.getName(), requestedCapacity, newCapacity);

        return mapAllocation(savedAllocation);
    }

    /**
     * Deallocate an employee from a project.
     * Restores employee capacity and writes to immutable history.
     */
    @Transactional
    public void deallocateEmployee(Long allocationId, String note) {
        ProjectAllocation allocation = allocationRepository.findById(allocationId)
                .orElseThrow(() -> new ResourceNotFoundException("ProjectAllocation", "id", allocationId));

        if (!allocation.isActive()) {
            throw new BusinessRuleException("This allocation is already deallocated");
        }

        Employee employee = allocation.getEmployee();
        Project project = allocation.getProject();
        BigDecimal releasedPercentage = allocation.getAllocationPercentage();

        // Restore capacity (capped at initialCapacity, usually 100%)
        BigDecimal restoredCapacity = employee.getCurrentCapacity().add(releasedPercentage);
        if (restoredCapacity.compareTo(employee.getInitialCapacity()) > 0) {
            restoredCapacity = employee.getInitialCapacity();
        }
        employee.setCurrentCapacity(restoredCapacity);
        employeeRepository.save(employee);

        // Deactivate allocation
        allocation.setActive(false);
        LocalDate endDate = LocalDate.now();
        allocation.setEndDate(endDate);
        allocationRepository.save(allocation);

        // Compute duration days
        long durationDays = 0;
        if (allocation.getStartDate() != null) {
            durationDays = ChronoUnit.DAYS.between(allocation.getStartDate(), endDate);
        }

        // Write immutable deallocation record
        AllocationHistory history = AllocationHistory.builder()
                .employeeId(employee.getId())
                .employeeCode(employee.getEmployeeId())
                .employeeName(employee.getFullName())
                .projectId(project.getId())
                .projectName(project.getName())
                .allocationPercentage(releasedPercentage)
                .startDate(allocation.getStartDate())
                .endDate(endDate)
                .durationDays(durationDays)
                .action("DEALLOCATED")
                .note(note != null ? note : "Deallocated from project " + project.getName())
                .build();
        historyRepository.save(history);

        log.info("Deallocated employee {} from project {} (Restored capacity: {}%)",
                employee.getEmail(), project.getName(), restoredCapacity);
    }

    /**
     * Get active allocations for an employee.
     */
    @Transactional(readOnly = true)
    public List<AllocationDto.ProjectAllocationResponse> getEmployeeActiveAllocations(Long employeeId) {
        return allocationRepository.findByEmployeeIdAndActiveTrue(employeeId).stream()
                .map(this::mapAllocation)
                .collect(Collectors.toList());
    }

    /**
     * Get active allocations for a project.
     */
    @Transactional(readOnly = true)
    public List<AllocationDto.ProjectAllocationResponse> getProjectActiveAllocations(Long projectId) {
        return allocationRepository.findByProjectIdAndActiveTrue(projectId).stream()
                .map(this::mapAllocation)
                .collect(Collectors.toList());
    }

    /**
     * Get immutable allocation history for an employee.
     */
    @Transactional(readOnly = true)
    public PageResponse<AllocationDto.AllocationHistoryResponse> getEmployeeAllocationHistory(Long employeeId, Pageable pageable) {
        Page<AllocationHistory> page = historyRepository.findByEmployeeId(employeeId, pageable);
        return PageResponse.of(page.map(this::mapHistory));
    }

    /**
     * Get immutable allocation history for a project.
     */
    @Transactional(readOnly = true)
    public PageResponse<AllocationDto.AllocationHistoryResponse> getProjectAllocationHistory(Long projectId, Pageable pageable) {
        Page<AllocationHistory> page = historyRepository.findByProjectId(projectId, pageable);
        return PageResponse.of(page.map(this::mapHistory));
    }

    // --- Mappers ---
    private AllocationDto.BenchEmployeeResponse mapBenchEmployee(Employee e) {
        List<ProjectAllocation> activeAllocs = allocationRepository.findByEmployeeIdAndActiveTrue(e.getId());
        boolean onBench = e.getCurrentCapacity().compareTo(e.getInitialCapacity()) == 0;

        return AllocationDto.BenchEmployeeResponse.builder()
                .id(e.getId())
                .employeeCode(e.getEmployeeId())
                .fullName(e.getFullName())
                .email(e.getEmail())
                .designation(e.getDesignation() != null ? e.getDesignation().getName() : null)
                .initialCapacity(e.getInitialCapacity())
                .currentCapacity(e.getCurrentCapacity())
                .onBench(onBench)
                .activeAllocationsCount(activeAllocs.size())
                .build();
    }

    private AllocationDto.ProjectAllocationResponse mapAllocation(ProjectAllocation a) {
        return AllocationDto.ProjectAllocationResponse.builder()
                .id(a.getId())
                .employeeId(a.getEmployee().getId())
                .employeeCode(a.getEmployee().getEmployeeId())
                .employeeName(a.getEmployee().getFullName())
                .projectId(a.getProject().getId())
                .projectName(a.getProject().getName())
                .allocationPercentage(a.getAllocationPercentage())
                .startDate(a.getStartDate())
                .endDate(a.getEndDate())
                .roleInProject(a.getRoleInProject())
                .active(a.isActive())
                .createdAt(a.getCreatedAt())
                .build();
    }

    private AllocationDto.AllocationHistoryResponse mapHistory(AllocationHistory h) {
        return AllocationDto.AllocationHistoryResponse.builder()
                .id(h.getId())
                .employeeId(h.getEmployeeId())
                .employeeCode(h.getEmployeeCode())
                .employeeName(h.getEmployeeName())
                .projectId(h.getProjectId())
                .projectName(h.getProjectName())
                .allocationPercentage(h.getAllocationPercentage())
                .startDate(h.getStartDate())
                .endDate(h.getEndDate())
                .action(h.getAction())
                .durationDays(h.getDurationDays())
                .note(h.getNote())
                .createdAt(h.getCreatedAt())
                .createdBy(h.getCreatedBy())
                .build();
    }
}

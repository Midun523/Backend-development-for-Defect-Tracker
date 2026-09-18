package com.defecttracker.service;

import com.defecttracker.dto.request.ProjectAllocationRequest;
import com.defecttracker.entity.Employee;
import com.defecttracker.entity.ProjectAllocation;

import java.time.LocalDate;
import java.util.List;

public interface ProjectAllocationService {
    ProjectAllocation allocateEmployee(ProjectAllocationRequest request);
    void deallocateEmployee(Long id);
    ProjectAllocation extendAllocation(Long id, LocalDate newEndDate);
    List<ProjectAllocation> getAllocationsByProject(Long projectId);
    List<Employee> getEmployeesByProject(Long projectId);
    List<ProjectAllocation> getEmployeeAllocationHistory(Long projectId);
    List<ProjectAllocation> getAllocationsByEmployee(Long employeeId);
    ProjectAllocation updateAllocation(Long id, ProjectAllocationRequest request);
    List<ProjectAllocation> getAllocations();
}

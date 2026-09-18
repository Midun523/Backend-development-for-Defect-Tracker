package com.defecttracker.service;

import com.defecttracker.dto.request.EmployeeCreateRequest;
import com.defecttracker.dto.response.PaginatedResponse;
import com.defecttracker.entity.Employee;

import java.util.List;

public interface EmployeeService {
    Employee createEmployee(EmployeeCreateRequest request);
    Employee updateEmployee(Long id, EmployeeCreateRequest request);
    Employee getEmployeeById(Long id);
    PaginatedResponse<Employee> getAllEmployees(int page, int size, String query);
    List<Employee> getAllEmployeesList();
    Employee updateEmployeeStatus(Long id, String status);
    void deleteEmployee(Long id);
    List<Employee> getBenchEmployees();
    List<Employee> getEmployeesByDesignation(Long designationId);
    List<Employee> getAvailableManagers(Long designationId, Long projectId);
}

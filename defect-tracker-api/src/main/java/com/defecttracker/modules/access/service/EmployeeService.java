package com.defecttracker.modules.access.service;

import com.defecttracker.common.dto.PageResponse;
import com.defecttracker.common.exception.BusinessRuleException;
import com.defecttracker.common.exception.ResourceNotFoundException;
import com.defecttracker.modules.access.dto.EffectivePermissionResponse;
import com.defecttracker.modules.access.dto.EmployeeCreateRequest;
import com.defecttracker.modules.access.dto.EmployeeResponse;
import com.defecttracker.modules.access.dto.EmployeeUpdateRequest;
import com.defecttracker.modules.access.dto.PermissionOverrideRequest;
import com.defecttracker.modules.access.entity.Employee;
import com.defecttracker.modules.access.entity.EmployeePermissionOverride;
import com.defecttracker.modules.access.entity.Permission;
import com.defecttracker.modules.access.entity.Role;
import com.defecttracker.modules.access.repository.EmployeePermissionOverrideRepository;
import com.defecttracker.modules.access.repository.EmployeeRepository;
import com.defecttracker.modules.access.repository.PermissionRepository;
import com.defecttracker.modules.access.repository.RoleRepository;
import com.defecttracker.modules.masterdata.entity.Designation;
import com.defecttracker.modules.masterdata.repository.DesignationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final RoleRepository roleRepository;
    private final DesignationRepository designationRepository;
    private final PermissionRepository permissionRepository;
    private final EmployeePermissionOverrideRepository overrideRepository;
    private final PasswordEncoder passwordEncoder;
    private final PermissionService permissionService;

    @Transactional
    public EmployeeResponse createEmployee(EmployeeCreateRequest request) {
        if (employeeRepository.existsByEmail(request.getEmail())) {
            throw new BusinessRuleException("Email already exists: " + request.getEmail());
        }
        if (employeeRepository.existsByEmployeeId(request.getEmployeeId())) {
            throw new BusinessRuleException("Employee ID already exists: " + request.getEmployeeId());
        }

        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", request.getRoleId()));

        Designation designation = null;
        if (request.getDesignationId() != null) {
            designation = designationRepository.findById(request.getDesignationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Designation", "id", request.getDesignationId()));
        }

        BigDecimal capacity = request.getInitialCapacity() != null ? request.getInitialCapacity() : new BigDecimal("100.00");

        Employee employee = Employee.builder()
                .employeeId(request.getEmployeeId())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .designation(designation)
                .role(role)
                .active(true)
                .initialCapacity(capacity)
                .currentCapacity(capacity)
                .build();

        Employee saved = employeeRepository.save(employee);
        return mapToResponse(saved);
    }

    @Transactional
    public EmployeeResponse updateEmployee(Long id, EmployeeUpdateRequest request) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));

        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", request.getRoleId()));

        Designation designation = null;
        if (request.getDesignationId() != null) {
            designation = designationRepository.findById(request.getDesignationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Designation", "id", request.getDesignationId()));
        }

        employee.setFirstName(request.getFirstName());
        employee.setLastName(request.getLastName());
        employee.setPhone(request.getPhone());
        employee.setRole(role);
        employee.setDesignation(designation);

        if (request.getActive() != null) {
            employee.setActive(request.getActive());
        }

        Employee updated = employeeRepository.save(employee);
        return mapToResponse(updated);
    }

    @Transactional(readOnly = true)
    public EmployeeResponse getEmployeeById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));
        return mapToResponse(employee);
    }

    @Transactional(readOnly = true)
    public PageResponse<EmployeeResponse> getAllEmployees(Pageable pageable) {
        Page<Employee> page = employeeRepository.findAll(pageable);
        return PageResponse.of(page.map(this::mapToResponse));
    }

    @Transactional(readOnly = true)
    public PageResponse<EmployeeResponse> searchEmployees(String query, Pageable pageable) {
        Page<Employee> page = employeeRepository.searchEmployees(query, pageable);
        return PageResponse.of(page.map(this::mapToResponse));
    }

    @Transactional
    public EffectivePermissionResponse updatePermissionOverrides(Long employeeId, List<PermissionOverrideRequest> requests) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", employeeId));

        // Delete existing overrides
        overrideRepository.deleteByEmployeeId(employeeId);

        // Save new overrides
        for (PermissionOverrideRequest req : requests) {
            Permission permission = permissionRepository.findById(req.getPermissionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Permission", "id", req.getPermissionId()));

            EmployeePermissionOverride override = EmployeePermissionOverride.builder()
                    .employee(employee)
                    .permission(permission)
                    .overrideType(req.getOverrideType())
                    .build();
            overrideRepository.save(override);
        }

        return permissionService.getEffectivePermissionResponse(employeeId);
    }

    @Transactional
    public void deactivateEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));
        employee.setActive(false);
        employeeRepository.save(employee);
    }

    public EmployeeResponse mapToResponse(Employee employee) {
        Set<String> effectivePermissions = permissionService.getEffectivePermissions(employee);

        return EmployeeResponse.builder()
                .id(employee.getId())
                .employeeId(employee.getEmployeeId())
                .firstName(employee.getFirstName())
                .lastName(employee.getLastName())
                .fullName(employee.getFullName())
                .email(employee.getEmail())
                .phone(employee.getPhone())
                .designationId(employee.getDesignation() != null ? employee.getDesignation().getId() : null)
                .designationName(employee.getDesignation() != null ? employee.getDesignation().getName() : null)
                .roleId(employee.getRole() != null ? employee.getRole().getId() : null)
                .roleName(employee.getRole() != null ? employee.getRole().getName() : null)
                .admin(employee.getRole() != null && employee.getRole().isAdmin())
                .active(employee.isActive())
                .initialCapacity(employee.getInitialCapacity())
                .currentCapacity(employee.getCurrentCapacity())
                .effectivePermissions(effectivePermissions)
                .createdAt(employee.getCreatedAt())
                .updatedAt(employee.getUpdatedAt())
                .build();
    }
}

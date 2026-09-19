package com.defecttracker.modules.access.service;

import com.defecttracker.common.exception.ResourceNotFoundException;
import com.defecttracker.modules.access.dto.AccessibleProjectSummary;
import com.defecttracker.modules.access.dto.EffectivePermissionResponse;
import com.defecttracker.modules.access.entity.Employee;
import com.defecttracker.modules.access.entity.EmployeePermissionOverride;
import com.defecttracker.modules.access.entity.OverrideType;
import com.defecttracker.modules.access.entity.Permission;
import com.defecttracker.modules.access.repository.EmployeePermissionOverrideRepository;
import com.defecttracker.modules.access.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionService {

    private final EmployeeRepository employeeRepository;
    private final EmployeePermissionOverrideRepository overrideRepository;
    private final JdbcTemplate jdbcTemplate;

    /**
     * Compute effective permissions: (Role Baseline) + (Employee GRANT overrides) - (Employee REVOKE overrides)
     */
    @Transactional(readOnly = true)
    public Set<String> getEffectivePermissions(Employee employee) {
        Set<String> permissions = new HashSet<>();

        // 1. Role baseline permissions
        if (employee.getRole() != null && employee.getRole().getPermissions() != null) {
            for (Permission p : employee.getRole().getPermissions()) {
                permissions.add(p.getCode());
            }
        }

        // 2. Overrides
        List<EmployeePermissionOverride> overrides = overrideRepository.findByEmployeeId(employee.getId());
        for (EmployeePermissionOverride override : overrides) {
            if (override.getOverrideType() == OverrideType.GRANT) {
                permissions.add(override.getPermission().getCode());
            } else if (override.getOverrideType() == OverrideType.REVOKE) {
                permissions.remove(override.getPermission().getCode());
            }
        }

        return permissions;
    }

    /**
     * Get accessible projects for the employee:
     * - Admins have access to all active projects.
     * - Other employees have access only to projects where they have an active allocation.
     */
    @Transactional(readOnly = true)
    public List<AccessibleProjectSummary> getAccessibleProjects(Employee employee) {
        List<AccessibleProjectSummary> projects = new ArrayList<>();

        if (employee.getRole() != null && employee.getRole().isAdmin()) {
            // Admin can access all active projects
            try {
                String sql = "SELECT id, name, description, status FROM projects WHERE is_active = true ORDER BY name";
                projects = jdbcTemplate.query(sql, (rs, rowNum) -> AccessibleProjectSummary.builder()
                        .id(rs.getLong("id"))
                        .name(rs.getString("name"))
                        .description(rs.getString("description"))
                        .status(rs.getString("status"))
                        .allocationPercentage(new BigDecimal("100.00"))
                        .isProjectManager(true)
                        .build());
            } catch (Exception e) {
                log.debug("Projects table not yet available or empty: {}", e.getMessage());
            }
        } else {
            // Regular employee: check allocations
            try {
                String sql = "SELECT p.id, p.name, p.description, p.status, a.allocation_percentage, " +
                             "(p.project_manager_id = ?) AS is_pm " +
                             "FROM projects p " +
                             "LEFT JOIN project_allocations a ON a.project_id = p.id AND a.employee_id = ? AND a.is_active = true " +
                             "WHERE p.is_active = true AND (a.id IS NOT NULL OR p.project_manager_id = ?) " +
                             "ORDER BY p.name";
                projects = jdbcTemplate.query(sql, (rs, rowNum) -> AccessibleProjectSummary.builder()
                        .id(rs.getLong("id"))
                        .name(rs.getString("name"))
                        .description(rs.getString("description"))
                        .status(rs.getString("status"))
                        .allocationPercentage(rs.getBigDecimal("allocation_percentage") != null ?
                                rs.getBigDecimal("allocation_percentage") : BigDecimal.ZERO)
                        .isProjectManager(rs.getBoolean("is_pm"))
                        .build(), employee.getId(), employee.getId(), employee.getId());
            } catch (Exception e) {
                log.debug("Project allocations query failed or table not yet created: {}", e.getMessage());
            }
        }

        return projects;
    }

    /**
     * Build the full EffectivePermissionResponse answering:
     * 1. Effective permission set
     * 2. Overrides granted/revoked
     * 3. Accessible projects
     */
    @Transactional(readOnly = true)
    public EffectivePermissionResponse getEffectivePermissionResponse(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", employeeId));

        Set<String> effective = getEffectivePermissions(employee);

        Set<String> granted = new HashSet<>();
        Set<String> revoked = new HashSet<>();
        List<EmployeePermissionOverride> overrides = overrideRepository.findByEmployeeId(employee.getId());
        for (EmployeePermissionOverride override : overrides) {
            if (override.getOverrideType() == OverrideType.GRANT) {
                granted.add(override.getPermission().getCode());
            } else {
                revoked.add(override.getPermission().getCode());
            }
        }

        List<AccessibleProjectSummary> accessibleProjects = getAccessibleProjects(employee);

        return EffectivePermissionResponse.builder()
                .id(employee.getId())
                .employeeId(employee.getEmployeeId())
                .email(employee.getEmail())
                .firstName(employee.getFirstName())
                .lastName(employee.getLastName())
                .fullName(employee.getFullName())
                .role(employee.getRole() != null ? employee.getRole().getName() : null)
                .designation(employee.getDesignation() != null ? employee.getDesignation().getName() : null)
                .admin(employee.getRole() != null && employee.getRole().isAdmin())
                .effectivePermissions(effective)
                .grantedOverrides(granted)
                .revokedOverrides(revoked)
                .accessibleProjects(accessibleProjects)
                .build();
    }

    /**
     * Check if employee has global permission AND project access for project-scoped operations.
     */
    @Transactional(readOnly = true)
    public boolean canPerformProjectAction(Long employeeId, Long projectId, String permissionCode) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", employeeId));

        // 1. Check global effective permission
        Set<String> effective = getEffectivePermissions(employee);
        if (!effective.contains(permissionCode)) {
            return false;
        }

        // 2. If Admin, always permitted
        if (employee.getRole() != null && employee.getRole().isAdmin()) {
            return true;
        }

        // 3. Check explicit project allocation
        List<AccessibleProjectSummary> projects = getAccessibleProjects(employee);
        return projects.stream().anyMatch(p -> p.getId().equals(projectId));
    }
}

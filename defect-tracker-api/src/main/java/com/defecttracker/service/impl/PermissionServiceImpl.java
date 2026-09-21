package com.defecttracker.service.impl;

import com.defecttracker.entity.Employee;
import com.defecttracker.entity.Permission;
import com.defecttracker.entity.Role;
import com.defecttracker.entity.RolePermission;
import com.defecttracker.exception.BadRequestException;
import com.defecttracker.exception.ResourceNotFoundException;
import com.defecttracker.repository.EmployeeRepository;
import com.defecttracker.repository.PermissionRepository;
import com.defecttracker.repository.RolePermissionRepository;
import com.defecttracker.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final PermissionRepository permissionRepository;
    private final EmployeeRepository employeeRepository;
    private final RolePermissionRepository rolePermissionRepository;

    @Override
    public List<Permission> getAllPermissions() {
        return permissionRepository.findAll();
    }

    @Override
    public Permission getPermissionById(Long id) {
        return permissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Permission", "permissionId", id));
    }

    @Override
    public Permission createPermission(Permission permission) {
        if (permissionRepository.existsByAction(permission.getAction())) {
            throw new BadRequestException("Permission action already exists: " + permission.getAction());
        }
        return permissionRepository.save(permission);
    }

    @Override
    public Permission updatePermission(Long id, Permission request) {
        Permission permission = getPermissionById(id);
        permission.setAction(request.getAction());
        permission.setDescription(request.getDescription());
        return permissionRepository.save(permission);
    }

    @Override
    public void deletePermission(Long id) {
        Permission permission = getPermissionById(id);
        permissionRepository.delete(permission);
    }

    @Override
    public List<String> getEmployeePermissions(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", employeeId));

        Set<String> permissions = new HashSet<>();
        if (employee.getUser() != null) {
            for (Role role : employee.getUser().getRoles()) {
                if ("Super Admin".equalsIgnoreCase(role.getRoleName())) {
                    permissions.add("ALL_PERMISSIONS");
                }
                List<RolePermission> rolePermissions = rolePermissionRepository.findByRoleId(role.getId());
                for (RolePermission rp : rolePermissions) {
                    if (rp.getPermission() != null) {
                        permissions.add(rp.getPermission().getAction());
                    }
                }
            }
        }
        return List.copyOf(permissions);
    }
}

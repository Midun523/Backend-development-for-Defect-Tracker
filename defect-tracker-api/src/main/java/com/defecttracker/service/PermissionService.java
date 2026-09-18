package com.defecttracker.service;

import com.defecttracker.entity.Permission;

import java.util.List;

public interface PermissionService {
    List<Permission> getAllPermissions();
    Permission getPermissionById(Long id);
    Permission createPermission(Permission permission);
    Permission updatePermission(Long id, Permission permission);
    void deletePermission(Long id);
    List<String> getEmployeePermissions(Long employeeId);
}

package com.defecttracker.service;

import com.defecttracker.dto.request.RolePermissionAssignRequest;
import com.defecttracker.dto.response.PaginatedResponse;
import com.defecttracker.dto.response.RolePermissionMatrixResponse;
import com.defecttracker.entity.Role;

import java.util.List;

public interface RoleService {
    Role createRole(Role role);
    Role updateRole(Long id, Role role);
    Role getRoleById(Long id);
    List<Role> getAllRoles();
    PaginatedResponse<Role> getRolesPaginated(int page, int size, String sort, String direction);
    void deleteRole(Long id);

    List<RolePermissionMatrixResponse> getRolePermissionMatrix();
    RolePermissionMatrixResponse getRolePermissionMatrixByRoleId(Long roleId);
    void assignPermissionsToRole(RolePermissionAssignRequest request);
}

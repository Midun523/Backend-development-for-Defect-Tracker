package com.defecttracker.service.impl;

import com.defecttracker.dto.request.RolePermissionAssignRequest;
import com.defecttracker.dto.response.PaginatedResponse;
import com.defecttracker.dto.response.RolePermissionMatrixResponse;
import com.defecttracker.entity.Permission;
import com.defecttracker.entity.Role;
import com.defecttracker.entity.RolePermission;
import com.defecttracker.exception.BadRequestException;
import com.defecttracker.exception.ResourceNotFoundException;
import com.defecttracker.repository.PermissionRepository;
import com.defecttracker.repository.RolePermissionRepository;
import com.defecttracker.repository.RoleRepository;
import com.defecttracker.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;

    @Override
    public Role createRole(Role role) {
        String name = role.getRoleName() != null && !role.getRoleName().trim().isEmpty()
                ? role.getRoleName().trim()
                : (role.getName() != null ? role.getName().trim() : null);

        if (name == null || name.isEmpty()) {
            throw new BadRequestException("Role name cannot be empty");
        }

        role.setRoleName(name);

        if (roleRepository.existsByRoleName(role.getRoleName())) {
            throw new BadRequestException("Role already exists: " + role.getRoleName());
        }
        return roleRepository.save(role);
    }

    @Override
    public Role updateRole(Long id, Role request) {
        Role role = getRoleById(id);
        String name = request.getRoleName() != null && !request.getRoleName().trim().isEmpty()
                ? request.getRoleName().trim()
                : (request.getName() != null ? request.getName().trim() : null);

        if (name != null && !name.isEmpty()) {
            role.setRoleName(name);
        }
        if (request.getType() != null) {
            role.setType(request.getType());
        }
        if (request.getDescription() != null) {
            role.setDescription(request.getDescription());
        }
        return roleRepository.save(role);
    }

    @Override
    public Role getRoleById(Long id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", id));
    }

    @Override
    public List<Role> getAllRoles() {
        return roleRepository.findAll(Sort.by("id").ascending());
    }

    @Override
    public PaginatedResponse<Role> getRolesPaginated(int page, int size, String sort, String direction) {
        Sort.Direction dir = "DESC".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
        String sortProperty = (sort != null && !sort.trim().isEmpty()) ? sort : "id";
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size), Sort.by(dir, sortProperty));

        Page<Role> p = roleRepository.findAll(pageable);
        return PaginatedResponse.<Role>builder()
                .content(p.getContent())
                .pageNumber(p.getNumber())
                .pageSize(p.getSize())
                .totalElements(p.getTotalElements())
                .totalPages(p.getTotalPages())
                .first(p.isFirst())
                .last(p.isLast())
                .build();
    }

    @Override
    public void deleteRole(Long id) {
        Role role = getRoleById(id);
        rolePermissionRepository.deleteByRoleId(id);
        roleRepository.delete(role);
    }

    @Override
    public List<RolePermissionMatrixResponse> getRolePermissionMatrix() {
        List<Role> roles = roleRepository.findAll();
        List<RolePermissionMatrixResponse> result = new ArrayList<>();
        for (Role role : roles) {
            result.add(getRolePermissionMatrixByRoleId(role.getId()));
        }
        return result;
    }

    @Override
    public RolePermissionMatrixResponse getRolePermissionMatrixByRoleId(Long roleId) {
        Role role = getRoleById(roleId);
        List<RolePermission> rolePermissions = rolePermissionRepository.findByRoleId(roleId);
        List<Long> permissionIds = rolePermissions.stream()
                .map(rp -> rp.getPermission().getPermissionId())
                .collect(Collectors.toList());
        List<String> permissionNames = rolePermissions.stream()
                .map(rp -> rp.getPermission().getAction())
                .collect(Collectors.toList());

        return RolePermissionMatrixResponse.builder()
                .roleId(role.getId())
                .roleName(role.getRoleName())
                .description(role.getDescription())
                .permissionIds(permissionIds)
                .permissions(permissionNames)
                .build();
    }

    @Override
    @Transactional
    public void assignPermissionsToRole(RolePermissionAssignRequest request) {
        Role role = getRoleById(request.getRoleId());
        rolePermissionRepository.deleteByRoleId(role.getId());

        if (request.getPermissionIds() != null) {
            for (Long permId : request.getPermissionIds()) {
                permissionRepository.findById(permId).ifPresent(permission -> {
                    rolePermissionRepository.save(RolePermission.builder()
                            .role(role)
                            .permission(permission)
                            .build());
                });
            }
        }
    }
}

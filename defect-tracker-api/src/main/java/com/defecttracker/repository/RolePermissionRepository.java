package com.defecttracker.repository;

import com.defecttracker.entity.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, Long> {
    List<RolePermission> findByRoleId(Long roleId);
    Optional<RolePermission> findByRoleIdAndPermissionPermissionId(Long roleId, Long permissionId);
    void deleteByRoleId(Long roleId);
    void deleteByRoleIdAndPermissionPermissionId(Long roleId, Long permissionId);
}

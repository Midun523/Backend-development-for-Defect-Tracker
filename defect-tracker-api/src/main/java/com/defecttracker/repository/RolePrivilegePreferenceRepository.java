package com.defecttracker.repository;

import com.defecttracker.entity.RolePrivilegePreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RolePrivilegePreferenceRepository extends JpaRepository<RolePrivilegePreference, Long> {
    List<RolePrivilegePreference> findByRoleId(Long roleId);
    Optional<RolePrivilegePreference> findByRoleIdAndPrivilegeTemplateId(Long roleId, Long privilegeTemplateId);
}

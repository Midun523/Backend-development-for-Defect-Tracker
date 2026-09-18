package com.defecttracker.repository;

import com.defecttracker.entity.UserPrivilegePreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserPrivilegePreferenceRepository extends JpaRepository<UserPrivilegePreference, Long> {
    List<UserPrivilegePreference> findByEmployeeId(Long employeeId);
    Optional<UserPrivilegePreference> findByEmployeeIdAndPrivilegeTemplateId(Long employeeId, Long privilegeTemplateId);
}

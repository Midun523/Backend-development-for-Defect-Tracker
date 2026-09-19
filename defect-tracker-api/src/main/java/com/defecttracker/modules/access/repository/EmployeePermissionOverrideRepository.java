package com.defecttracker.modules.access.repository;

import com.defecttracker.modules.access.entity.EmployeePermissionOverride;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeePermissionOverrideRepository extends JpaRepository<EmployeePermissionOverride, Long> {
    List<EmployeePermissionOverride> findByEmployeeId(Long employeeId);
    Optional<EmployeePermissionOverride> findByEmployeeIdAndPermissionId(Long employeeId, Long permissionId);
    void deleteByEmployeeId(Long employeeId);
}

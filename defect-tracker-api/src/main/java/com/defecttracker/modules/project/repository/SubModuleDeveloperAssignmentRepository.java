package com.defecttracker.modules.project.repository;

import com.defecttracker.modules.project.entity.SubModuleDeveloperAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubModuleDeveloperAssignmentRepository extends JpaRepository<SubModuleDeveloperAssignment, Long> {
    List<SubModuleDeveloperAssignment> findBySubModuleId(Long subModuleId);
    List<SubModuleDeveloperAssignment> findByEmployeeId(Long employeeId);
    Optional<SubModuleDeveloperAssignment> findBySubModuleIdAndEmployeeId(Long subModuleId, Long employeeId);
    boolean existsBySubModuleIdAndEmployeeId(Long subModuleId, Long employeeId);
    void deleteBySubModuleIdAndEmployeeId(Long subModuleId, Long employeeId);
    void deleteBySubModuleId(Long subModuleId);
}

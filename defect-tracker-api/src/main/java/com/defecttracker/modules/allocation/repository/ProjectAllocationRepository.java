package com.defecttracker.modules.allocation.repository;

import com.defecttracker.modules.allocation.entity.ProjectAllocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectAllocationRepository extends JpaRepository<ProjectAllocation, Long> {
    List<ProjectAllocation> findByEmployeeIdAndActiveTrue(Long employeeId);
    List<ProjectAllocation> findByProjectIdAndActiveTrue(Long projectId);
    Optional<ProjectAllocation> findByEmployeeIdAndProjectIdAndActiveTrue(Long employeeId, Long projectId);
    boolean existsByEmployeeIdAndProjectIdAndActiveTrue(Long employeeId, Long projectId);
}

package com.defecttracker.repository;

import com.defecttracker.entity.ProjectAllocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectAllocationRepository extends JpaRepository<ProjectAllocation, Long> {

    @Query("SELECT pa FROM ProjectAllocation pa WHERE pa.project.id = :projectId")
    List<ProjectAllocation> findByProjectId(@Param("projectId") Long projectId);

    @Query("SELECT pa FROM ProjectAllocation pa WHERE pa.project.id = :projectId AND pa.status = :status")
    List<ProjectAllocation> findByProjectIdAndStatus(@Param("projectId") Long projectId, @Param("status") String status);

    @Query("SELECT pa FROM ProjectAllocation pa WHERE pa.employee.id = :employeeId")
    List<ProjectAllocation> findByEmployeeId(@Param("employeeId") Long employeeId);

    @Query("SELECT pa FROM ProjectAllocation pa WHERE pa.employee.id = :employeeId AND pa.status = :status")
    List<ProjectAllocation> findByEmployeeIdAndStatus(@Param("employeeId") Long employeeId, @Param("status") String status);

    @Query("SELECT pa FROM ProjectAllocation pa WHERE pa.project.id = :projectId AND pa.employee.id = :employeeId AND pa.status = :status")
    Optional<ProjectAllocation> findByProjectIdAndEmployeeIdAndStatus(@Param("projectId") Long projectId, @Param("employeeId") Long employeeId, @Param("status") String status);
}

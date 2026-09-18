package com.defecttracker.repository;

import com.defecttracker.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    Optional<Project> findByProjectId(String projectId);
    Optional<Project> findByName(String name);
    List<Project> findByStatus(String status);
    List<Project> findByManagerId(Long managerId);

    @Query("SELECT p FROM Project p WHERE " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.projectId) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Project> searchProjects(@Param("query") String query, Pageable pageable);

    @Query("SELECT DISTINCT pa.project FROM ProjectAllocation pa WHERE pa.employee.id = :employeeId AND pa.status = 'ACTIVE'")
    List<Project> findProjectsByAllocatedEmployeeId(@Param("employeeId") Long employeeId);
}

package com.defecttracker.modules.defect.repository;

import com.defecttracker.modules.defect.entity.Defect;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DefectRepository extends JpaRepository<Defect, Long> {

    Optional<Defect> findByProjectIdAndProjectSeqNum(Long projectId, Long projectSeqNum);

    @Query("SELECT d FROM Defect d WHERE d.active = true AND d.project.id = :projectId " +
            "AND (:statusId IS NULL OR d.status.id = :statusId) " +
            "AND (:severityId IS NULL OR d.severity.id = :severityId) " +
            "AND (:priorityId IS NULL OR d.priority.id = :priorityId) " +
            "AND (:assignedToId IS NULL OR d.assignedTo.id = :assignedToId) " +
            "AND (:releaseId IS NULL OR d.release.id = :releaseId) " +
            "AND (:query IS NULL OR LOWER(d.title) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(d.defectCode) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Defect> findFilteredDefects(
            @Param("projectId") Long projectId,
            @Param("statusId") Long statusId,
            @Param("severityId") Long severityId,
            @Param("priorityId") Long priorityId,
            @Param("assignedToId") Long assignedToId,
            @Param("releaseId") Long releaseId,
            @Param("query") String query,
            Pageable pageable);

    long countByProjectIdAndActiveTrue(Long projectId);
    long countByReleaseIdAndActiveTrue(Long releaseId);

    // Severity breakdown
    @Query("SELECT d.severity.name, COUNT(d) FROM Defect d WHERE d.project.id = :projectId AND d.active = true GROUP BY d.severity.name")
    List<Object[]> countBySeverityForProject(@Param("projectId") Long projectId);

    @Query("SELECT d.severity.name, COUNT(d) FROM Defect d WHERE d.release.id = :releaseId AND d.active = true GROUP BY d.severity.name")
    List<Object[]> countBySeverityForRelease(@Param("releaseId") Long releaseId);

    // Module breakdown
    @Query("SELECT d.module.name, COUNT(d) FROM Defect d WHERE d.project.id = :projectId AND d.active = true GROUP BY d.module.name")
    List<Object[]> countByModuleForProject(@Param("projectId") Long projectId);

    // Defect Type breakdown
    @Query("SELECT d.defectType.name, COUNT(d) FROM Defect d WHERE d.project.id = :projectId AND d.active = true GROUP BY d.defectType.name")
    List<Object[]> countByTypeForProject(@Param("projectId") Long projectId);

    // Created vs Fixed in release window
    @Query("SELECT COUNT(d) FROM Defect d WHERE d.project.id = :projectId AND d.createdAt BETWEEN :startDate AND :endDate")
    long countCreatedBetween(@Param("projectId") Long projectId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(d) FROM Defect d WHERE d.project.id = :projectId AND d.resolvedAt IS NOT NULL AND d.resolvedAt BETWEEN :startDate AND :endDate")
    long countFixedBetween(@Param("projectId") Long projectId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // Defects for time-to-find & time-to-fix metrics
    @Query("SELECT d FROM Defect d WHERE d.project.id = :projectId AND d.firstAssignedAt IS NOT NULL")
    List<Defect> findAssignedDefectsForProject(@Param("projectId") Long projectId);

    @Query("SELECT d FROM Defect d WHERE d.project.id = :projectId AND d.resolvedAt IS NOT NULL")
    List<Defect> findResolvedDefectsForProject(@Param("projectId") Long projectId);
}

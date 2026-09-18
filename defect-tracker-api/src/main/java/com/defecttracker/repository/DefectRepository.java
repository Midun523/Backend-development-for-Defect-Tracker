package com.defecttracker.repository;

import com.defecttracker.entity.Defect;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DefectRepository extends JpaRepository<Defect, Long> {
    Optional<Defect> findByDefectId(String defectId);
    List<Defect> findByProjectId(Long projectId);
    List<Defect> findByProjectIdAndReleaseId(Long projectId, Long releaseId);
    List<Defect> findByAssignedToId(Long assignedToId);
    long countByDefectStatusId(Long statusId);

    @Query("SELECT d FROM Defect d WHERE d.project.id = :projectId " +
           "AND (:releaseId IS NULL OR d.release.id = :releaseId) " +
           "AND (:severityId IS NULL OR d.severity.id = :severityId) " +
           "AND (:priorityId IS NULL OR d.priority.id = :priorityId) " +
           "AND (:statusId IS NULL OR d.defectStatus.id = :statusId) " +
           "AND (:typeId IS NULL OR d.defectType.id = :typeId) " +
           "AND (:moduleId IS NULL OR d.module.id = :moduleId) " +
           "AND (:subModuleId IS NULL OR d.subModule.id = :subModuleId) " +
           "AND (:assignedToId IS NULL OR d.assignedTo.id = :assignedToId)")
    Page<Defect> filterDefects(@Param("projectId") Long projectId,
                              @Param("releaseId") Long releaseId,
                              @Param("severityId") Long severityId,
                              @Param("priorityId") Long priorityId,
                              @Param("statusId") Long statusId,
                              @Param("typeId") Long typeId,
                              @Param("moduleId") Long moduleId,
                              @Param("subModuleId") Long subModuleId,
                              @Param("assignedToId") Long assignedToId,
                              Pageable pageable);

    @Query("SELECT d.severity.name, COUNT(d) FROM Defect d WHERE d.project.id = :projectId GROUP BY d.severity.name")
    List<Object[]> countBySeverityForProject(@Param("projectId") Long projectId);

    @Query("SELECT d.defectType.name, COUNT(d) FROM Defect d WHERE d.project.id = :projectId GROUP BY d.defectType.name")
    List<Object[]> countByTypeForProject(@Param("projectId") Long projectId);

    @Query("SELECT d.module.name, COUNT(d) FROM Defect d WHERE d.project.id = :projectId GROUP BY d.module.name")
    List<Object[]> countByModuleForProject(@Param("projectId") Long projectId);

    @Query("SELECT COUNT(d) FROM Defect d WHERE d.project.id = :projectId AND (:releaseId IS NULL OR d.release.id = :releaseId)")
    long countTotalDefects(@Param("projectId") Long projectId, @Param("releaseId") Long releaseId);

    @Query("SELECT COUNT(d) FROM Defect d WHERE d.project.id = :projectId AND (:releaseId IS NULL OR d.release.id = :releaseId) AND LOWER(d.defectStatus.name) IN ('closed', 'resolved')")
    long countFixedDefects(@Param("projectId") Long projectId, @Param("releaseId") Long releaseId);

    @Query("SELECT COUNT(d) FROM Defect d WHERE d.project.id = :projectId AND d.reOpenCount > 0")
    long countReopenedDefectsForProject(@Param("projectId") Long projectId);
}

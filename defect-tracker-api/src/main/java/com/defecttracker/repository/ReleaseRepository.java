package com.defecttracker.repository;

import com.defecttracker.entity.Release;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReleaseRepository extends JpaRepository<Release, Long> {
    @Query("SELECT r FROM Release r WHERE r.project.id = :projectId")
    List<Release> findByProjectId(@Param("projectId") Long projectId);

    @Query("SELECT r FROM Release r WHERE r.project.id = :projectId AND r.status = :status")
    List<Release> findByProjectIdAndStatus(@Param("projectId") Long projectId, @Param("status") String status);

    @Query("SELECT r FROM Release r WHERE r.project.id = :projectId AND r.status = :status ORDER BY r.createdAt DESC")
    List<Release> findByProjectIdAndStatusOrderByCreatedAtDesc(@Param("projectId") Long projectId, @Param("status") String status);
}

package com.defecttracker.modules.release.repository;

import com.defecttracker.modules.release.entity.Release;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReleaseRepository extends JpaRepository<Release, Long> {
    List<Release> findByProjectIdOrderByReleaseDateDesc(Long projectId);
    Page<Release> findByProjectId(Long projectId, Pageable pageable);
    Optional<Release> findByProjectIdAndVersion(Long projectId, String version);
    boolean existsByProjectIdAndVersion(Long projectId, String version);
    long countByProjectId(Long projectId);
    long countByProjectIdAndStatus(Long projectId, String status);
}

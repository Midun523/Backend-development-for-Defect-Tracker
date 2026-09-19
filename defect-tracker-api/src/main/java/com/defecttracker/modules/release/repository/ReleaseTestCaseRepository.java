package com.defecttracker.modules.release.repository;

import com.defecttracker.modules.release.entity.ReleaseTestCase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReleaseTestCaseRepository extends JpaRepository<ReleaseTestCase, Long> {
    List<ReleaseTestCase> findByReleaseId(Long releaseId);
    Page<ReleaseTestCase> findByReleaseId(Long releaseId, Pageable pageable);
    Optional<ReleaseTestCase> findByReleaseIdAndTestCaseId(Long releaseId, Long testCaseId);
    boolean existsByReleaseIdAndTestCaseId(Long releaseId, Long testCaseId);
    long countByReleaseId(Long releaseId);
    long countByReleaseIdAndExecutionStatus(Long releaseId, String executionStatus);
    List<ReleaseTestCase> findByReleaseIdAndAssignedToId(Long releaseId, Long assignedToId);
}

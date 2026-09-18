package com.defecttracker.repository;

import com.defecttracker.entity.ReleaseTestCase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReleaseTestCaseRepository extends JpaRepository<ReleaseTestCase, Long> {
    List<ReleaseTestCase> findByReleaseId(Long releaseId);
    List<ReleaseTestCase> findByAssignedQaId(Long employeeId);
    Optional<ReleaseTestCase> findByReleaseIdAndTestCaseId(Long releaseId, Long testCaseId);
    void deleteByReleaseIdAndTestCaseId(Long releaseId, Long testCaseId);
}

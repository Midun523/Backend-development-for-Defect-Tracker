package com.defecttracker.repository;

import com.defecttracker.entity.TestCase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestCaseRepository extends JpaRepository<TestCase, Long> {
    List<TestCase> findBySubModuleId(Long subModuleId);
    List<TestCase> findBySubModuleModuleProjectId(Long projectId);
    List<TestCase> findBySubModuleIdIn(List<Long> subModuleIds);

    @Query("SELECT tc FROM TestCase tc WHERE tc.subModule.id = :subModuleId " +
           "AND (:description IS NULL OR LOWER(tc.description) LIKE LOWER(CONCAT('%', :description, '%')) " +
           "     OR LOWER(tc.testcaseNo) LIKE LOWER(CONCAT('%', :description, '%'))) " +
           "AND (:defectTypeId IS NULL OR tc.defectType.id = :defectTypeId) " +
           "AND (:severityId IS NULL OR tc.severity.id = :severityId)")
    Page<TestCase> filterTestCases(@Param("subModuleId") Long subModuleId,
                                  @Param("description") String description,
                                  @Param("defectTypeId") Long defectTypeId,
                                  @Param("severityId") Long severityId,
                                  Pageable pageable);
}

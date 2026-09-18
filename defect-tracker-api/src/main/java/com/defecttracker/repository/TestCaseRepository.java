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
    Page<TestCase> findBySubModuleId(Long subModuleId, Pageable pageable);
    List<TestCase> findBySubModuleModuleProjectId(Long projectId);
    Page<TestCase> findBySubModuleModuleProjectId(Long projectId, Pageable pageable);
    List<TestCase> findBySubModuleModuleId(Long moduleId);
    Page<TestCase> findBySubModuleModuleId(Long moduleId, Pageable pageable);
    List<TestCase> findBySubModuleIdIn(List<Long> subModuleIds);

    @Query("SELECT tc FROM TestCase tc WHERE tc.subModule.id = :subModuleId " +
           "AND (:search IS NULL OR LOWER(tc.description) LIKE :search OR LOWER(tc.testcaseNo) LIKE :search) " +
           "AND (:defectTypeId IS NULL OR tc.defectType.id = :defectTypeId) " +
           "AND (:severityId IS NULL OR tc.severity.id = :severityId)")
    Page<TestCase> filterTestCases(@Param("subModuleId") Long subModuleId,
                                  @Param("search") String search,
                                  @Param("defectTypeId") Long defectTypeId,
                                  @Param("severityId") Long severityId,
                                  Pageable pageable);

    @Query("SELECT tc FROM TestCase tc WHERE tc.subModule.module.id = :moduleId " +
           "AND (:search IS NULL OR LOWER(tc.description) LIKE :search OR LOWER(tc.testcaseNo) LIKE :search) " +
           "AND (:defectTypeId IS NULL OR tc.defectType.id = :defectTypeId) " +
           "AND (:severityId IS NULL OR tc.severity.id = :severityId)")
    Page<TestCase> filterTestCasesByModule(@Param("moduleId") Long moduleId,
                                         @Param("search") String search,
                                         @Param("defectTypeId") Long defectTypeId,
                                         @Param("severityId") Long severityId,
                                         Pageable pageable);

    @Query("SELECT tc FROM TestCase tc WHERE tc.subModule.module.project.id = :projectId " +
           "AND (:search IS NULL OR LOWER(tc.description) LIKE :search OR LOWER(tc.testcaseNo) LIKE :search) " +
           "AND (:defectTypeId IS NULL OR tc.defectType.id = :defectTypeId) " +
           "AND (:severityId IS NULL OR tc.severity.id = :severityId)")
    Page<TestCase> filterTestCasesByProject(@Param("projectId") Long projectId,
                                          @Param("search") String search,
                                          @Param("defectTypeId") Long defectTypeId,
                                          @Param("severityId") Long severityId,
                                          Pageable pageable);
}

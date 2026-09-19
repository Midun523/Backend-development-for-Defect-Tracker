package com.defecttracker.modules.testcase.repository;

import com.defecttracker.modules.testcase.entity.TestCase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TestCaseRepository extends JpaRepository<TestCase, Long> {

    List<TestCase> findBySubModuleIdAndActiveTrue(Long subModuleId);

    Optional<TestCase> findByTestCaseCode(String testCaseCode);

    long countBySubModuleModuleProjectId(Long projectId);

    @Query("SELECT tc FROM TestCase tc WHERE tc.active = true AND tc.subModule.module.project.id = :projectId")
    Page<TestCase> findByProjectId(@Param("projectId") Long projectId, Pageable pageable);

    @Query("SELECT tc FROM TestCase tc WHERE tc.active = true AND tc.subModule.module.project.id = :projectId AND (" +
            "LOWER(tc.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(tc.testCaseCode) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(tc.steps) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(tc.description) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<TestCase> searchTestCasesByProject(
            @Param("projectId") Long projectId,
            @Param("query") String query,
            Pageable pageable);

    @Query("SELECT tc FROM TestCase tc WHERE tc.active = true AND tc.subModule.module.project.id = :projectId")
    List<TestCase> findAllByProjectId(@Param("projectId") Long projectId);
}

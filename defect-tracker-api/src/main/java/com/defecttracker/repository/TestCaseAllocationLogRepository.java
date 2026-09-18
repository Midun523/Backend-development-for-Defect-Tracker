package com.defecttracker.repository;

import com.defecttracker.entity.TestCaseAllocationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestCaseAllocationLogRepository extends JpaRepository<TestCaseAllocationLog, Long> {
    List<TestCaseAllocationLog> findByReleaseId(Long releaseId);
    List<TestCaseAllocationLog> findByTestCaseId(Long testCaseId);
    List<TestCaseAllocationLog> findByEmployeeId(Long employeeId);
}

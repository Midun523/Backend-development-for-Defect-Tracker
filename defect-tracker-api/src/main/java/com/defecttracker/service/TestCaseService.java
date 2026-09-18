package com.defecttracker.service;

import com.defecttracker.dto.request.TestCaseCreateRequest;
import com.defecttracker.dto.response.PaginatedResponse;
import com.defecttracker.entity.TestCase;

import java.util.List;

public interface TestCaseService {
    TestCase createTestCase(Long subModuleId, TestCaseCreateRequest request);
    TestCase updateTestCase(Long subModuleId, Long id, TestCaseCreateRequest request);
    TestCase getTestCaseById(Long id);
    PaginatedResponse<TestCase> getTestCasesBySubModule(Long subModuleId, String description, Long defectTypeId, Long severityId, int page, int size);
    PaginatedResponse<TestCase> getTestCasesByModule(Long moduleId, String description, Long defectTypeId, Long severityId, int page, int size);
    PaginatedResponse<TestCase> getTestCasesByProject(Long projectId, String description, Long defectTypeId, Long severityId, int page, int size);
    List<TestCase> getAllTestCases();
    List<TestCase> createBulkTestCases(List<TestCaseCreateRequest> requests);
    void deleteTestCase(Long id);
}

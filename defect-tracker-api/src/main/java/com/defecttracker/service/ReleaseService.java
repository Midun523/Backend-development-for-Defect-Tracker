package com.defecttracker.service;

import com.defecttracker.dto.request.ReleaseCreateRequest;
import com.defecttracker.entity.Release;
import com.defecttracker.entity.ReleaseTestCase;
import com.defecttracker.entity.TestCaseAllocationLog;

import java.util.List;
import java.util.Map;

public interface ReleaseService {
    Release createRelease(ReleaseCreateRequest request);
    Release updateRelease(Long id, ReleaseCreateRequest request);
    Release getReleaseById(Long id);
    List<Release> getAllReleases();
    List<Release> getReleasesByProject(Long projectId);
    Release getActiveReleaseByProject(Long projectId);
    Release updateReleaseStatus(Long releaseId, String status);
    Release updateReleaseKloc(Long releaseId, Double kloc);
    void deleteRelease(Long id);
    Map<String, Object> getReleaseCounts();

    List<ReleaseTestCase> getReleaseTestCases(Long releaseId);
    ReleaseTestCase getReleaseTestCase(Long releaseId, Long testCaseId);
    ReleaseTestCase assignQaToReleaseTestCase(Long releaseId, Long testCaseId, Long employeeId);
    ReleaseTestCase updateReleaseTestCaseStatus(Long releaseId, Long testCaseId, String status, String comment);
    ReleaseTestCase updateReleaseTestCaseStatus(Long releaseId, Long testCaseId, String status, String comment, Long priorityId, Long assignedToId);
    List<TestCaseAllocationLog> getTestCaseAllocationLogs();
}

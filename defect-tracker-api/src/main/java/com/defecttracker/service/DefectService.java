package com.defecttracker.service;

import com.defecttracker.dto.request.DefectBulkReassignRequest;
import com.defecttracker.dto.request.DefectCommentRequest;
import com.defecttracker.dto.request.DefectCreateRequest;
import com.defecttracker.dto.request.DefectStatusChangeRequest;
import com.defecttracker.dto.response.PaginatedResponse;
import com.defecttracker.entity.Defect;
import com.defecttracker.entity.DefectComment;
import com.defecttracker.entity.DefectHistory;
import com.defecttracker.entity.DefectStatusLog;

import java.util.List;
import java.util.Map;

public interface DefectService {
    Defect createDefect(DefectCreateRequest request);
    Defect updateDefect(Long id, DefectCreateRequest request);
    Defect getDefectById(Long id);
    List<Defect> getDefectsByProject(Long projectId);
    PaginatedResponse<Defect> filterDefects(Long projectId, Long releaseId, Long severityId, Long priorityId, Long statusId, Long typeId, Long moduleId, Long subModuleId, Long assignedToId, int page, int size);
    Defect changeDefectStatus(Long defectId, DefectStatusChangeRequest request, String changedByUser);
    Defect assignDeveloper(Long defectId, Long employeeId);
    void bulkReassign(DefectBulkReassignRequest request);
    void deleteDefect(Long id);

    DefectComment addComment(Long defectId, DefectCommentRequest request, String userEmail);
    List<DefectComment> getCommentsByDefect(Long defectId);
    List<DefectHistory> getDefectHistory(Long defectId);
    List<DefectStatusLog> getDefectStatusLogs(Long projectId, Long releaseId);
}

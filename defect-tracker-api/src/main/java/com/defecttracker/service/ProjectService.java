package com.defecttracker.service;

import com.defecttracker.dto.request.ProjectCreateRequest;
import com.defecttracker.dto.response.PaginatedResponse;
import com.defecttracker.entity.Project;

import java.util.List;

public interface ProjectService {
    Project createProject(ProjectCreateRequest request);
    Project updateProject(Long id, ProjectCreateRequest request);
    Project getProjectById(Long id);
    List<Project> getAllProjects();
    PaginatedResponse<Project> searchProjects(String query, int page, int size);
    void deleteProject(Long id);
    Project updateKloc(Long projectId, Double kloc);
}

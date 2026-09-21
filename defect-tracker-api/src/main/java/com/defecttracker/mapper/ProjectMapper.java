package com.defecttracker.mapper;

import com.defecttracker.dto.response.ProjectResponse;
import com.defecttracker.dto.response.ProjectSummary;
import com.defecttracker.entity.Project;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring", uses = {EmployeeMapper.class})
public interface ProjectMapper {
    ProjectSummary toSummary(Project project);
    ProjectResponse toResponse(Project project);
    List<ProjectResponse> toResponseList(List<Project> projects);
}

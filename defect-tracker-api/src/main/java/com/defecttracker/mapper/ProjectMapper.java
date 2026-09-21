package com.defecttracker.mapper;

import com.defecttracker.dto.response.ProjectSummary;
import com.defecttracker.entity.Project;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProjectMapper {
    ProjectSummary toSummary(Project project);
}

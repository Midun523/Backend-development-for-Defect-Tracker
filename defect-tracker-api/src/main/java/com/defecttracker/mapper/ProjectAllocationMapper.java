package com.defecttracker.mapper;

import com.defecttracker.dto.response.ProjectAllocationResponse;
import com.defecttracker.entity.ProjectAllocation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {ProjectMapper.class, EmployeeMapper.class})
public interface ProjectAllocationMapper {

    @Mapping(target = "email", expression = "java(allocation.getEmail())")
    @Mapping(target = "projectId", expression = "java(allocation.getProjectId())")
    @Mapping(target = "lastName", expression = "java(allocation.getLastName())")
    @Mapping(target = "firstName", expression = "java(allocation.getFirstName())")
    @Mapping(target = "roleName", expression = "java(allocation.getRoleName())")
    @Mapping(target = "employeeId", expression = "java(allocation.getEmployeeId())")
    @Mapping(target = "userId", expression = "java(allocation.getUserId())")
    @Mapping(target = "roleId", expression = "java(allocation.getRoleId())")
    @Mapping(target = "status", expression = "java(allocation.getStatusForJson())")
    @Mapping(target = "projectName", expression = "java(allocation.getProjectName())")
    @Mapping(target = "userFullName", expression = "java(allocation.getUserFullName())")
    @Mapping(target = "allocationPercent", expression = "java(allocation.getAllocationPercent())")
    ProjectAllocationResponse toResponse(ProjectAllocation allocation);

    List<ProjectAllocationResponse> toResponseList(List<ProjectAllocation> list);
}

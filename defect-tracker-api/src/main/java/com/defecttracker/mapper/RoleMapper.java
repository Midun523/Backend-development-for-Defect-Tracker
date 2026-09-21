package com.defecttracker.mapper;

import com.defecttracker.dto.request.RoleRequest;
import com.defecttracker.dto.response.RoleResponse;
import com.defecttracker.dto.response.RoleSummary;
import com.defecttracker.entity.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    @Mapping(target = "name", expression = "java(role.getRoleName())")
    RoleResponse toResponse(Role role);

    @Mapping(target = "name", expression = "java(role.getRoleName())")
    RoleSummary toSummary(Role role);

    List<RoleResponse> toResponseList(List<Role> roles);
    Set<RoleSummary> toSummarySet(Set<Role> roles);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "roleName", expression = "java(request.getEffectiveRoleName())")
    Role toEntity(RoleRequest request);
}

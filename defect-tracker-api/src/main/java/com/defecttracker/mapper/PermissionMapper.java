package com.defecttracker.mapper;

import com.defecttracker.dto.request.PermissionRequest;
import com.defecttracker.dto.response.PermissionResponse;
import com.defecttracker.entity.Permission;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
    PermissionResponse toResponse(Permission permission);
    List<PermissionResponse> toResponseList(List<Permission> permissions);

    @Mapping(target = "permissionId", ignore = true)
    Permission toEntity(PermissionRequest request);
}

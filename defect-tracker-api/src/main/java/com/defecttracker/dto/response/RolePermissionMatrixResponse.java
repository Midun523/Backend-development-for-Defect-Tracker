package com.defecttracker.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RolePermissionMatrixResponse {
    private Long roleId;
    private String roleName;
    private String description;
    private List<Long> permissionIds;
    private List<String> permissions;
}

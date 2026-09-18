package com.defecttracker.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RolePermissionAssignRequest {
    @NotNull(message = "Role ID is required")
    private Long roleId;
    @NotNull(message = "Permission IDs list is required")
    private List<Long> permissionIds;
}

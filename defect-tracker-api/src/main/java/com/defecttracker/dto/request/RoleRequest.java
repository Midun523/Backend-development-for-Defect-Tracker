package com.defecttracker.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleRequest {
    @NotBlank(message = "Role name is required")
    private String roleName;
    private String type;
    private String description;
    private String name;

    public String getEffectiveRoleName() {
        if (roleName != null && !roleName.trim().isEmpty()) {
            return roleName;
        }
        return name;
    }
}

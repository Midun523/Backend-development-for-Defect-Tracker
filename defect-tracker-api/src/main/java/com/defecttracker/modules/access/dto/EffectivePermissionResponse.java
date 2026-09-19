package com.defecttracker.modules.access.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EffectivePermissionResponse {

    private Long id;
    private String employeeId;
    private String email;
    private String firstName;
    private String lastName;
    private String fullName;
    private String role;
    private String designation;
    private boolean admin;

    // Full effective permission codes (Role baseline + Grants - Revokes)
    private Set<String> effectivePermissions;

    // Specific overrides configured for this employee
    private Set<String> grantedOverrides;
    private Set<String> revokedOverrides;

    // List of projects the user is explicitly allocated to (or all if admin)
    private List<AccessibleProjectSummary> accessibleProjects;
}

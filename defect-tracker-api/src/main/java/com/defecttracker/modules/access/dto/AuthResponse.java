package com.defecttracker.modules.access.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String accessToken;
    private String refreshToken;
    @Builder.Default
    private String tokenType = "Bearer";
    private long expiresInMs;

    // User info
    private Long id;
    private String employeeId;
    private String email;
    private String firstName;
    private String lastName;
    private String fullName;
    private String role;
    private String designation;
    private boolean admin;
    private Set<String> effectivePermissions;
}

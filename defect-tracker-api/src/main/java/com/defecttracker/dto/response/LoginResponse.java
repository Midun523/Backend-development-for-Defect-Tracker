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
public class LoginResponse {
    private String token;
    private String refreshToken;
    @Builder.Default
    private String type = "Bearer";
    private Long userId;
    private Long employeeId;
    private Long companyStaffId;
    private String email;
    private String firstName;
    private String lastName;
    private String userType;
    private List<String> roles;
    private List<String> globalPermissions;
    private List<String> projectAccessList;
}

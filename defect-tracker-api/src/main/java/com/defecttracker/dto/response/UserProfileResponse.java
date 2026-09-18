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
public class UserProfileResponse {
    private Long id;
    private String userId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String gender;
    private String userStatus;
    private String userType;
    private Long designationId;
    private String designationName;
    private List<String> roles;
    private List<String> permissions;
    private List<String> projectAccessList;
}

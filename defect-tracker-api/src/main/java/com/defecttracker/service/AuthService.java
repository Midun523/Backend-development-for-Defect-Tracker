package com.defecttracker.service;

import com.defecttracker.dto.request.*;
import com.defecttracker.dto.response.LoginResponse;
import com.defecttracker.dto.response.UserProfileResponse;
import com.defecttracker.entity.Project;

import java.util.List;

public interface AuthService {
    LoginResponse login(LoginRequest request);
    LoginResponse refreshToken(RefreshTokenRequest request);
    void changePassword(String userEmail, ChangePasswordRequest request);
    void forgetPassword(ForgetPasswordRequest request);
    void resetPassword(ResetPasswordRequest request);
    UserProfileResponse getCurrentUserProfile(String userEmail);
    List<String> getCurrentUserPermissions(String userEmail);
    List<Project> getCurrentUserProjects(String userEmail);
    List<String> getCurrentUserProjectPermissions(String userEmail, Long projectId);
}

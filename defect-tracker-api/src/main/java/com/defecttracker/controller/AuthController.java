package com.defecttracker.controller;

import com.defecttracker.dto.request.*;
import com.defecttracker.dto.response.ApiResponse;
import com.defecttracker.dto.response.LoginResponse;
import com.defecttracker.dto.response.UserProfileResponse;
import com.defecttracker.entity.Project;
import com.defecttracker.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Authentication & User Context", description = "Endpoints for login, token refresh, password resets and user profile")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/auth/login")
    @Operation(summary = "Authenticate user and get JWT tokens")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Login successful"));
    }

    @PostMapping("/auth/refresh-token")
    @Operation(summary = "Renew access token using refresh token")
    public ResponseEntity<ApiResponse<LoginResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        LoginResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Token refreshed successfully"));
    }

    @PostMapping("/auth/change-password")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Change password for authenticated user")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            Authentication authentication,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        authService.changePassword(authentication.getName(), request);
        return ResponseEntity.ok(ApiResponse.success(null, "Password changed successfully"));
    }

    @PostMapping("/auth/forget-password")
    @Operation(summary = "Request password reset token")
    public ResponseEntity<ApiResponse<Void>> forgetPassword(@Valid @RequestBody ForgetPasswordRequest request) {
        authService.forgetPassword(request);
        return ResponseEntity.ok(ApiResponse.success(null, "Password reset instructions dispatched"));
    }

    @PostMapping("/auth/reset-password")
    @Operation(summary = "Reset password with reset token")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success(null, "Password reset successful"));
    }

    @GetMapping("/auth/validate-reset-token")
    @Operation(summary = "Validate password reset token state")
    public ResponseEntity<ApiResponse<java.util.Map<String, Object>>> validateResetToken(@RequestParam String token) {
        boolean valid = authService.validateResetToken(token);
        java.util.Map<String, Object> data = java.util.Map.of(
                "valid", valid,
                "status", valid ? "VALID" : "INVALID"
        );
        return ResponseEntity.ok(ApiResponse.success(data, valid ? "Reset token is valid" : "Reset token is invalid or expired"));
    }

    @PostMapping({"/auth/log-out", "/auth/logout"})
    @Operation(summary = "Logout user")
    public ResponseEntity<ApiResponse<Void>> logout(
            Authentication authentication,
            @RequestBody(required = false) RefreshTokenRequest request
    ) {
        authService.logout(authentication != null ? authentication.getName() : null, request);
        return ResponseEntity.ok(ApiResponse.success(null, "Logged out successfully"));
    }

    @GetMapping("/user/me/permissions")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get current authenticated user's permissions")
    public ResponseEntity<ApiResponse<List<String>>> getCurrentUserPermissions(Authentication authentication) {
        List<String> permissions = authService.getCurrentUserPermissions(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(permissions, "Permissions retrieved"));
    }

    @GetMapping("/user/me/projects")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get accessible projects for current user")
    public ResponseEntity<ApiResponse<List<Project>>> getCurrentUserProjects(Authentication authentication) {
        List<Project> projects = authService.getCurrentUserProjects(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(projects, "Projects retrieved"));
    }

    @GetMapping("/user/me/projects/{projectId}/permissions")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get project permissions for current user")
    public ResponseEntity<ApiResponse<List<String>>> getCurrentUserProjectPermissions(
            Authentication authentication,
            @PathVariable Long projectId
    ) {
        List<String> permissions = authService.getCurrentUserProjectPermissions(authentication.getName(), projectId);
        return ResponseEntity.ok(ApiResponse.success(permissions, "Project permissions retrieved"));
    }

    @GetMapping("/user/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get current user profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getCurrentUser(Authentication authentication) {
        UserProfileResponse profile = authService.getCurrentUserProfile(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(profile, "User profile retrieved"));
    }
}

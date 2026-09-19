package com.defecttracker.modules.access.controller;

import com.defecttracker.common.dto.ApiResponse;
import com.defecttracker.modules.access.dto.AuthResponse;
import com.defecttracker.modules.access.dto.EffectivePermissionResponse;
import com.defecttracker.modules.access.dto.ForgotPasswordRequest;
import com.defecttracker.modules.access.dto.LoginRequest;
import com.defecttracker.modules.access.dto.RefreshTokenRequest;
import com.defecttracker.modules.access.dto.ResetPasswordRequest;
import com.defecttracker.modules.access.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication & Access", description = "Endpoints for employee authentication, session tokens, password recovery, and effective permissions")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Authenticate user and issue session tokens")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Authentication successful", response));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Renew session with refresh token")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(ApiResponse.success("Session renewed successfully", response));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Initiate forgot password reset flow")
    public ResponseEntity<ApiResponse<String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        String token = authService.forgotPassword(request);
        return ResponseEntity.ok(ApiResponse.success("Password reset instructions have been generated", token));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password with reset token")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success("Password has been reset successfully", null));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current authenticated employee details, full effective permissions, and accessible projects")
    public ResponseEntity<ApiResponse<EffectivePermissionResponse>> getCurrentUser() {
        EffectivePermissionResponse response = authService.getCurrentUserEffectivePermissions();
        return ResponseEntity.ok(ApiResponse.success("Current user profile retrieved", response));
    }
}

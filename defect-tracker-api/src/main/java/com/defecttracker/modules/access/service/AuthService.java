package com.defecttracker.modules.access.service;

import com.defecttracker.common.exception.BusinessRuleException;
import com.defecttracker.common.exception.ResourceNotFoundException;
import com.defecttracker.modules.access.dto.AuthResponse;
import com.defecttracker.modules.access.dto.EffectivePermissionResponse;
import com.defecttracker.modules.access.dto.ForgotPasswordRequest;
import com.defecttracker.modules.access.dto.LoginRequest;
import com.defecttracker.modules.access.dto.RefreshTokenRequest;
import com.defecttracker.modules.access.dto.ResetPasswordRequest;
import com.defecttracker.modules.access.entity.Employee;
import com.defecttracker.modules.access.entity.PasswordResetToken;
import com.defecttracker.modules.access.entity.RefreshToken;
import com.defecttracker.modules.access.repository.EmployeeRepository;
import com.defecttracker.modules.access.repository.PasswordResetTokenRepository;
import com.defecttracker.modules.access.repository.RefreshTokenRepository;
import com.defecttracker.security.JwtTokenProvider;
import com.defecttracker.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final EmployeeRepository employeeRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository resetTokenRepository;
    private final JwtTokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final PermissionService permissionService;

    @Value("${app.jwt.expiration-ms:86400000}")
    private long jwtExpirationMs;

    @Value("${app.jwt.refresh-expiration-ms:604800000}")
    private long refreshExpirationMs;

    @Value("${app.jwt.password-reset-expiration-ms:3600000}")
    private long passwordResetExpirationMs;

    @Transactional
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        Employee employee = employeeRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", userPrincipal.getId()));

        if (!employee.isActive()) {
            throw new BusinessRuleException("Your account is deactivated. Please contact an administrator.");
        }

        String accessToken = tokenProvider.generateAccessToken(userPrincipal);
        String refreshTokenStr = UUID.randomUUID().toString();

        // Persist refresh token
        RefreshToken refreshToken = RefreshToken.builder()
                .employee(employee)
                .token(refreshTokenStr)
                .expiryDate(LocalDateTime.now().plusSeconds(refreshExpirationMs / 1000))
                .revoked(false)
                .build();
        refreshTokenRepository.save(refreshToken);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenStr)
                .tokenType("Bearer")
                .expiresInMs(jwtExpirationMs)
                .id(employee.getId())
                .employeeId(employee.getEmployeeId())
                .email(employee.getEmail())
                .firstName(employee.getFirstName())
                .lastName(employee.getLastName())
                .fullName(employee.getFullName())
                .role(employee.getRole() != null ? employee.getRole().getName() : null)
                .designation(employee.getDesignation() != null ? employee.getDesignation().getName() : null)
                .admin(employee.getRole() != null && employee.getRole().isAdmin())
                .effectivePermissions(userPrincipal.getEffectivePermissions())
                .build();
    }

    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken storedToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new BadCredentialsException("Invalid refresh token"));

        if (storedToken.isRevoked() || storedToken.isExpired()) {
            throw new BadCredentialsException("Refresh token is expired or revoked. Please log in again.");
        }

        Employee employee = storedToken.getEmployee();
        if (!employee.isActive()) {
            throw new BusinessRuleException("Employee account is deactivated.");
        }

        UserPrincipal userPrincipal = UserPrincipal.create(employee, null);
        String newAccessToken = tokenProvider.generateAccessToken(userPrincipal);

        // Rotate refresh token
        storedToken.setRevoked(true);
        refreshTokenRepository.save(storedToken);

        String newRefreshTokenStr = UUID.randomUUID().toString();
        RefreshToken newRefreshToken = RefreshToken.builder()
                .employee(employee)
                .token(newRefreshTokenStr)
                .expiryDate(LocalDateTime.now().plusSeconds(refreshExpirationMs / 1000))
                .revoked(false)
                .build();
        refreshTokenRepository.save(newRefreshToken);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshTokenStr)
                .tokenType("Bearer")
                .expiresInMs(jwtExpirationMs)
                .id(employee.getId())
                .employeeId(employee.getEmployeeId())
                .email(employee.getEmail())
                .firstName(employee.getFirstName())
                .lastName(employee.getLastName())
                .fullName(employee.getFullName())
                .role(employee.getRole() != null ? employee.getRole().getName() : null)
                .designation(employee.getDesignation() != null ? employee.getDesignation().getName() : null)
                .admin(employee.getRole() != null && employee.getRole().isAdmin())
                .effectivePermissions(permissionService.getEffectivePermissions(employee))
                .build();
    }

    @Transactional
    public String forgotPassword(ForgotPasswordRequest request) {
        Employee employee = employeeRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "email", request.getEmail()));

        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .employee(employee)
                .token(token)
                .expiryDate(LocalDateTime.now().plusSeconds(passwordResetExpirationMs / 1000))
                .used(false)
                .build();
        resetTokenRepository.save(resetToken);

        log.info("Password reset token generated for employee {}: {}", employee.getEmail(), token);
        return token;
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken = resetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new BusinessRuleException("Invalid or expired password reset token"));

        if (resetToken.isUsed() || resetToken.isExpired()) {
            throw new BusinessRuleException("Password reset token has already been used or has expired");
        }

        Employee employee = resetToken.getEmployee();
        employee.setPassword(passwordEncoder.encode(request.getNewPassword()));
        employeeRepository.save(employee);

        resetToken.setUsed(true);
        resetTokenRepository.save(resetToken);

        log.info("Password successfully reset for employee {}", employee.getEmail());
    }

    @Transactional(readOnly = true)
    public EffectivePermissionResponse getCurrentUserEffectivePermissions() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            throw new BadCredentialsException("No authenticated user found in security context");
        }
        return permissionService.getEffectivePermissionResponse(principal.getId());
    }
}

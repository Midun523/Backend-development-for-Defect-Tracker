package com.defecttracker.service.impl;

import com.defecttracker.dto.request.*;
import com.defecttracker.dto.response.LoginResponse;
import com.defecttracker.dto.response.UserProfileResponse;
import com.defecttracker.entity.*;
import com.defecttracker.exception.BadRequestException;
import com.defecttracker.exception.ResourceNotFoundException;
import com.defecttracker.exception.UnauthorizedException;
import com.defecttracker.repository.*;
import com.defecttracker.security.CustomUserDetailsService;
import com.defecttracker.security.JwtTokenProvider;
import com.defecttracker.security.UserPrincipal;
import com.defecttracker.service.AuthService;
import com.defecttracker.service.NotificationService;
import com.defecttracker.util.PasswordPolicy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final ProjectRepository projectRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final JwtTokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final CustomUserDetailsService userDetailsService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final NotificationService notificationService;

    @Value("${app.jwt.refresh-expiration-ms:604800000}")
    private long refreshExpirationMs;

    @Value("${app.jwt.password-reset-expiration-ms:600000}")
    private long passwordResetExpirationMs;

    @Value("${app.jwt.refresh-reuse-grace-seconds:30}")
    private long refreshReuseGraceSeconds;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    private String hashToken(String token) {
        if (token == null) {
            return "";
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        String identifier = request.getEffectiveIdentifier();
        if (identifier.isEmpty()) {
            throw new BadRequestException("Username or email is required");
        }

        User user = userRepository.findByEmailOrUserId(identifier, identifier)
                .orElseThrow(() -> new UnauthorizedException("Invalid username or password"));

        if (!"ACTIVE".equalsIgnoreCase(user.getUserStatus())) {
            throw new UnauthorizedException("Invalid username or password");
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getEmail(), request.getPassword())
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (Exception ex) {
            throw new UnauthorizedException("Invalid username or password");
        }

        UserPrincipal userPrincipal = (UserPrincipal) userDetailsService.loadUserByUsername(user.getEmail());
        String token = tokenProvider.generateToken(userPrincipal);
        String refreshToken = tokenProvider.generateRefreshToken(user.getEmail());

        UUID familyId = UUID.randomUUID();
        RefreshToken rtEntity = RefreshToken.builder()
                .user(user)
                .tokenHash(hashToken(refreshToken))
                .familyId(familyId)
                .expiresAt(Instant.now().plusMillis(refreshExpirationMs))
                .build();
        refreshTokenRepository.save(rtEntity);

        Optional<Employee> employeeOpt = employeeRepository.findByUserId(user.getId());
        Long employeeId = employeeOpt.map(Employee::getId).orElse(user.getId());

        List<String> roles = user.getRoles().stream().map(Role::getRoleName).collect(Collectors.toList());
        List<String> permissions = new ArrayList<>(userPrincipal.getPermissions());

        List<Project> accessibleProjects = getAccessibleProjectsForUser(user, employeeId);
        List<String> projectIds = accessibleProjects.stream().map(p -> String.valueOf(p.getId())).collect(Collectors.toList());

        return LoginResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .type("Bearer")
                .userId(user.getId())
                .employeeId(employeeId)
                .companyStaffId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .userType(user.getUserType())
                .roles(roles)
                .globalPermissions(permissions)
                .projectAccessList(projectIds)
                .mustChangePassword(Boolean.TRUE.equals(user.getMustChangePassword()))
                .build();
    }

    @Override
    @Transactional(noRollbackFor = UnauthorizedException.class)
    public LoginResponse refreshToken(RefreshTokenRequest request) {
        String presentedToken = request.getRefreshToken();
        if (presentedToken == null || !tokenProvider.validateToken(presentedToken) || !tokenProvider.isRefreshToken(presentedToken)) {
            throw new UnauthorizedException("Invalid or expired refresh token");
        }

        String tokenHash = hashToken(presentedToken);
        RefreshToken storedToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new UnauthorizedException("Invalid or expired refresh token"));

        if (storedToken.getExpiresAt().isBefore(Instant.now())) {
            throw new UnauthorizedException("Invalid or expired refresh token");
        }

        User user = storedToken.getUser();

        if (storedToken.getRevokedAt() != null) {
            String reason = storedToken.getRevokedReason();
            if ("ROTATED".equals(reason)) {
                Instant revokedAt = storedToken.getRevokedAt();
                Instant graceCutoff = revokedAt.plusSeconds(refreshReuseGraceSeconds);
                if (Instant.now().isBefore(graceCutoff)) {
                    // Within grace window: reject request, but do not revoke family
                    throw new UnauthorizedException("Invalid or expired refresh token");
                } else {
                    // Beyond grace window: token reuse detected! Revoke ONLY its own family_id
                    refreshTokenRepository.revokeAllByFamilyId(storedToken.getFamilyId(), "FAMILY_REVOKED", Instant.now());
                    throw new UnauthorizedException("Invalid or expired refresh token");
                }
            } else {
                // Revoked by LOGOUT, PASSWORD_RESET, or FAMILY_REVOKED
                // Presented again just returns 401 without revoking other sessions
                throw new UnauthorizedException("Invalid or expired refresh token");
            }
        }

        // Active token: rotate it
        storedToken.setRevokedAt(Instant.now());
        storedToken.setRevokedReason("ROTATED");
        refreshTokenRepository.save(storedToken);

        UserPrincipal userPrincipal = (UserPrincipal) userDetailsService.loadUserByUsername(user.getEmail());
        String newToken = tokenProvider.generateToken(userPrincipal);
        String newRefreshToken = tokenProvider.generateRefreshToken(user.getEmail());

        RefreshToken newRtEntity = RefreshToken.builder()
                .user(user)
                .tokenHash(hashToken(newRefreshToken))
                .familyId(storedToken.getFamilyId())
                .expiresAt(Instant.now().plusMillis(refreshExpirationMs))
                .build();
        refreshTokenRepository.save(newRtEntity);

        Optional<Employee> employeeOpt = employeeRepository.findByUserId(user.getId());
        Long employeeId = employeeOpt.map(Employee::getId).orElse(user.getId());

        List<String> roles = user.getRoles().stream().map(Role::getRoleName).collect(Collectors.toList());
        List<Project> accessibleProjects = getAccessibleProjectsForUser(user, employeeId);
        List<String> projectIds = accessibleProjects.stream().map(p -> String.valueOf(p.getId())).collect(Collectors.toList());

        return LoginResponse.builder()
                .token(newToken)
                .refreshToken(newRefreshToken)
                .type("Bearer")
                .userId(user.getId())
                .employeeId(employeeId)
                .companyStaffId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .userType(user.getUserType())
                .roles(roles)
                .globalPermissions(new ArrayList<>(userPrincipal.getPermissions()))
                .projectAccessList(projectIds)
                .build();
    }

    @Override
    @Transactional
    public void logout(String userEmail, RefreshTokenRequest request) {
        if (request != null && request.getRefreshToken() != null && !request.getRefreshToken().isBlank()) {
            String tokenHash = hashToken(request.getRefreshToken());
            refreshTokenRepository.findByTokenHash(tokenHash).ifPresent(rt -> {
                rt.setRevokedAt(Instant.now());
                rt.setRevokedReason("LOGOUT");
                refreshTokenRepository.save(rt);
            });
        } else if (userEmail != null) {
            userRepository.findByEmail(userEmail).ifPresent(user -> {
                refreshTokenRepository.revokeAllByUserId(user.getId(), "LOGOUT", Instant.now());
            });
        }
    }

    @Override
    @Transactional
    public void changePassword(String userEmail, ChangePasswordRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userEmail));

        if (request.getCurrentPassword() == null || request.getCurrentPassword().isBlank()) {
            throw new BadRequestException("Current password is required");
        }

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password does not match");
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new BadRequestException("New password must differ from current password");
        }

        if (request.getConfirmPassword() != null && !request.getConfirmPassword().isBlank()) {
            if (!request.getNewPassword().equals(request.getConfirmPassword())) {
                throw new BadRequestException("Confirm password does not match new password");
            }
        }

        PasswordPolicy.validate(request.getNewPassword());

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        if (Boolean.TRUE.equals(user.getMustChangePassword())) {
            user.setMustChangePassword(false);
        }
        userRepository.save(user);

        notificationService.notify("PASSWORD_CHANGED", user.getEmail(), Map.of("email", user.getEmail()));
    }

    @Override
    @Transactional
    public void forgetPassword(ForgetPasswordRequest request) {
        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            byte[] randomBytes = new byte[32]; // 256 bits
            new SecureRandom().nextBytes(randomBytes);
            String rawToken = HexFormat.of().formatHex(randomBytes);
            String tokenHash = hashToken(rawToken);

            PasswordResetToken resetToken = PasswordResetToken.builder()
                    .user(user)
                    .tokenHash(tokenHash)
                    .expiresAt(Instant.now().plusMillis(passwordResetExpirationMs))
                    .build();
            passwordResetTokenRepository.save(resetToken);

            String baseUrl = (frontendUrl != null && !frontendUrl.isBlank()) ? frontendUrl.replaceAll("/+$", "") : "http://localhost:5173";
            String resetLink = baseUrl + "/reset-password?token=" + rawToken;

            String fullName = (user.getFirstName() != null ? user.getFirstName() : "") +
                    (user.getLastName() != null && !user.getLastName().isBlank() ? " " + user.getLastName() : "");
            if (fullName.isBlank()) {
                fullName = user.getEmail();
            }

            Map<String, String> vars = new HashMap<>();
            vars.put("name", fullName);
            vars.put("token", rawToken);
            vars.put("link", resetLink);

            notificationService.notify("PASSWORD_RESET", user.getEmail(), vars);
        }
        // Always return neutrally without leaking whether email exists (FR-AUTH-10)
    }

    @Override
    @Transactional(readOnly = true)
    public boolean validateResetToken(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        String tokenHash = hashToken(token);
        Optional<PasswordResetToken> tokenOpt = passwordResetTokenRepository.findByTokenHash(tokenHash);
        if (tokenOpt.isEmpty()) {
            return false;
        }
        PasswordResetToken resetToken = tokenOpt.get();
        return resetToken.getUsedAt() == null && resetToken.getExpiresAt().isAfter(Instant.now());
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        if (request.getToken() == null || request.getToken().isBlank()) {
            throw new BadRequestException("Reset token is required");
        }
        String tokenHash = hashToken(request.getToken());
        PasswordResetToken resetToken = passwordResetTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new BadRequestException("Invalid or expired reset token"));

        if (resetToken.getUsedAt() != null || resetToken.getExpiresAt().isBefore(Instant.now())) {
            throw new BadRequestException("Invalid or expired reset token");
        }

        PasswordPolicy.validate(request.getNewPassword());

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        if (Boolean.TRUE.equals(user.getMustChangePassword())) {
            user.setMustChangePassword(false);
        }
        userRepository.save(user);

        resetToken.setUsedAt(Instant.now());
        passwordResetTokenRepository.save(resetToken);

        // Revoke all refresh tokens for this user with reason PASSWORD_RESET
        refreshTokenRepository.revokeAllByUserId(user.getId(), "PASSWORD_RESET", Instant.now());
    }

    @Override
    public UserProfileResponse getCurrentUserProfile(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userEmail));

        UserPrincipal principal = (UserPrincipal) userDetailsService.loadUserByUsername(userEmail);
        Optional<Employee> empOpt = employeeRepository.findByUserId(user.getId());

        List<Project> accessible = getAccessibleProjectsForUser(user, empOpt.map(Employee::getId).orElse(user.getId()));
        List<String> projectIds = accessible.stream().map(p -> String.valueOf(p.getId())).collect(Collectors.toList());

        return UserProfileResponse.builder()
                .id(user.getId())
                .userId(user.getUserId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .gender(user.getGender())
                .userStatus(user.getUserStatus())
                .userType(user.getUserType())
                .designationId(user.getDesignation() != null ? user.getDesignation().getId() : null)
                .designationName(user.getDesignation() != null ? user.getDesignation().getDesignationName() : null)
                .roles(user.getRoles().stream().map(Role::getRoleName).collect(Collectors.toList()))
                .permissions(new ArrayList<>(principal.getPermissions()))
                .projectAccessList(projectIds)
                .build();
    }

    @Override
    public List<String> getCurrentUserPermissions(String userEmail) {
        UserPrincipal principal = (UserPrincipal) userDetailsService.loadUserByUsername(userEmail);
        return new ArrayList<>(principal.getPermissions());
    }

    @Override
    public List<Project> getCurrentUserProjects(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userEmail));
        Optional<Employee> empOpt = employeeRepository.findByUserId(user.getId());
        return getAccessibleProjectsForUser(user, empOpt.map(Employee::getId).orElse(user.getId()));
    }

    @Override
    public List<String> getCurrentUserProjectPermissions(String userEmail, Long projectId) {
        return getCurrentUserPermissions(userEmail);
    }

    private List<Project> getAccessibleProjectsForUser(User user, Long employeeId) {
        boolean isSuperAdmin = user.getRoles().stream()
                .anyMatch(r -> "Super Admin".equalsIgnoreCase(r.getRoleName()) || "admin".equalsIgnoreCase(r.getRoleName()));
        if (isSuperAdmin) {
            return projectRepository.findAll();
        }
        return projectRepository.findProjectsByAllocatedEmployeeId(employeeId);
    }
}

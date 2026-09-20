package com.defecttracker.service.impl;

import com.defecttracker.dto.request.*;
import com.defecttracker.dto.response.LoginResponse;
import com.defecttracker.dto.response.UserProfileResponse;
import com.defecttracker.entity.Employee;
import com.defecttracker.entity.Project;
import com.defecttracker.entity.Role;
import com.defecttracker.entity.RolePermission;
import com.defecttracker.entity.User;
import com.defecttracker.exception.BadRequestException;
import com.defecttracker.exception.ResourceNotFoundException;
import com.defecttracker.exception.UnauthorizedException;
import com.defecttracker.repository.*;
import com.defecttracker.security.CustomUserDetailsService;
import com.defecttracker.security.JwtTokenProvider;
import com.defecttracker.security.UserPrincipal;
import com.defecttracker.service.AuthService;
import com.defecttracker.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
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
    private final EmailService emailService;

    @Override
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
    public LoginResponse refreshToken(RefreshTokenRequest request) {
        if (!tokenProvider.validateToken(request.getRefreshToken())) {
            throw new UnauthorizedException("Invalid or expired refresh token");
        }

        String email = tokenProvider.getEmailFromToken(request.getRefreshToken());
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        UserPrincipal userPrincipal = (UserPrincipal) userDetailsService.loadUserByUsername(user.getEmail());
        String newToken = tokenProvider.generateToken(userPrincipal);
        String newRefreshToken = tokenProvider.generateRefreshToken(user.getEmail());

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
    public void changePassword(String userEmail, ChangePasswordRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userEmail));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password does not match");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    public void forgetPassword(ForgetPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", request.getEmail()));

        String resetToken = tokenProvider.generateRefreshToken(user.getEmail());
        log.info("Credential reset requested for user: {}", user.getEmail());

        Map<String, String> vars = new HashMap<>();
        vars.put("name", user.getFirstName());
        vars.put("token", resetToken);
        emailService.sendTemplatedEmail(user.getEmail(), "PASSWORD_RESET", vars);
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        if (!tokenProvider.validateToken(request.getToken())) {
            throw new BadRequestException("Invalid or expired reset token");
        }

        String email = tokenProvider.getEmailFromToken(request.getToken());
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
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

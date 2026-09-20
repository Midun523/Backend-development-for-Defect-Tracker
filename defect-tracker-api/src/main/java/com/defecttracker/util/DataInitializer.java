package com.defecttracker.util;

import com.defecttracker.entity.*;
import com.defecttracker.entity.Module;
import com.defecttracker.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import jakarta.annotation.PostConstruct;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final DesignationRepository designationRepository;
    private final PriorityRepository priorityRepository;
    private final SeverityRepository severityRepository;
    private final DefectTypeRepository defectTypeRepository;
    private final ReleaseTypeRepository releaseTypeRepository;
    private final StatusTypeRepository statusTypeRepository;
    private final StatusTransitionRepository statusTransitionRepository;
    private final EmployeeRepository employeeRepository;
    private final ProjectRepository projectRepository;
    private final ModuleRepository moduleRepository;
    private final SubModuleRepository subModuleRepository;
    private final TestCaseRepository testCaseRepository;
    private final ReleaseRepository releaseRepository;
    private final DefectRepository defectRepository;
    private final EmailConfigRepository emailConfigRepository;
    private final EmailTemplateRepository emailTemplateRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.admin.email:admin@defecttracker.com}")
    private String adminEmail;
    @Value("${app.seed.admin.username:US0001}")
    private String adminUsername;
    @Value("${app.seed.admin.password}")
    private String adminPassword;

    private static final Set<String> WEAK_PASSWORDS = Set.of(
            "admin123", "password", "123456", "12345678", "123456789",
            "admin", "administrator", "root", "toor", "qwerty", "admin@123", "password123"
    );

    @PostConstruct
    public void validateAdminPassword() {
        if (adminPassword == null || adminPassword.length() < 12) {
            throw new IllegalArgumentException("SEED_ADMIN_PASSWORD must be at least 12 characters long");
        }
        if (WEAK_PASSWORDS.contains(adminPassword.trim().toLowerCase())) {
            throw new IllegalArgumentException("SEED_ADMIN_PASSWORD cannot be a known weak value: " + adminPassword);
        }
    }

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Checking database initialization...");
        seedPermissions();
        seedSuperAdminRole();
        seedSuperAdmin();
        seedEmailConfigAndTemplates();
        log.info("Database initialization completed successfully (Clean state - no sample/predefined entities)!");
    }

    private void seedPermissions() {
        if (permissionRepository.count() > 0) return;

        List<String[]> perms = List.of(
            new String[]{"DESIGNATION_CREATE", "Create Designation"},
            new String[]{"DESIGNATION_UPDATE", "Update Designation"},
            new String[]{"DESIGNATION_READ", "View Designation"},
            new String[]{"DESIGNATION_DELETE", "Delete Designation"},
            new String[]{"ROLE_CREATE", "Create Role"},
            new String[]{"ROLE_UPDATE", "Update Role"},
            new String[]{"ROLE_READ", "View Role"},
            new String[]{"ROLE_DELETE", "Delete Role"},
            new String[]{"PERMISSION_READ", "View Permissions"},
            new String[]{"ROLE_PERMISSION_ASSIGN", "Assign Permissions to Role"},
            new String[]{"ROLE_PERMISSION_READ", "View Role Permissions"},
            new String[]{"DEFECT_TYPE_CREATE", "Create Defect Type"},
            new String[]{"DEFECT_TYPE_UPDATE", "Update Defect Type"},
            new String[]{"DEFECT_TYPE_READ", "View Defect Type"},
            new String[]{"DEFECT_TYPE_DELETE", "Delete Defect Type"},
            new String[]{"RELEASE_TYPE_CREATE", "Create Release Type"},
            new String[]{"RELEASE_TYPE_UPDATE", "Update Release Type"},
            new String[]{"RELEASE_TYPE_READ", "View Release Type"},
            new String[]{"RELEASE_TYPE_DELETE", "Delete Release Type"},
            new String[]{"SEVERITY_CREATE", "Create Severity"},
            new String[]{"SEVERITY_UPDATE", "Update Severity"},
            new String[]{"SEVERITY_READ", "View Severity"},
            new String[]{"SEVERITY_DELETE", "Delete Severity"},
            new String[]{"PRIORITY_CREATE", "Create Priority"},
            new String[]{"PRIORITY_UPDATE", "Update Priority"},
            new String[]{"PRIORITY_READ", "View Priority"},
            new String[]{"PRIORITY_DELETE", "Delete Priority"},
            new String[]{"STATUS_TYPE_CREATE", "Create Status Type"},
            new String[]{"STATUS_TYPE_UPDATE", "Update Status Type"},
            new String[]{"STATUS_TYPE_READ", "View Status Type"},
            new String[]{"STATUS_TYPE_DELETE", "Delete Status Type"},
            new String[]{"WORKFLOW_CREATE", "Create Workflow"},
            new String[]{"WORKFLOW_READ", "View Workflow"},
            new String[]{"EMPLOYEE_CREATE", "Create Employee"},
            new String[]{"EMPLOYEE_UPDATE", "Update Employee"},
            new String[]{"EMPLOYEE_READ", "View Employee"},
            new String[]{"EMPLOYEE_DELETE", "Delete Employee"},
            new String[]{"BENCH_READ", "View Bench"},
            new String[]{"PROJECT_CREATE", "Create Project"},
            new String[]{"PROJECT_UPDATE", "Update Project"},
            new String[]{"PROJECT_READ", "View Project"},
            new String[]{"PROJECT_DELETE", "Delete Project"},
            new String[]{"MODULE_CREATE", "Create Module"},
            new String[]{"MODULE_UPDATE", "Update Module"},
            new String[]{"MODULE_READ", "View Module"},
            new String[]{"MODULE_DELETE", "Delete Module"},
            new String[]{"TEST_CASE_CREATE", "Create Test Case"},
            new String[]{"TEST_CASE_UPDATE", "Update Test Case"},
            new String[]{"TEST_CASE_READ", "View Test Case"},
            new String[]{"TEST_CASE_DELETE", "Delete Test Case"},
            new String[]{"RELEASE_CREATE", "Create Release"},
            new String[]{"RELEASE_UPDATE", "Update Release"},
            new String[]{"RELEASE_READ", "View Release"},
            new String[]{"RELEASE_DELETE", "Delete Release"},
            new String[]{"DEFECT_CREATE", "Create Defect"},
            new String[]{"DEFECT_UPDATE", "Update Defect"},
            new String[]{"DEFECT_READ", "View Defect"},
            new String[]{"DEFECT_DELETE", "Delete Defect"},
            new String[]{"DEFECT_ASSIGN_DEVELOPER", "Assign Developer to Defect"},
            new String[]{"DEFECT_STATUS_CHANGE", "Change Defect Status"},
            new String[]{"DEFECT_COMMENT_CREATE", "Add Defect Comment"},
            new String[]{"DEFECT_COMMENT_READ", "View Defect Comments"},
            new String[]{"EMAIL_CONFIG_READ", "View Email Configurations"},
            new String[]{"EMAIL_CONFIG_CREATE", "Create Email Configuration"},
            new String[]{"EMAIL_CONFIG_UPDATE", "Update Email Configuration"},
            new String[]{"EMAIL_CONFIG_DELETE", "Delete Email Configuration"},
            new String[]{"ALL_PERMISSIONS", "Full System Administrator Access"}
        );

        for (String[] p : perms) {
            permissionRepository.save(Permission.builder()
                    .action(p[0])
                    .description(p[1])
                    .build());
        }
    }

    private void seedSuperAdminRole() {
        if (roleRepository.count() > 0) return;

        Role superAdmin = roleRepository.save(Role.builder()
                .roleName("Super Admin")
                .description("Full system access and configurations")
                .build());

        // Assign all permissions to Super Admin
        List<Permission> allPermissions = permissionRepository.findAll();
        for (Permission p : allPermissions) {
            rolePermissionRepository.save(RolePermission.builder().role(superAdmin).permission(p).build());
        }
    }

    private void seedSuperAdmin() {
        if (userRepository.findByEmail(adminEmail).isPresent()) return;

        Role superAdminRole = roleRepository.findByRoleName("Super Admin")
                .orElseGet(() -> roleRepository.save(Role.builder().roleName("Super Admin").description("Super Administrator").build()));

        Designation pmDesignation = designationRepository.findByDesignationName("Project Manager")
                .orElse(null);

        User adminUser = User.builder()
                .userId(adminUsername)
                .firstName("Super")
                .lastName("Admin")
                .email(adminEmail)
                .password(passwordEncoder.encode(adminPassword))
                .phone("+1 555-0199")
                .gender("Male")
                .userStatus("ACTIVE")
                .userType("CompanyStaff")
                .designation(pmDesignation)
                .roles(Set.of(superAdminRole))
                .mustChangePassword(true)
                .build();
        User savedAdmin = userRepository.save(adminUser);

        Employee adminEmp = Employee.builder()
                .user(savedAdmin)
                .firstName("Super")
                .lastName("Admin")
                .email(adminEmail)
                .phone("+1 555-0199")
                .gender("Male")
                .designation(pmDesignation)
                .experience(8.5)
                .joinedDate(LocalDate.now().minusYears(3))
                .skills("Spring Boot,PostgreSQL,React,TypeScript,System Architecture")
                .availability(100)
                .status("active")
                .department("Engineering")
                .manager("Executive Board")
                .build();
        employeeRepository.save(adminEmp);

        log.info("Initialized Super Admin: {}", adminEmail);
    }

    private void seedEmailConfigAndTemplates() {
        if (emailConfigRepository.count() == 0) {
            emailConfigRepository.save(EmailConfig.builder()
                    .name("Default Local SMTP Server")
                    .smtpHost("smtp.gmail.com")
                    .smtpPort(587)
                    .username("notifications@defecttracker.com")
                    .password("app-password-here")
                    .fromEmail("notifications@defecttracker.com")
                    .fromName("Defect Tracker Pro")
                    .isActive(true)
                    .isDefault(true)
                    .useTls(true)
                    .useSsl(false)
                    .build());
        }

        if (emailTemplateRepository.count() == 0) {
            emailTemplateRepository.save(EmailTemplate.builder()
                    .templateName("DEFECT_ASSIGNED")
                    .subject("New Defect Assigned: [{defectId}] in {projectName}")
                    .bodyContent("<p>Hello <b>{name}</b>,</p><p>You have been assigned to defect <b>{defectId}</b> in project <b>{projectName}</b>.</p><p>Please log in to the Defect Tracker to review details and begin investigation.</p>")
                    .eventTrigger("DEFECT_ASSIGNED")
                    .variables("name,defectId,projectName")
                    .isActive(true)
                    .isDefault(true)
                    .build());

            emailTemplateRepository.save(EmailTemplate.builder()
                    .templateName("PASSWORD_RESET")
                    .subject("Password Reset Request - Defect Tracker Pro")
                    .bodyContent("<p>Hello <b>{name}</b>,</p><p>Your password reset code is: <b>{token}</b>.</p><p>If you did not request this, please ignore this email.</p>")
                    .eventTrigger("PASSWORD_RESET")
                    .variables("name,token")
                    .isActive(true)
                    .isDefault(true)
                    .build());
        }
    }


}

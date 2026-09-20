package com.defecttracker.security;

import com.defecttracker.entity.*;
import com.defecttracker.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service("access")
@RequiredArgsConstructor
public class AccessSecurityService {

    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final ProjectRepository projectRepository;
    private final ProjectAllocationRepository projectAllocationRepository;
    private final ModuleRepository moduleRepository;
    private final SubModuleRepository subModuleRepository;
    private final ReleaseRepository releaseRepository;
    private final DefectRepository defectRepository;
    private final TestCaseRepository testCaseRepository;

    /**
     * General authority check without project context.
     * Bypassed by SUPER_ADMIN or ALL_PERMISSIONS.
     */
    public boolean has(String permissionCode) {
        return hasProjectAccess(permissionCode, null);
    }

    /**
     * Overload taking (permissionCode, projectId).
     */
    public boolean has(String permissionCode, Long projectId) {
        return hasProjectAccess(permissionCode, projectId);
    }

    /**
     * Checks authority and verifies the current user is allocated to the given projectId.
     * Bypassed by SUPER_ADMIN or ALL_PERMISSIONS.
     */
    public boolean hasProjectAccess(String permissionCode, Long projectId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return false;
        }

        // 1. Check Super Admin / ALL_PERMISSIONS bypass
        if (isSuperAdmin(auth)) {
            return true;
        }

        // 2. Check authority for the permissionCode
        if (permissionCode != null && !hasAuthority(auth, permissionCode)) {
            return false;
        }

        // If no project context is required, permission check is sufficient
        if (projectId == null) {
            return true;
        }

        // 3. Verify user is allocated to projectId
        return isUserAllocatedToProject(auth.getName(), projectId);
    }

    /**
     * Resolves project from moduleId and evaluates access.
     */
    public boolean hasModuleAccess(String permissionCode, Long moduleId) {
        if (isSuperAdmin(SecurityContextHolder.getContext().getAuthentication())) {
            return true;
        }
        if (moduleId == null) {
            return has(permissionCode);
        }
        Long projectId = moduleRepository.findById(moduleId)
                .map(m -> m.getProject() != null ? m.getProject().getId() : null)
                .orElse(null);
        return hasProjectAccess(permissionCode, projectId);
    }

    /**
     * Resolves project from subModuleId and evaluates access.
     */
    public boolean hasSubModuleAccess(String permissionCode, Long subModuleId) {
        if (isSuperAdmin(SecurityContextHolder.getContext().getAuthentication())) {
            return true;
        }
        if (subModuleId == null) {
            return has(permissionCode);
        }
        Long projectId = subModuleRepository.findById(subModuleId)
                .map(sm -> sm.getModule() != null && sm.getModule().getProject() != null
                        ? sm.getModule().getProject().getId()
                        : null)
                .orElse(null);
        return hasProjectAccess(permissionCode, projectId);
    }

    /**
     * Resolves project from releaseId and evaluates access.
     */
    public boolean hasReleaseAccess(String permissionCode, Long releaseId) {
        if (isSuperAdmin(SecurityContextHolder.getContext().getAuthentication())) {
            return true;
        }
        if (releaseId == null) {
            return has(permissionCode);
        }
        Long projectId = releaseRepository.findById(releaseId)
                .map(r -> r.getProject() != null ? r.getProject().getId() : null)
                .orElse(null);
        return hasProjectAccess(permissionCode, projectId);
    }

    /**
     * Resolves project from defectId and evaluates access.
     */
    public boolean hasDefectAccess(String permissionCode, Long defectId) {
        if (isSuperAdmin(SecurityContextHolder.getContext().getAuthentication())) {
            return true;
        }
        if (defectId == null) {
            return has(permissionCode);
        }
        Long projectId = defectRepository.findById(defectId)
                .map(d -> d.getProject() != null ? d.getProject().getId() : null)
                .orElse(null);
        return hasProjectAccess(permissionCode, projectId);
    }

    /**
     * Resolves project from testCaseId and evaluates access.
     */
    public boolean hasTestCaseAccess(String permissionCode, Long testCaseId) {
        if (isSuperAdmin(SecurityContextHolder.getContext().getAuthentication())) {
            return true;
        }
        if (testCaseId == null) {
            return has(permissionCode);
        }
        Long projectId = testCaseRepository.findById(testCaseId)
                .map(tc -> tc.getSubModule() != null && tc.getSubModule().getModule() != null && tc.getSubModule().getModule().getProject() != null
                        ? tc.getSubModule().getModule().getProject().getId()
                        : null)
                .orElse(null);
        return hasProjectAccess(permissionCode, projectId);
    }

    /**
     * Resolves project from allocationId and evaluates access.
     */
    public boolean hasAllocationAccess(String permissionCode, Long allocationId) {
        if (isSuperAdmin(SecurityContextHolder.getContext().getAuthentication())) {
            return true;
        }
        if (allocationId == null) {
            return has(permissionCode);
        }
        Long projectId = projectAllocationRepository.findById(allocationId)
                .map(a -> a.getProject() != null ? a.getProject().getId() : null)
                .orElse(null);
        return hasProjectAccess(permissionCode, projectId);
    }

    private boolean isSuperAdmin(Authentication auth) {
        if (auth == null) return false;
        for (GrantedAuthority ga : auth.getAuthorities()) {
            String a = ga.getAuthority();
            if ("ALL_PERMISSIONS".equalsIgnoreCase(a)
                    || "ROLE_SUPER_ADMIN".equalsIgnoreCase(a)
                    || "ROLE_ADMIN".equalsIgnoreCase(a)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasAuthority(Authentication auth, String permissionCode) {
        String altColon = permissionCode.replace('_', ':');
        String altUnderscore = permissionCode.replace(':', '_');
        for (GrantedAuthority ga : auth.getAuthorities()) {
            String a = ga.getAuthority();
            if (a.equalsIgnoreCase(permissionCode)
                    || a.equalsIgnoreCase(altColon)
                    || a.equalsIgnoreCase(altUnderscore)) {
                return true;
            }
        }
        return false;
    }

    private boolean isUserAllocatedToProject(String usernameOrEmail, Long projectId) {
        Optional<User> userOpt = userRepository.findByEmailOrUserId(usernameOrEmail, usernameOrEmail);
        if (userOpt.isEmpty()) {
            return false;
        }
        User user = userOpt.get();
        Optional<Employee> empOpt = employeeRepository.findByUserId(user.getId());
        if (empOpt.isEmpty()) {
            return false;
        }
        Long employeeId = empOpt.get().getId();

        // 1. Check if user is the assigned Project Manager
        Optional<Project> projectOpt = projectRepository.findById(projectId);
        if (projectOpt.isPresent()) {
            Project project = projectOpt.get();
            if (project.getManager() != null && project.getManager().getId().equals(employeeId)) {
                return true;
            }
        }

        // 2. Check active project allocation
        return projectAllocationRepository.findByProjectIdAndEmployeeIdAndStatus(projectId, employeeId, "ACTIVE").isPresent();
    }
}

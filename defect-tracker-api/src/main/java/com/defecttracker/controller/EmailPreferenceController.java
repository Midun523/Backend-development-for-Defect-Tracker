package com.defecttracker.controller;

import com.defecttracker.dto.response.ApiResponse;
import com.defecttracker.entity.EmailRolePreference;
import com.defecttracker.entity.EmailTemplate;
import com.defecttracker.entity.EmailUserPreference;
import com.defecttracker.entity.Employee;
import com.defecttracker.entity.Role;
import com.defecttracker.exception.ResourceNotFoundException;
import com.defecttracker.repository.EmailRolePreferenceRepository;
import com.defecttracker.repository.EmailTemplateRepository;
import com.defecttracker.repository.EmailUserPreferenceRepository;
import com.defecttracker.repository.EmployeeRepository;
import com.defecttracker.repository.RoleRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/email-preferences")
@RequiredArgsConstructor
@Tag(name = "Email Notification Preferences", description = "Endpoints matching ERD User Perfernces and Role Based notification configurations")
public class EmailPreferenceController {

    private final EmailUserPreferenceRepository userPrefRepo;
    private final EmailRolePreferenceRepository rolePrefRepo;
    private final EmployeeRepository employeeRepo;
    private final RoleRepository roleRepo;
    private final EmailTemplateRepository emailTemplateRepo;

    @GetMapping("/user/{employeeId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get user email notification preferences")
    public ResponseEntity<ApiResponse<List<EmailUserPreference>>> getUserPreferences(@PathVariable Long employeeId) {
        List<EmailUserPreference> prefs = userPrefRepo.findByEmployeeId(employeeId);
        return ResponseEntity.ok(ApiResponse.success(prefs, "User preferences retrieved"));
    }

    @PostMapping("/user")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Set user email notification preference")
    public ResponseEntity<ApiResponse<EmailUserPreference>> setUserPreference(@jakarta.validation.Valid @RequestBody Map<String, Object> body) {
        Long employeeId = Long.valueOf(body.get("employeeId").toString());
        Long templateId = Long.valueOf(body.get("templateId").toString());
        String status = body.getOrDefault("status", "active").toString();

        Employee emp = employeeRepo.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", employeeId));
        EmailTemplate template = emailTemplateRepo.findById(templateId)
                .orElseThrow(() -> new ResourceNotFoundException("EmailTemplate", "id", templateId));

        EmailUserPreference pref = userPrefRepo.findByEmployeeIdAndEmailTemplateId(employeeId, templateId)
                .orElse(EmailUserPreference.builder().employee(emp).emailTemplate(template).build());
        pref.setStatus(status);

        EmailUserPreference saved = userPrefRepo.save(pref);
        return ResponseEntity.ok(ApiResponse.success(saved, "User email preference saved"));
    }

    @GetMapping("/role/{roleId}")
    @PreAuthorize("@access.has('EMAIL_CONFIG_READ') or @access.has('CONFIG_READ')")
    @Operation(summary = "Get role email notification preferences")
    public ResponseEntity<ApiResponse<List<EmailRolePreference>>> getRolePreferences(@PathVariable Long roleId) {
        List<EmailRolePreference> prefs = rolePrefRepo.findByRoleId(roleId);
        return ResponseEntity.ok(ApiResponse.success(prefs, "Role preferences retrieved"));
    }

    @PostMapping("/role")
    @PreAuthorize("@access.has('EMAIL_CONFIG_UPDATE') or @access.has('CONFIG_UPDATE')")
    @Operation(summary = "Set role email notification preference")
    public ResponseEntity<ApiResponse<EmailRolePreference>> setRolePreference(@jakarta.validation.Valid @RequestBody Map<String, Object> body) {
        Long roleId = Long.valueOf(body.get("roleId").toString());
        Long templateId = Long.valueOf(body.get("templateId").toString());
        String status = body.getOrDefault("status", "active").toString();

        Role role = roleRepo.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", roleId));
        EmailTemplate template = emailTemplateRepo.findById(templateId)
                .orElseThrow(() -> new ResourceNotFoundException("EmailTemplate", "id", templateId));

        EmailRolePreference pref = rolePrefRepo.findByRoleIdAndEmailTemplateId(roleId, templateId)
                .orElse(EmailRolePreference.builder().role(role).emailTemplate(template).build());
        pref.setStatus(status);

        EmailRolePreference saved = rolePrefRepo.save(pref);
        return ResponseEntity.ok(ApiResponse.success(saved, "Role email preference saved"));
    }
}

package com.defecttracker.controller;

import com.defecttracker.dto.response.ApiResponse;
import com.defecttracker.entity.Employee;
import com.defecttracker.entity.PrivilegeTemplate;
import com.defecttracker.entity.Role;
import com.defecttracker.entity.RolePrivilegePreference;
import com.defecttracker.entity.UserPrivilegePreference;
import com.defecttracker.exception.ResourceNotFoundException;
import com.defecttracker.repository.EmployeeRepository;
import com.defecttracker.repository.PrivilegeTemplateRepository;
import com.defecttracker.repository.RolePrivilegePreferenceRepository;
import com.defecttracker.repository.RoleRepository;
import com.defecttracker.repository.UserPrivilegePreferenceRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/privileges")
@RequiredArgsConstructor
@Tag(name = "Privilege & Permission Preferences", description = "Endpoints matching ERD PrivalegiesTemplate, UserPrivilagy Perfernces, and RolePrivilage Perfernces")
public class PrivilegeController {

    private final PrivilegeTemplateRepository templateRepo;
    private final UserPrivilegePreferenceRepository userPrefRepo;
    private final RolePrivilegePreferenceRepository rolePrefRepo;
    private final EmployeeRepository employeeRepo;
    private final RoleRepository roleRepo;

    @GetMapping("/templates")
    @Operation(summary = "Get all privilege templates")
    public ResponseEntity<ApiResponse<List<PrivilegeTemplate>>> getTemplates() {
        return ResponseEntity.ok(ApiResponse.success(templateRepo.findAll(), "Templates retrieved"));
    }

    @PostMapping("/templates")
    @Operation(summary = "Create a privilege template")
    public ResponseEntity<ApiResponse<PrivilegeTemplate>> createTemplate(@RequestBody Map<String, String> body) {
        PrivilegeTemplate template = PrivilegeTemplate.builder()
                .privilegesType(body.get("privilegesType"))
                .subject(body.get("subject"))
                .status(body.getOrDefault("status", "active"))
                .build();
        return ResponseEntity.ok(ApiResponse.created(templateRepo.save(template), "Template created"));
    }

    @GetMapping("/user/{employeeId}")
    @Operation(summary = "Get user privilege preferences")
    public ResponseEntity<ApiResponse<List<UserPrivilegePreference>>> getUserPreferences(@PathVariable Long employeeId) {
        List<UserPrivilegePreference> prefs = userPrefRepo.findByEmployeeId(employeeId);
        return ResponseEntity.ok(ApiResponse.success(prefs, "User privilege preferences retrieved"));
    }

    @PostMapping("/user")
    @Operation(summary = "Set user privilege preference")
    public ResponseEntity<ApiResponse<UserPrivilegePreference>> setUserPreference(@RequestBody Map<String, Object> body) {
        Long employeeId = Long.valueOf(body.get("employeeId").toString());
        Long templateId = Long.valueOf(body.get("templateId").toString());
        String status = body.getOrDefault("status", "active").toString();

        Employee emp = employeeRepo.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", employeeId));
        PrivilegeTemplate template = templateRepo.findById(templateId)
                .orElseThrow(() -> new ResourceNotFoundException("PrivilegeTemplate", "id", templateId));

        UserPrivilegePreference pref = userPrefRepo.findByEmployeeIdAndPrivilegeTemplateId(employeeId, templateId)
                .orElse(UserPrivilegePreference.builder().employee(emp).privilegeTemplate(template).build());
        pref.setStatus(status);

        return ResponseEntity.ok(ApiResponse.success(userPrefRepo.save(pref), "User privilege preference saved"));
    }

    @GetMapping("/role/{roleId}")
    @Operation(summary = "Get role privilege preferences")
    public ResponseEntity<ApiResponse<List<RolePrivilegePreference>>> getRolePreferences(@PathVariable Long roleId) {
        List<RolePrivilegePreference> prefs = rolePrefRepo.findByRoleId(roleId);
        return ResponseEntity.ok(ApiResponse.success(prefs, "Role privilege preferences retrieved"));
    }

    @PostMapping("/role")
    @Operation(summary = "Set role privilege preference")
    public ResponseEntity<ApiResponse<RolePrivilegePreference>> setRolePreference(@RequestBody Map<String, Object> body) {
        Long roleId = Long.valueOf(body.get("roleId").toString());
        Long templateId = Long.valueOf(body.get("templateId").toString());
        String status = body.getOrDefault("status", "active").toString();

        Role role = roleRepo.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", roleId));
        PrivilegeTemplate template = templateRepo.findById(templateId)
                .orElseThrow(() -> new ResourceNotFoundException("PrivilegeTemplate", "id", templateId));

        RolePrivilegePreference pref = rolePrefRepo.findByRoleIdAndPrivilegeTemplateId(roleId, templateId)
                .orElse(RolePrivilegePreference.builder().role(role).privilegeTemplate(template).build());
        pref.setStatus(status);

        return ResponseEntity.ok(ApiResponse.success(rolePrefRepo.save(pref), "Role privilege preference saved"));
    }
}

package com.defecttracker.controller;

import com.defecttracker.dto.request.PrivilegeTemplateRequest;
import com.defecttracker.dto.request.RolePrivilegePreferenceRequest;
import com.defecttracker.dto.request.UserPrivilegePreferenceRequest;
import com.defecttracker.dto.response.*;
import com.defecttracker.entity.Employee;
import com.defecttracker.entity.PrivilegeTemplate;
import com.defecttracker.entity.Role;
import com.defecttracker.entity.RolePrivilegePreference;
import com.defecttracker.entity.UserPrivilegePreference;
import com.defecttracker.exception.ResourceNotFoundException;
import com.defecttracker.mapper.PrivilegeMapper;
import com.defecttracker.repository.EmployeeRepository;
import com.defecttracker.repository.PrivilegeTemplateRepository;
import com.defecttracker.repository.RolePrivilegePreferenceRepository;
import com.defecttracker.repository.RoleRepository;
import com.defecttracker.repository.UserPrivilegePreferenceRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    private final PrivilegeMapper privilegeMapper;

    @GetMapping("/templates")
    @PreAuthorize("@access.has('PERMISSION_READ') or @access.has('CONFIG_READ')")
    @Operation(summary = "Get all privilege templates")
    public ResponseEntity<ApiResponse<List<PrivilegeTemplateResponse>>> getTemplates() {
        return ResponseEntity.ok(ApiResponse.success(privilegeMapper.toTemplateResponseList(templateRepo.findAll()), "Templates retrieved"));
    }

    @PostMapping("/templates")
    @PreAuthorize("@access.has('ROLE_PERMISSION_ASSIGN') or @access.has('CONFIG_UPDATE')")
    @Operation(summary = "Create a privilege template")
    public ResponseEntity<ApiResponse<PrivilegeTemplateResponse>> createTemplate(@jakarta.validation.Valid @RequestBody PrivilegeTemplateRequest request) {
        PrivilegeTemplate template = PrivilegeTemplate.builder()
                .privilegesType(request.getPrivilegesType())
                .subject(request.getSubject())
                .status(request.getStatus() != null ? request.getStatus() : "active")
                .build();
        return ResponseEntity.ok(ApiResponse.created(privilegeMapper.toTemplateResponse(templateRepo.save(template)), "Template created"));
    }

    @GetMapping("/user/{employeeId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get user privilege preferences")
    public ResponseEntity<ApiResponse<List<UserPrivilegePreferenceResponse>>> getUserPreferences(@PathVariable Long employeeId) {
        List<UserPrivilegePreference> prefs = userPrefRepo.findByEmployeeId(employeeId);
        return ResponseEntity.ok(ApiResponse.success(privilegeMapper.toUserPreferenceResponseList(prefs), "User privilege preferences retrieved"));
    }

    @PostMapping("/user")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Set user privilege preference")
    public ResponseEntity<ApiResponse<UserPrivilegePreferenceResponse>> setUserPreference(@jakarta.validation.Valid @RequestBody UserPrivilegePreferenceRequest request) {
        Long employeeId = request.getEmployeeId();
        Long templateId = request.getTemplateId();
        String status = request.getStatus() != null ? request.getStatus() : "active";

        Employee emp = employeeRepo.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", employeeId));
        PrivilegeTemplate template = templateRepo.findById(templateId)
                .orElseThrow(() -> new ResourceNotFoundException("PrivilegeTemplate", "id", templateId));

        UserPrivilegePreference pref = userPrefRepo.findByEmployeeIdAndPrivilegeTemplateId(employeeId, templateId)
                .orElse(UserPrivilegePreference.builder().employee(emp).privilegeTemplate(template).build());
        pref.setStatus(status);

        return ResponseEntity.ok(ApiResponse.success(privilegeMapper.toUserPreferenceResponse(userPrefRepo.save(pref)), "User privilege preference saved"));
    }

    @GetMapping("/role/{roleId}")
    @PreAuthorize("@access.has('ROLE_PERMISSION_READ') or @access.has('ROLE_READ')")
    @Operation(summary = "Get role privilege preferences")
    public ResponseEntity<ApiResponse<List<RolePrivilegePreferenceResponse>>> getRolePreferences(@PathVariable Long roleId) {
        List<RolePrivilegePreference> prefs = rolePrefRepo.findByRoleId(roleId);
        return ResponseEntity.ok(ApiResponse.success(privilegeMapper.toRolePreferenceResponseList(prefs), "Role privilege preferences retrieved"));
    }

    @PostMapping("/role")
    @PreAuthorize("@access.has('ROLE_PERMISSION_ASSIGN') or @access.has('ROLE_UPDATE')")
    @Operation(summary = "Set role privilege preference")
    public ResponseEntity<ApiResponse<RolePrivilegePreferenceResponse>> setRolePreference(@jakarta.validation.Valid @RequestBody RolePrivilegePreferenceRequest request) {
        Long roleId = request.getRoleId();
        Long templateId = request.getTemplateId();
        String status = request.getStatus() != null ? request.getStatus() : "active";

        Role role = roleRepo.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", roleId));
        PrivilegeTemplate template = templateRepo.findById(templateId)
                .orElseThrow(() -> new ResourceNotFoundException("PrivilegeTemplate", "id", templateId));

        RolePrivilegePreference pref = rolePrefRepo.findByRoleIdAndPrivilegeTemplateId(roleId, templateId)
                .orElse(RolePrivilegePreference.builder().role(role).privilegeTemplate(template).build());
        pref.setStatus(status);

        return ResponseEntity.ok(ApiResponse.success(privilegeMapper.toRolePreferenceResponse(rolePrefRepo.save(pref)), "Role privilege preference saved"));
    }
}

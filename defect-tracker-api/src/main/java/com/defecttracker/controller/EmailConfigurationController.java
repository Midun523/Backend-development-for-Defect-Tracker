package com.defecttracker.controller;

import com.defecttracker.dto.request.EmailConfigDTO;
import com.defecttracker.dto.request.EmailTemplateDTO;
import com.defecttracker.dto.request.RoleNotificationUpdateDTO;
import com.defecttracker.dto.request.UserExtraRulesUpdateDTO;
import com.defecttracker.dto.response.ApiResponse;
import com.defecttracker.dto.response.EmailConfigResponse;
import com.defecttracker.dto.response.EmailLogResponse;
import com.defecttracker.dto.response.EmailTemplateResponse;
import com.defecttracker.entity.EmailConfig;
import com.defecttracker.entity.EmailLog;
import com.defecttracker.entity.EmailTemplate;
import com.defecttracker.mapper.EmailConfigMapper;
import com.defecttracker.mapper.EmailLogMapper;
import com.defecttracker.mapper.EmailTemplateMapper;
import com.defecttracker.service.EmailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Email & Notification Configuration", description = "Endpoints for dynamic SMTP configs, email templates, notification matrices, and sent logs")
public class EmailConfigurationController {

    private final EmailService emailService;
    private final EmailConfigMapper emailConfigMapper;
    private final EmailTemplateMapper emailTemplateMapper;
    private final EmailLogMapper emailLogMapper;

    @GetMapping("/email/config")
    @PreAuthorize("@access.has('EMAIL_CONFIG_READ') or @access.has('CONFIG_READ')")
    @Operation(summary = "Get all email SMTP configurations")
    public ResponseEntity<ApiResponse<List<EmailConfigResponse>>> getAllConfigs() {
        List<EmailConfig> configs = emailService.getAllConfigs();
        return ResponseEntity.ok(ApiResponse.success(emailConfigMapper.toResponseList(configs), "Email configs retrieved"));
    }

    @GetMapping("/email/config/{id}")
    @PreAuthorize("@access.has('EMAIL_CONFIG_READ') or @access.has('CONFIG_READ')")
    @Operation(summary = "Get email configuration by ID")
    public ResponseEntity<ApiResponse<EmailConfigResponse>> getConfigById(@PathVariable Long id) {
        EmailConfig config = emailService.getConfigById(id);
        return ResponseEntity.ok(ApiResponse.success(emailConfigMapper.toResponse(config), "Email config found"));
    }

    @PostMapping("/email/config")
    @PreAuthorize("@access.has('EMAIL_CONFIG_CREATE') or @access.has('CONFIG_UPDATE')")
    @Operation(summary = "Create email configuration")
    public ResponseEntity<ApiResponse<EmailConfigResponse>> createConfig(@Valid @RequestBody EmailConfigDTO dto) {
        EmailConfig created = emailService.saveConfig(dto);
        return ResponseEntity.ok(ApiResponse.created(emailConfigMapper.toResponse(created), "Email config created"));
    }

    @PutMapping("/email/config/{id}")
    @PreAuthorize("@access.has('EMAIL_CONFIG_UPDATE') or @access.has('CONFIG_UPDATE')")
    @Operation(summary = "Update email configuration")
    public ResponseEntity<ApiResponse<EmailConfigResponse>> updateConfig(@PathVariable Long id, @Valid @RequestBody EmailConfigDTO dto) {
        dto.setId(id);
        EmailConfig updated = emailService.saveConfig(dto);
        return ResponseEntity.ok(ApiResponse.success(emailConfigMapper.toResponse(updated), "Email config updated"));
    }

    @PatchMapping("/email/config/{id}/enable")
    @PreAuthorize("@access.has('EMAIL_CONFIG_UPDATE') or @access.has('CONFIG_UPDATE')")
    @Operation(summary = "Enable and set default email configuration")
    public ResponseEntity<ApiResponse<EmailConfigResponse>> enableConfig(@PathVariable Long id) {
        EmailConfig enabled = emailService.enableConfig(id);
        return ResponseEntity.ok(ApiResponse.success(emailConfigMapper.toResponse(enabled), "Email config enabled"));
    }

    @DeleteMapping("/email/config/{id}")
    @PreAuthorize("@access.has('EMAIL_CONFIG_DELETE') or @access.has('CONFIG_UPDATE')")
    @Operation(summary = "Delete email configuration")
    public ResponseEntity<ApiResponse<Void>> deleteConfig(@PathVariable Long id) {
        emailService.deleteConfig(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Email config deleted"));
    }

    @GetMapping("/email/template")
    @PreAuthorize("@access.has('EMAIL_CONFIG_READ') or @access.has('CONFIG_READ')")
    @Operation(summary = "Get all email templates")
    public ResponseEntity<ApiResponse<List<EmailTemplateResponse>>> getAllTemplates() {
        List<EmailTemplate> templates = emailService.getAllTemplates();
        return ResponseEntity.ok(ApiResponse.success(emailTemplateMapper.toResponseList(templates), "Email templates retrieved"));
    }

    @GetMapping("/email/template/{id}")
    @PreAuthorize("@access.has('EMAIL_CONFIG_READ') or @access.has('CONFIG_READ')")
    @Operation(summary = "Get email template by ID")
    public ResponseEntity<ApiResponse<EmailTemplateResponse>> getTemplateById(@PathVariable Long id) {
        EmailTemplate template = emailService.getTemplateById(id);
        return ResponseEntity.ok(ApiResponse.success(emailTemplateMapper.toResponse(template), "Email template found"));
    }

    @PostMapping("/email/template")
    @PreAuthorize("@access.has('EMAIL_CONFIG_CREATE') or @access.has('CONFIG_UPDATE')")
    @Operation(summary = "Create email template")
    public ResponseEntity<ApiResponse<EmailTemplateResponse>> createTemplate(@Valid @RequestBody EmailTemplateDTO dto) {
        EmailTemplate created = emailService.saveTemplate(dto);
        return ResponseEntity.ok(ApiResponse.created(emailTemplateMapper.toResponse(created), "Email template created"));
    }

    @PutMapping("/email/template/{id}")
    @PreAuthorize("@access.has('EMAIL_CONFIG_UPDATE') or @access.has('CONFIG_UPDATE')")
    @Operation(summary = "Update email template")
    public ResponseEntity<ApiResponse<EmailTemplateResponse>> updateTemplate(@PathVariable Long id, @Valid @RequestBody EmailTemplateDTO dto) {
        dto.setId(id);
        EmailTemplate updated = emailService.saveTemplate(dto);
        return ResponseEntity.ok(ApiResponse.success(emailTemplateMapper.toResponse(updated), "Email template updated"));
    }

    @PatchMapping("/email/template/{id}/reset")
    @PreAuthorize("@access.has('EMAIL_CONFIG_UPDATE') or @access.has('CONFIG_UPDATE')")
    @Operation(summary = "Reset email template to default body")
    public ResponseEntity<ApiResponse<EmailTemplateResponse>> resetTemplate(@PathVariable Long id) {
        EmailTemplate reset = emailService.resetTemplate(id);
        return ResponseEntity.ok(ApiResponse.success(emailTemplateMapper.toResponse(reset), "Email template reset"));
    }

    @GetMapping("/email/template/{templateId}/variable")
    @PreAuthorize("@access.has('EMAIL_CONFIG_READ') or @access.has('CONFIG_READ')")
    @Operation(summary = "Get variable placeholders for an email template")
    public ResponseEntity<ApiResponse<List<String>>> getTemplateVariables(@PathVariable Long templateId) {
        return ResponseEntity.ok(ApiResponse.success(emailService.getTemplateVariables(templateId), "Variables retrieved"));
    }

    @GetMapping(value = {"/email/log", "/email/sent"})
    @PreAuthorize("@access.has('EMAIL_CONFIG_READ') or @access.has('CONFIG_READ')")
    @Operation(summary = "Get email delivery logs")
    public ResponseEntity<ApiResponse<List<EmailLogResponse>>> getEmailLogs() {
        List<EmailLog> logs = emailService.getAllLogs();
        return ResponseEntity.ok(ApiResponse.success(emailLogMapper.toResponseList(logs), "Email logs retrieved"));
    }

    @GetMapping("/email/recipients/role/matrix")
    @PreAuthorize("@access.has('EMAIL_CONFIG_READ') or @access.has('CONFIG_READ')")
    @Operation(summary = "Get role notification trigger settings matrix")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getRoleNotificationMatrix() {
        return ResponseEntity.ok(ApiResponse.success(emailService.getRoleNotificationMatrix(), "Matrix retrieved"));
    }

    @PostMapping("/role-notifications/update")
    @PreAuthorize("@access.has('EMAIL_CONFIG_UPDATE') or @access.has('CONFIG_UPDATE')")
    @Operation(summary = "Update role notification settings")
    public ResponseEntity<ApiResponse<Void>> updateRoleNotifications(@RequestBody RoleNotificationUpdateDTO dto) {
        emailService.updateRoleNotifications(dto);
        return ResponseEntity.ok(ApiResponse.success(null, "Role notifications updated"));
    }

    @GetMapping("/user/{userId}/extra-points")
    @PreAuthorize("@access.has('EMAIL_CONFIG_READ') or @access.has('CONFIG_READ')")
    @Operation(summary = "Get user extra notification rules")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserExtraRules(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(emailService.getUserExtraRules(userId), "User extra rules retrieved"));
    }

    @PostMapping("/user/extra-rules/update")
    @PreAuthorize("@access.has('EMAIL_CONFIG_UPDATE') or @access.has('CONFIG_UPDATE')")
    @Operation(summary = "Update user extra notification rules")
    public ResponseEntity<ApiResponse<Void>> updateUserExtraRules(@RequestBody UserExtraRulesUpdateDTO dto) {
        emailService.updateUserExtraRules(dto);
        return ResponseEntity.ok(ApiResponse.success(null, "User extra rules updated"));
    }
}

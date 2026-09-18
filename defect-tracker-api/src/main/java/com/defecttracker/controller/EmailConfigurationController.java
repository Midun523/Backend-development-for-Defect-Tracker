package com.defecttracker.controller;

import com.defecttracker.dto.request.EmailConfigDTO;
import com.defecttracker.dto.request.EmailTemplateDTO;
import com.defecttracker.dto.request.RoleNotificationUpdateDTO;
import com.defecttracker.dto.request.UserExtraRulesUpdateDTO;
import com.defecttracker.dto.response.ApiResponse;
import com.defecttracker.entity.EmailConfig;
import com.defecttracker.entity.EmailLog;
import com.defecttracker.entity.EmailTemplate;
import com.defecttracker.service.EmailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Email & Notification Configuration", description = "Endpoints for dynamic SMTP configs, email templates, notification matrices, and sent logs")
public class EmailConfigurationController {

    private final EmailService emailService;

    @GetMapping("/email/config")
    @Operation(summary = "Get all email SMTP configurations")
    public ResponseEntity<ApiResponse<List<EmailConfig>>> getAllConfigs() {
        return ResponseEntity.ok(ApiResponse.success(emailService.getAllConfigs(), "Email configs retrieved"));
    }

    @GetMapping("/email/config/{id}")
    @Operation(summary = "Get email configuration by ID")
    public ResponseEntity<ApiResponse<EmailConfig>> getConfigById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(emailService.getConfigById(id), "Email config found"));
    }

    @PostMapping("/email/config")
    @Operation(summary = "Create email configuration")
    public ResponseEntity<ApiResponse<EmailConfig>> createConfig(@Valid @RequestBody EmailConfigDTO dto) {
        return ResponseEntity.ok(ApiResponse.created(emailService.saveConfig(dto), "Email config created"));
    }

    @PutMapping("/email/config/{id}")
    @Operation(summary = "Update email configuration")
    public ResponseEntity<ApiResponse<EmailConfig>> updateConfig(@PathVariable Long id, @Valid @RequestBody EmailConfigDTO dto) {
        dto.setId(id);
        return ResponseEntity.ok(ApiResponse.success(emailService.saveConfig(dto), "Email config updated"));
    }

    @PatchMapping("/email/config/{id}/enable")
    @Operation(summary = "Enable and set default email configuration")
    public ResponseEntity<ApiResponse<EmailConfig>> enableConfig(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(emailService.enableConfig(id), "Email config enabled"));
    }

    @DeleteMapping("/email/config/{id}")
    @Operation(summary = "Delete email configuration")
    public ResponseEntity<ApiResponse<Void>> deleteConfig(@PathVariable Long id) {
        emailService.deleteConfig(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Email config deleted"));
    }

    @GetMapping("/email/template")
    @Operation(summary = "Get all email templates")
    public ResponseEntity<ApiResponse<List<EmailTemplate>>> getAllTemplates() {
        return ResponseEntity.ok(ApiResponse.success(emailService.getAllTemplates(), "Email templates retrieved"));
    }

    @GetMapping("/email/template/{id}")
    @Operation(summary = "Get email template by ID")
    public ResponseEntity<ApiResponse<EmailTemplate>> getTemplateById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(emailService.getTemplateById(id), "Email template found"));
    }

    @PostMapping("/email/template")
    @Operation(summary = "Create email template")
    public ResponseEntity<ApiResponse<EmailTemplate>> createTemplate(@Valid @RequestBody EmailTemplateDTO dto) {
        return ResponseEntity.ok(ApiResponse.created(emailService.saveTemplate(dto), "Email template created"));
    }

    @PutMapping("/email/template/{id}")
    @Operation(summary = "Update email template")
    public ResponseEntity<ApiResponse<EmailTemplate>> updateTemplate(@PathVariable Long id, @Valid @RequestBody EmailTemplateDTO dto) {
        dto.setId(id);
        return ResponseEntity.ok(ApiResponse.success(emailService.saveTemplate(dto), "Email template updated"));
    }

    @PatchMapping("/email/template/{id}/reset")
    @Operation(summary = "Reset email template to default body")
    public ResponseEntity<ApiResponse<EmailTemplate>> resetTemplate(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(emailService.resetTemplate(id), "Email template reset"));
    }

    @GetMapping("/email/template/{templateId}/variable")
    @Operation(summary = "Get variable placeholders for an email template")
    public ResponseEntity<ApiResponse<List<String>>> getTemplateVariables(@PathVariable Long templateId) {
        return ResponseEntity.ok(ApiResponse.success(emailService.getTemplateVariables(templateId), "Variables retrieved"));
    }

    @GetMapping("/email/log")
    @Operation(summary = "Get all email delivery logs")
    public ResponseEntity<ApiResponse<List<EmailLog>>> getEmailLogs() {
        return ResponseEntity.ok(ApiResponse.success(emailService.getAllLogs(), "Email logs retrieved"));
    }

    @GetMapping("/email/sent")
    @Operation(summary = "Get sent email logs (alias)")
    public ResponseEntity<ApiResponse<List<EmailLog>>> getSentEmails() {
        return ResponseEntity.ok(ApiResponse.success(emailService.getAllLogs(), "Sent emails retrieved"));
    }

    @GetMapping("/email/recipients/role/matrix")
    @Operation(summary = "Get role notification trigger settings matrix")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getRoleNotificationMatrix() {
        return ResponseEntity.ok(ApiResponse.success(emailService.getRoleNotificationMatrix(), "Matrix retrieved"));
    }

    @PostMapping("/role-notifications/update")
    @Operation(summary = "Update role notification settings")
    public ResponseEntity<ApiResponse<Void>> updateRoleNotifications(@RequestBody RoleNotificationUpdateDTO dto) {
        emailService.updateRoleNotifications(dto);
        return ResponseEntity.ok(ApiResponse.success(null, "Role notifications updated"));
    }

    @GetMapping("/user/{userId}/extra-points")
    @Operation(summary = "Get user extra notification rules")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserExtraRules(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(emailService.getUserExtraRules(userId), "User extra rules retrieved"));
    }

    @PostMapping("/user/extra-rules/update")
    @Operation(summary = "Update user extra notification rules")
    public ResponseEntity<ApiResponse<Void>> updateUserExtraRules(@RequestBody UserExtraRulesUpdateDTO dto) {
        emailService.updateUserExtraRules(dto);
        return ResponseEntity.ok(ApiResponse.success(null, "User extra rules updated"));
    }
}

package com.defecttracker.modules.notification.controller;

import com.defecttracker.common.dto.ApiResponse;
import com.defecttracker.common.dto.PageResponse;
import com.defecttracker.modules.notification.dto.NotificationDto;
import com.defecttracker.modules.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Email & Notifications", description = "Endpoints for SMTP configuration, editable email templates, role recipient matrix, and sent email logs")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/config")
    @PreAuthorize("hasAuthority('CONFIG:READ') or hasRole('ADMIN')")
    @Operation(summary = "Get current SMTP settings")
    public ResponseEntity<ApiResponse<NotificationDto.EmailConfigDto>> getEmailConfig() {
        return ResponseEntity.ok(ApiResponse.success(notificationService.getEmailConfiguration()));
    }

    @PutMapping("/config")
    @PreAuthorize("hasAuthority('CONFIG:UPDATE') or hasRole('ADMIN')")
    @Operation(summary = "Update SMTP settings (host, port, credentials, enabled)")
    public ResponseEntity<ApiResponse<NotificationDto.EmailConfigDto>> updateEmailConfig(
            @Valid @RequestBody NotificationDto.EmailConfigDto dto) {
        return ResponseEntity.ok(ApiResponse.success("Email configuration updated", notificationService.updateEmailConfiguration(dto)));
    }

    @GetMapping("/templates")
    @PreAuthorize("hasAuthority('CONFIG:READ') or hasRole('ADMIN')")
    @Operation(summary = "Get all editable email templates")
    public ResponseEntity<ApiResponse<List<NotificationDto.EmailTemplateDto>>> getTemplates() {
        return ResponseEntity.ok(ApiResponse.success(notificationService.getAllTemplates()));
    }

    @PutMapping("/templates/{id}")
    @PreAuthorize("hasAuthority('CONFIG:UPDATE') or hasRole('ADMIN')")
    @Operation(summary = "Update an email template with subject and body variables")
    public ResponseEntity<ApiResponse<NotificationDto.EmailTemplateDto>> updateTemplate(
            @PathVariable Long id,
            @Valid @RequestBody NotificationDto.EmailTemplateDto dto) {
        return ResponseEntity.ok(ApiResponse.success("Email template updated", notificationService.updateTemplate(id, dto)));
    }

    @GetMapping("/matrix")
    @PreAuthorize("hasAuthority('CONFIG:READ') or hasRole('ADMIN')")
    @Operation(summary = "Get role-to-notification routing matrix")
    public ResponseEntity<ApiResponse<List<NotificationDto.RoleRecipientMatrixItem>>> getRoleMatrix() {
        return ResponseEntity.ok(ApiResponse.success(notificationService.getRoleRecipientMatrix()));
    }

    @PutMapping("/matrix")
    @PreAuthorize("hasAuthority('CONFIG:UPDATE') or hasRole('ADMIN')")
    @Operation(summary = "Configure role notification subscription")
    public ResponseEntity<ApiResponse<NotificationDto.RoleRecipientMatrixItem>> setRoleRecipient(
            @Valid @RequestBody NotificationDto.RoleRecipientMatrixItem dto) {
        return ResponseEntity.ok(ApiResponse.success("Role recipient preference saved", notificationService.setRoleRecipient(dto)));
    }

    @PutMapping("/overrides")
    @PreAuthorize("hasAuthority('CONFIG:UPDATE') or hasRole('ADMIN')")
    @Operation(summary = "Set individual employee opt-in or mute override")
    public ResponseEntity<ApiResponse<NotificationDto.EmployeeOverrideItem>> setEmployeeOverride(
            @Valid @RequestBody NotificationDto.EmployeeOverrideItem dto) {
        return ResponseEntity.ok(ApiResponse.success("Employee notification override saved", notificationService.setEmployeeOverride(dto)));
    }

    @GetMapping("/logs")
    @PreAuthorize("hasAuthority('CONFIG:READ') or hasRole('ADMIN')")
    @Operation(summary = "Get paginated sent email audit logs")
    public ResponseEntity<ApiResponse<PageResponse<NotificationDto.EmailLogResponse>>> getEmailLogs(
            @PageableDefault(size = 20, sort = "sentAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(notificationService.getEmailLogs(pageable)));
    }
}

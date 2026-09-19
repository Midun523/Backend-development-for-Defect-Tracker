package com.defecttracker.modules.notification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class NotificationDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmailConfigDto {
        private Long id;
        @NotBlank(message = "SMTP host is required")
        private String host;
        @NotNull(message = "SMTP port is required")
        private Integer port;
        private String username;
        private String password;
        @NotBlank(message = "From email is required")
        private String fromEmail;
        private String fromName;
        private boolean authEnabled;
        private boolean starttlsEnabled;
        private boolean enabled;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmailTemplateDto {
        private Long id;
        @NotBlank(message = "Notification type is required")
        private String notificationType;
        @NotBlank(message = "Subject is required")
        private String subject;
        @NotBlank(message = "Body template is required")
        private String body;
        private String description;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoleRecipientMatrixItem {
        private Long id;
        @NotNull(message = "Role ID is required")
        private Long roleId;
        private String roleName;
        @NotBlank(message = "Notification type is required")
        private String notificationType;
        private boolean enabled;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmployeeOverrideItem {
        private Long id;
        @NotNull(message = "Employee ID is required")
        private Long employeeId;
        private String employeeName;
        @NotBlank(message = "Notification type is required")
        private String notificationType;
        private boolean enabled;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmailLogResponse {
        private Long id;
        private String recipient;
        private String subject;
        private String body;
        private String notificationType;
        private String status;
        private String errorMessage;
        private LocalDateTime sentAt;
    }
}

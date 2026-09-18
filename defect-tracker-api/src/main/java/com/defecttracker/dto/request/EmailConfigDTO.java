package com.defecttracker.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailConfigDTO {
    private Long id;
    @NotBlank(message = "Config name is required")
    private String name;
    @NotBlank(message = "SMTP host is required")
    private String smtpHost;
    @NotNull(message = "SMTP port is required")
    private Integer smtpPort;
    private String username;
    private String password;
    @NotBlank(message = "From email is required")
    private String fromEmail;
    private String fromName;
    @Builder.Default
    private Boolean isActive = true;
    @Builder.Default
    private Boolean isDefault = false;
    @Builder.Default
    private Boolean useTls = true;
    @Builder.Default
    private Boolean useSsl = false;
}

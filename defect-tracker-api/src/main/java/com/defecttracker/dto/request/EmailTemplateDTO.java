package com.defecttracker.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailTemplateDTO {
    private Long id;
    @NotBlank(message = "Template name is required")
    private String templateName;
    @NotBlank(message = "Subject is required")
    private String subject;
    @NotBlank(message = "Body content is required")
    private String bodyContent;
    private String eventTrigger;
    private String variables;
    @Builder.Default
    private Boolean isActive = true;
    @Builder.Default
    private Boolean isDefault = false;
}

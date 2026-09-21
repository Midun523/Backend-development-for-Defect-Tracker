package com.defecttracker.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public record EmailTemplateResponse(
    Long id,
    String templateName,
    String subject,
    String bodyContent,
    String eventTrigger,
    String variables,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    @JsonProperty("default") boolean isDefault,
    @JsonProperty("active") boolean isActive
) {
    @JsonProperty("isDefault")
    public boolean getIsDefault() {
        return isDefault;
    }

    @JsonProperty("isActive")
    public boolean getIsActive() {
        return isActive;
    }
}

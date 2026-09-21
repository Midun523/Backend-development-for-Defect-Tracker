package com.defecttracker.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public record EmailConfigResponse(
    Long id,
    String name,
    String smtpHost,
    Integer smtpPort,
    String username,
    String fromEmail,
    String fromName,
    boolean useTls,
    boolean useSsl,
    boolean passwordSet,
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

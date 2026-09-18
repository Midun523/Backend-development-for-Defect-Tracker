package com.defecttracker.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleNotificationUpdateDTO {
    private Long roleId;
    private Map<String, Boolean> emailNotifications;
    private Map<String, Boolean> systemNotifications;
}

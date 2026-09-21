package com.defecttracker.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrivilegeTemplateRequest {
    private String privilegesType;
    private String subject;
    private String status;
}

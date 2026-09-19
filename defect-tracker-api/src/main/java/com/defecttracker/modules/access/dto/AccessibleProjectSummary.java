package com.defecttracker.modules.access.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccessibleProjectSummary {
    private Long id;
    private String name;
    private String description;
    private String status;
    private BigDecimal allocationPercentage;
    private boolean isProjectManager;
}

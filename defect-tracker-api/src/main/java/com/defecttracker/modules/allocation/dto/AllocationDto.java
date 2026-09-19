package com.defecttracker.modules.allocation.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class AllocationDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BenchEmployeeResponse {
        private Long id;
        private String employeeCode;
        private String fullName;
        private String email;
        private String designation;
        private BigDecimal initialCapacity;
        private BigDecimal currentCapacity;
        private boolean onBench;
        private int activeAllocationsCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AllocationRequest {
        @NotNull(message = "Employee ID is required")
        private Long employeeId;

        @NotNull(message = "Project ID is required")
        private Long projectId;

        @NotNull(message = "Allocation percentage is required")
        @DecimalMin(value = "1.00", message = "Allocation must be at least 1%")
        @DecimalMax(value = "100.00", message = "Allocation cannot exceed 100%")
        private BigDecimal allocationPercentage;

        @NotNull(message = "Start date is required")
        private LocalDate startDate;

        private LocalDate endDate;

        private String roleInProject;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProjectAllocationResponse {
        private Long id;
        private Long employeeId;
        private String employeeCode;
        private String employeeName;
        private Long projectId;
        private String projectName;
        private BigDecimal allocationPercentage;
        private LocalDate startDate;
        private LocalDate endDate;
        private String roleInProject;
        private boolean active;
        private LocalDateTime createdAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AllocationHistoryResponse {
        private Long id;
        private Long employeeId;
        private String employeeCode;
        private String employeeName;
        private Long projectId;
        private String projectName;
        private BigDecimal allocationPercentage;
        private LocalDate startDate;
        private LocalDate endDate;
        private String action;
        private Long durationDays;
        private String note;
        private LocalDateTime createdAt;
        private String createdBy;
    }
}

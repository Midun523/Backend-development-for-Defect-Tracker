package com.defecttracker.modules.allocation.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "allocation_histories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AllocationHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_id")
    private Long employeeId; // Survives employee deletion

    @Column(name = "employee_code", length = 50)
    private String employeeCode;

    @Column(name = "employee_name", nullable = false, length = 200)
    private String employeeName;

    @Column(name = "project_id")
    private Long projectId; // Survives project deletion

    @Column(name = "project_name", nullable = false, length = 200)
    private String projectName;

    @Column(name = "allocation_percentage", nullable = false, precision = 5, scale = 2)
    private BigDecimal allocationPercentage;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "action", nullable = false, length = 50)
    private String action; // ALLOCATED, DEALLOCATED, MODIFIED, PROJECT_ENDED

    @Column(name = "duration_days")
    private Long durationDays;

    @Column(name = "note", length = 255)
    private String note;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "created_by", length = 100)
    private String createdBy;
}

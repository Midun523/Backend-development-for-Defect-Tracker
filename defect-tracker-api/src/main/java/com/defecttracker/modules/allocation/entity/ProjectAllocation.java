package com.defecttracker.modules.allocation.entity;

import com.defecttracker.common.entity.BaseAuditableEntity;
import com.defecttracker.modules.access.entity.Employee;
import com.defecttracker.modules.project.entity.Project;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "project_allocations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectAllocation extends BaseAuditableEntity {

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(name = "allocation_percentage", nullable = false, precision = 5, scale = 2)
    private BigDecimal allocationPercentage;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "role_in_project", length = 100)
    @Builder.Default
    private String roleInProject = "Developer";

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;
}

package com.defecttracker.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "project_allocations")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectAllocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    private LocalDate startDate;
    private LocalDate endDate;

    @Builder.Default
    private Integer allocationPercentage = 100;

    @Column(length = 50)
    private String role; // Developer, QA Tester, Tech Lead

    @Builder.Default
    @Column(length = 30)
    private String status = "ACTIVE"; // ACTIVE, DEALLOCATED, EXTENDED

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Transient
    @JsonProperty("userFullName")
    public String getUserFullName() {
        if (employee != null) {
            String f = employee.getFirstName() != null ? employee.getFirstName() : "";
            String l = employee.getLastName() != null ? employee.getLastName() : "";
            return (f + " " + l).trim();
        }
        return "Unknown";
    }

    @Transient
    @JsonProperty("employeeId")
    public Long getEmployeeId() {
        return employee != null ? employee.getId() : null;
    }

    @Transient
    @JsonProperty("userId")
    public Long getUserId() {
        return employee != null ? employee.getId() : null;
    }

    @Transient
    @JsonProperty("roleName")
    public String getRoleName() {
        return role != null ? role : "Developer";
    }

    @Transient
    @JsonProperty("roleId")
    public Long getRoleId() {
        if ("Project Manager".equalsIgnoreCase(role)) {
            return 2L;
        }
        return 1L;
    }

    @Transient
    @JsonProperty("allocationPercent")
    public Integer getAllocationPercent() {
        return allocationPercentage;
    }

    @Transient
    @JsonProperty("projectId")
    public Long getProjectId() {
        return project != null ? project.getId() : null;
    }

    @Transient
    @JsonProperty("projectName")
    public String getProjectName() {
        return project != null ? project.getName() : "";
    }
}

package com.defecttracker.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "test_cases")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestCase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String testcaseNo; // e.g. "TC001"

    @Column(nullable = false, length = 1000)
    private String description;

    @Column(columnDefinition = "TEXT")
    private String detailsSteps;

    @Column(columnDefinition = "TEXT")
    private String expectedResult;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sub_module_id", nullable = false)
    private SubModule subModule;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "severity_id")
    private Severity severity;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "defect_type_id")
    private DefectType defectType;

    @Column(length = 50)
    private String type; // Functional, UI, Security, Regression, etc.

    @Builder.Default
    @Column(name = "test_case_required")
    private Boolean testCaseRequired = true;

    @Builder.Default
    @Column(length = 30)
    private String executionStatus = "NOT_RUN"; // PASS, FAIL, BLOCKED, NOT_RUN, HOLD

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "assigned_qa_id")
    private Employee assignedQa;

    @Column(length = 100)
    private String createdBy;

    @Column(length = 100)
    private String updatedBy;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}

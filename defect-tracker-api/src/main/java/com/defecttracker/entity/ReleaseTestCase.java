package com.defecttracker.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "release_test_cases", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"release_id", "test_case_id"})
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReleaseTestCase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "release_id", nullable = false)
    private Release release;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "test_case_id", nullable = false)
    private TestCase testCase;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "assigned_qa_id")
    private Employee assignedQa;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "linked_defect_id")
    private com.defecttracker.entity.Defect linkedDefect;

    @Builder.Default
    @Column(length = 30)
    private String executionStatus = "NOT_RUN"; // PASS, FAIL, BLOCKED, NOT_RUN, HOLD

    @Column(length = 1000)
    private String executionComment;

    private LocalDateTime executedAt;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @Transient
    @com.fasterxml.jackson.annotation.JsonProperty("defectNo")
    public String getDefectNo() {
        return linkedDefect != null ? linkedDefect.getDefectId() : null;
    }

    @Transient
    @com.fasterxml.jackson.annotation.JsonProperty("assignedTo")
    public String getAssignedToName() {
        if (linkedDefect != null && linkedDefect.getAssignedTo() != null) {
            return linkedDefect.getAssignedTo().getFirstName() + " " + linkedDefect.getAssignedTo().getLastName();
        }
        return null;
    }

    @Transient
    @com.fasterxml.jackson.annotation.JsonProperty("priorityName")
    public String getPriorityName() {
        if (linkedDefect != null && linkedDefect.getPriority() != null) {
            return linkedDefect.getPriority().getName();
        }
        return null;
    }

    @Transient
    @com.fasterxml.jackson.annotation.JsonProperty("testcaseNo")
    public String getTestcaseNo() {
        return testCase != null ? testCase.getTestcaseNo() : null;
    }

    @Transient
    @com.fasterxml.jackson.annotation.JsonProperty("description")
    public String getDescription() {
        return testCase != null ? testCase.getDescription() : null;
    }

    @Transient
    @com.fasterxml.jackson.annotation.JsonProperty("steps")
    public String getSteps() {
        return testCase != null ? testCase.getDetailsSteps() : null;
    }

    @Transient
    @com.fasterxml.jackson.annotation.JsonProperty("expectedResult")
    public String getExpectedResult() {
        return testCase != null ? testCase.getExpectedResult() : null;
    }

    @Transient
    @com.fasterxml.jackson.annotation.JsonProperty("severityName")
    public String getSeverityName() {
        return testCase != null && testCase.getSeverity() != null ? testCase.getSeverity().getName() : null;
    }

    @Transient
    @com.fasterxml.jackson.annotation.JsonProperty("defectTypeName")
    public String getDefectTypeName() {
        return testCase != null && testCase.getDefectType() != null ? testCase.getDefectType().getName() : null;
    }

    @Transient
    @com.fasterxml.jackson.annotation.JsonProperty("subModuleName")
    public String getSubModuleName() {
        return testCase != null && testCase.getSubModule() != null ? testCase.getSubModule().getName() : null;
    }

    @Transient
    @com.fasterxml.jackson.annotation.JsonProperty("moduleName")
    public String getModuleName() {
        return testCase != null && testCase.getSubModule() != null && testCase.getSubModule().getModule() != null ? testCase.getSubModule().getModule().getName() : null;
    }
}

package com.defecttracker.modules.release.entity;

import com.defecttracker.common.entity.BaseAuditableEntity;
import com.defecttracker.modules.access.entity.Employee;
import com.defecttracker.modules.testcase.entity.TestCase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "release_test_cases",
        uniqueConstraints = @UniqueConstraint(columnNames = {"release_id", "test_case_id"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReleaseTestCase extends BaseAuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "release_id", nullable = false)
    private Release release;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "test_case_id", nullable = false)
    private TestCase testCase;

    // QA-Allocation: assigned employee responsible for executing this test case
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "assigned_to_id")
    private Employee assignedTo;

    @Column(name = "execution_status", nullable = false, length = 50)
    @Builder.Default
    private String executionStatus = "PENDING"; // PENDING, PASSED, FAILED, BLOCKED, SKIPPED

    // Execution attribution
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "executed_by_id")
    private Employee executedBy;

    @Column(name = "executed_at")
    private LocalDateTime executedAt;

    @Column(name = "execution_notes", columnDefinition = "TEXT")
    private String executionNotes;
}

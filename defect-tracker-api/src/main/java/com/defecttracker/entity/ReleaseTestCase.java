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

    @Builder.Default
    @Column(length = 30)
    private String executionStatus = "NOT_RUN"; // PASS, FAIL, BLOCKED, NOT_RUN, HOLD

    @Column(length = 1000)
    private String executionComment;

    private LocalDateTime executedAt;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}

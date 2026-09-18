package com.defecttracker.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "test_case_allocation_logs")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestCaseAllocationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "release_id")
    private Release release;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "test_case_id")
    private TestCase testCase;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @Column(length = 100)
    private String action; // e.g. "ASSIGNED", "REMOVED", "STATUS_CHANGE"

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime timestamp;
}

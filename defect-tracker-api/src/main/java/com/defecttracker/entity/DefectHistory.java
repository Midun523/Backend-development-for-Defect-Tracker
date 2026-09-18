package com.defecttracker.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "defect_histories")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DefectHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "defect_id", nullable = false)
    private Defect defect;

    @Column(length = 50)
    private String fromStatus;

    @Column(nullable = false, length = 50)
    private String toStatus;

    @Column(length = 100)
    private String changedBy;

    @Column(length = 1000)
    private String comment;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime changedAt;
}

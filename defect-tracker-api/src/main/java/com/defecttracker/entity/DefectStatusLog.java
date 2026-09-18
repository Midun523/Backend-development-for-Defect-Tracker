package com.defecttracker.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "defect_status_logs")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DefectStatusLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "release_id")
    private Release release;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "defect_id", nullable = false)
    private Defect defect;

    @Column(nullable = false, length = 50)
    private String status;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime loggedAt;
}

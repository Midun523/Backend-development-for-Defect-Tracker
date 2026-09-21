package com.defecttracker.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "kloc_metrics")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KlocMetric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "defect_id")
    private Defect defect;

    @Column(name = "backend_repo_url", length = 500)
    private String backendRepoUrl;

    @Column(name = "frontend_repo_url", length = 500)
    private String frontendRepoUrl;

    @Column(name = "github_token", length = 255)
    @com.fasterxml.jackson.annotation.JsonProperty(access = com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY)
    private String githubToken;

    @Column(name = "github_username", length = 100)
    private String githubUsername;

    @Builder.Default
    @Column(name = "calculated_kloc")
    private Double calculatedKloc = 0.0;

    @Builder.Default
    @Column(name = "total_lines_of_code")
    private Long totalLinesOfCode = 0L;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

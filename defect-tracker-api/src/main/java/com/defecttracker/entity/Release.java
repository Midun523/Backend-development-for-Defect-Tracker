package com.defecttracker.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "releases")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Release {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50)
    private String releaseNo; // e.g. "REL001"

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 50)
    private String version; // e.g. "v1.0.0"

    @Column(length = 2000)
    private String description;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "release_type_id")
    private ReleaseType releaseType;

    @Builder.Default
    @Column(nullable = false, length = 30)
    private String status = "PLANNED"; // PLANNED, IN_PROGRESS, TESTING, RELEASED, COMPLETED

    private LocalDate startDate;
    private LocalDate releaseDate;
    private LocalDate endDate;

    @Builder.Default
    private Double kloc = 0.0;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Transient
    @JsonProperty("releaseId")
    public Long getReleaseId() {
        return id;
    }

    @Transient
    @JsonProperty("releaseName")
    public String getReleaseName() {
        return name;
    }

    @Transient
    @JsonProperty("projectId")
    public Long fetchProjectId() {
        return project != null ? project.getId() : null;
    }

    @Transient
    @JsonProperty("projectName")
    public String fetchProjectName() {
        return project != null ? project.getName() : "";
    }

    @Transient
    @JsonProperty("releaseTypeId")
    public Long fetchReleaseTypeId() {
        return releaseType != null ? releaseType.getId() : null;
    }

    @Transient
    @JsonProperty("releaseTypeName")
    public String fetchReleaseTypeName() {
        return releaseType != null ? releaseType.getName() : "";
    }
}

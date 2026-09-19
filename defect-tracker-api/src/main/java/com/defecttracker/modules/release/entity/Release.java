package com.defecttracker.modules.release.entity;

import com.defecttracker.common.entity.BaseAuditableEntity;
import com.defecttracker.modules.masterdata.entity.ReleaseType;
import com.defecttracker.modules.project.entity.Project;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "releases",
        uniqueConstraints = @UniqueConstraint(columnNames = {"project_id", "version"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Release extends BaseAuditableEntity {

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "version", nullable = false, length = 50)
    private String version;

    @Column(name = "release_date")
    private LocalDate releaseDate;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "release_type_id")
    private ReleaseType releaseType;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(name = "status", nullable = false, length = 50)
    @Builder.Default
    private String status = "ACTIVE"; // ACTIVE, CLOSED, PLANNED, CANCELLED

    @Column(name = "kloc", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal kloc = BigDecimal.ZERO;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @OneToMany(mappedBy = "release", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ReleaseTestCase> releaseTestCases = new ArrayList<>();
}

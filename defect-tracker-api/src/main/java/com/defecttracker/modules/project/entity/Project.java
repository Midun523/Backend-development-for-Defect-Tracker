package com.defecttracker.modules.project.entity;

import com.defecttracker.common.entity.BaseAuditableEntity;
import com.defecttracker.modules.access.entity.Employee;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
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
@Table(name = "projects")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Project extends BaseAuditableEntity {

    @Column(name = "name", nullable = false, unique = true, length = 150)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "status", nullable = false, length = 50)
    @Builder.Default
    private String status = "ACTIVE"; // ACTIVE, COMPLETED, ON_HOLD

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "client_id", unique = true)
    private Client client;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "project_manager_id")
    private Employee projectManager;

    @Column(name = "kloc", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal kloc = BigDecimal.ZERO;

    @Column(name = "source_control_url", length = 255)
    private String sourceControlUrl;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ProjectModule> modules = new ArrayList<>();
}

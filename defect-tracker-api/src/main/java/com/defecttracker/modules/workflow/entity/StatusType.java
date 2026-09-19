package com.defecttracker.modules.workflow.entity;

import com.defecttracker.common.entity.BaseAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "status_types")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatusType extends BaseAuditableEntity {

    @Column(name = "name", nullable = false, unique = true, length = 50)
    private String name;

    @Column(name = "category", nullable = false, length = 30)
    @Builder.Default
    private String category = "OPEN"; // OPEN, IN_PROGRESS, RESOLVED, CLOSED

    @Column(name = "display_color", nullable = false, length = 30)
    @Builder.Default
    private String displayColor = "#3B82F6";

    @Column(name = "is_default_initial", nullable = false)
    @Builder.Default
    private boolean defaultInitial = false;

    @Column(name = "is_open_stage", nullable = false)
    @Builder.Default
    private boolean openStage = false;

    @Column(name = "is_resolved_stage", nullable = false)
    @Builder.Default
    private boolean resolvedStage = false;

    // Visual Workflow Node coordinates for frontend canvas
    @Column(name = "position_x")
    @Builder.Default
    private Double positionX = 100.0;

    @Column(name = "position_y")
    @Builder.Default
    private Double positionY = 100.0;
}

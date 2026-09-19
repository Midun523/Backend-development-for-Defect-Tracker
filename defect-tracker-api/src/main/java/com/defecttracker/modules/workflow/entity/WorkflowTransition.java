package com.defecttracker.modules.workflow.entity;

import com.defecttracker.common.entity.BaseAuditableEntity;
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

@Entity
@Table(
        name = "workflow_transitions",
        uniqueConstraints = @UniqueConstraint(columnNames = {"from_status_id", "to_status_id"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkflowTransition extends BaseAuditableEntity {

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "from_status_id", nullable = false)
    private StatusType fromStatus;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "to_status_id", nullable = false)
    private StatusType toStatus;

    @Column(name = "name", length = 100)
    private String name;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "requires_comment", nullable = false)
    @Builder.Default
    private boolean requiresComment = false;
}

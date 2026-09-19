package com.defecttracker.modules.defect.entity;

import com.defecttracker.modules.access.entity.Employee;
import com.defecttracker.modules.workflow.entity.StatusType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "defect_histories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DefectHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "defect_id", nullable = false)
    private Defect defect;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "from_status_id")
    private StatusType fromStatus;

    @Column(name = "from_status_name", length = 50)
    private String fromStatusName;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "to_status_id", nullable = false)
    private StatusType toStatus;

    @Column(name = "to_status_name", nullable = false, length = 50)
    private String toStatusName;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "changed_by_id")
    private Employee changedBy;

    @Column(name = "changed_by_name", length = 150)
    private String changedByName;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}

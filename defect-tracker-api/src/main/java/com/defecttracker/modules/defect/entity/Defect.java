package com.defecttracker.modules.defect.entity;

import com.defecttracker.common.entity.BaseAuditableEntity;
import com.defecttracker.modules.access.entity.Employee;
import com.defecttracker.modules.masterdata.entity.DefectType;
import com.defecttracker.modules.masterdata.entity.Priority;
import com.defecttracker.modules.masterdata.entity.Severity;
import com.defecttracker.modules.project.entity.Project;
import com.defecttracker.modules.project.entity.ProjectModule;
import com.defecttracker.modules.project.entity.SubModule;
import com.defecttracker.modules.release.entity.Release;
import com.defecttracker.modules.testcase.entity.TestCase;
import com.defecttracker.modules.workflow.entity.StatusType;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "defects",
        uniqueConstraints = @UniqueConstraint(columnNames = {"project_id", "project_seq_num"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Defect extends BaseAuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(name = "project_seq_num", nullable = false)
    private Long projectSeqNum;

    @Column(name = "defect_code", nullable = false, length = 50)
    private String defectCode; // e.g. "PRJ-1"

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "steps_to_reproduce", nullable = false, columnDefinition = "TEXT")
    private String stepsToReproduce;

    @Column(name = "attachment_url", length = 255)
    private String attachmentUrl;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "module_id")
    private ProjectModule module;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sub_module_id")
    private SubModule subModule;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "defect_type_id")
    private DefectType defectType;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "severity_id")
    private Severity severity;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "priority_id")
    private Priority priority;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "status_id", nullable = false)
    private StatusType status;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "assigned_to_id")
    private Employee assignedTo;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "reporter_id")
    private Employee reporter;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "release_id")
    private Release release;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "test_case_id")
    private TestCase testCase;

    @Column(name = "reopen_counter", nullable = false)
    @Builder.Default
    private Integer reopenCounter = 0;

    @Column(name = "first_assigned_at")
    private LocalDateTime firstAssignedAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;

    @OneToMany(mappedBy = "defect", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<DefectHistory> history = new ArrayList<>();

    @OneToMany(mappedBy = "defect", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<DefectComment> comments = new ArrayList<>();
}

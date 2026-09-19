package com.defecttracker.modules.testcase.entity;

import com.defecttracker.common.entity.BaseAuditableEntity;
import com.defecttracker.modules.masterdata.entity.DefectType;
import com.defecttracker.modules.masterdata.entity.Severity;
import com.defecttracker.modules.project.entity.SubModule;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "test_cases")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestCase extends BaseAuditableEntity {

    @Column(name = "test_case_code", nullable = false, length = 50)
    private String testCaseCode;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "steps", nullable = false, columnDefinition = "TEXT")
    private String steps;

    @Column(name = "expected_result", nullable = false, columnDefinition = "TEXT")
    private String expectedResult;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sub_module_id", nullable = false)
    private SubModule subModule;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "severity_id")
    private Severity severity;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "defect_type_id")
    private DefectType defectType;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;
}

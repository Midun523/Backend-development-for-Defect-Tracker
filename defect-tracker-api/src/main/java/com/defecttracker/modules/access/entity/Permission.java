package com.defecttracker.modules.access.entity;

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
@Table(name = "permissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Permission extends BaseAuditableEntity {

    @Column(name = "code", nullable = false, unique = true, length = 100)
    private String code; // e.g. "PROJECT:READ", "DEFECT:CREATE"

    @Column(name = "feature_area", nullable = false, length = 50)
    private String featureArea; // e.g. "PROJECT", "DEFECT", "TEST_CASE"

    @Column(name = "action", nullable = false, length = 50)
    private String action; // e.g. "READ", "CREATE", "UPDATE", "DELETE"

    @Column(name = "description", length = 255)
    private String description;
}

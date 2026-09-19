package com.defecttracker.modules.masterdata.entity;

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
@Table(name = "severities")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Severity extends BaseAuditableEntity {

    @Column(name = "name", nullable = false, unique = true, length = 50)
    private String name;

    @Column(name = "weight", nullable = false)
    @Builder.Default
    private Integer weight = 1;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "color_code", length = 30)
    @Builder.Default
    private String colorCode = "#3B82F6";
}

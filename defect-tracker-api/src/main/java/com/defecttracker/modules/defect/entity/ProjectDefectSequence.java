package com.defecttracker.modules.defect.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "project_defect_sequences")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectDefectSequence {

    @Id
    @Column(name = "project_id")
    private Long projectId;

    @Column(name = "current_seq", nullable = false)
    @Builder.Default
    private Long currentSeq = 0L;
}

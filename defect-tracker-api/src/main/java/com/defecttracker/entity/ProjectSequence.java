package com.defecttracker.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "project_sequences", uniqueConstraints = {
    @UniqueConstraint(name = "uk_project_seq", columnNames = {"project_id", "sequence_type"})
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectSequence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(name = "sequence_type", nullable = false, length = 20)
    private String sequenceType;

    @Column(name = "current_value", nullable = false)
    @Builder.Default
    private Long currentValue = 0L;
}

package com.defecttracker.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "privilege_templates")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrivilegeTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "privileges_type", nullable = false, length = 100)
    private String privilegesType; // e.g., "DEFECT_MANAGEMENT", "PROJECT_MANAGEMENT"

    @Column(length = 200)
    private String subject; // e.g. "Create & edit defects permission"

    @Builder.Default
    @Column(nullable = false, length = 30)
    private String status = "active"; // active, inactive

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

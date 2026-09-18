package com.defecttracker.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "permissions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long permissionId;

    @Column(nullable = false, unique = true, length = 100)
    private String action; // e.g. "DESIGNATION_CREATE", "DEFECT_READ"

    @Column(length = 500)
    private String description;
}

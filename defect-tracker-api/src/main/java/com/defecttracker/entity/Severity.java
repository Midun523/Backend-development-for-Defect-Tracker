package com.defecttracker.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "severities")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Severity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name; // Low, Medium, High, Critical

    @Column(length = 30)
    private String color;

    @Column(length = 255)
    private String description;

    @Column
    @Builder.Default
    private Integer weight = 1;
}

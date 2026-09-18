package com.defecttracker.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "projects")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, length = 50)
    private String projectId; // e.g. "PRJ001"

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 20)
    private String prefix; // e.g. "DT"

    @Column(length = 50)
    private String projectType;

    @Builder.Default
    @Column(nullable = false, length = 30)
    private String status = "ACTIVE"; // ACTIVE, INACTIVE, COMPLETED

    private LocalDate startDate;
    private LocalDate endDate;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "manager_id")
    private Employee manager;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "client_id")
    private Client client;

    @Column(length = 150)
    private String clientName;

    @Column(length = 100)
    private String clientCountry;

    @Column(length = 100)
    private String clientState;

    @Column(length = 150)
    private String clientEmail;

    @Column(length = 30)
    private String clientPhone;

    @Column(length = 500)
    private String address;

    @Column(length = 2000)
    private String description;

    @Builder.Default
    private Double progress = 0.0;

    @Builder.Default
    private Double kloc = 0.0; // Kilo Lines of Code

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}

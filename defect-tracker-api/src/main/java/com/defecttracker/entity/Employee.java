package com.defecttracker.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "employees")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @Column(nullable = false, length = 100)
    private String firstName;

    @Column(nullable = false, length = 100)
    private String lastName;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(length = 30)
    private String phone;

    @Column(length = 20)
    private String gender;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "designation_id")
    private Designation designation;

    @Builder.Default
    private Double experience = 0.0;

    private LocalDate joinedDate;

    @Column(length = 1000)
    private String skills; // JSON or comma-separated

    @Builder.Default
    private Integer availability = 100; // availability percentage 0-100

    @Builder.Default
    @Column(length = 30)
    private String status = "active"; // active, inactive, on-leave

    @Column(length = 100)
    private String department;

    @Column(length = 100)
    private String manager;

    private LocalDate startDate;
    private LocalDate endDate;

    @Column(length = 30)
    private String whatsappNumber;

    @Builder.Default
    @Column(name = "reset_count")
    private Integer resetCount = 0;

    @Column(name = "user_token", length = 500)
    private String userToken;

    @Column(name = "forgot_password_token", length = 255)
    private String forgotPasswordToken;

    @Column(name = "forgot_password_token_expiry")
    private LocalDateTime forgotPasswordTokenExpiry;

    @Column(length = 500)
    private String address;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}

package com.defecttracker.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "email_configs")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 150)
    private String smtpHost;

    @Column(nullable = false)
    private Integer smtpPort;

    @Column(length = 150)
    private String username;

    @Column(length = 255)
    private String password;

    @Column(nullable = false, length = 150)
    private String fromEmail;

    @Column(length = 100)
    private String fromName;

    @Builder.Default
    private boolean isActive = true;

    @Builder.Default
    private boolean isDefault = false;

    @Builder.Default
    private boolean useTls = true;

    @Builder.Default
    private boolean useSsl = false;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}

package com.defecttracker.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "role_notification_settings")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleNotificationSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Column(nullable = false, length = 100)
    private String pointKey; // e.g., "DEFECT_NEW", "DEFECT_REOPENED", "TESTCASE_FAILED"

    @Builder.Default
    private boolean isEmailEnabled = true;

    @Builder.Default
    private boolean isSystemEnabled = true;
}

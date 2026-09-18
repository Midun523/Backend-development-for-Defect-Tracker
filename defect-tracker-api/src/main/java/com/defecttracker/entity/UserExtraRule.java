package com.defecttracker.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_extra_rules")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserExtraRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 100)
    private String ruleKey; // e.g., "RECEIVE_DAILY_DIGEST", "SMS_NOTIFICATIONS"

    @Builder.Default
    private boolean isEnabled = true;
}

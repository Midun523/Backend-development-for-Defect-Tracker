package com.defecttracker.modules.notification.entity;

import com.defecttracker.common.entity.BaseAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "email_configurations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailConfiguration extends BaseAuditableEntity {

    @Column(name = "host", nullable = false, length = 150)
    private String host;

    @Column(name = "port", nullable = false)
    @Builder.Default
    private Integer port = 587;

    @Column(name = "username", length = 150)
    private String username;

    @Column(name = "password", length = 255)
    private String password;

    @Column(name = "from_email", nullable = false, length = 150)
    private String fromEmail;

    @Column(name = "from_name", length = 150)
    private String fromName;

    @Column(name = "auth_enabled", nullable = false)
    @Builder.Default
    private boolean authEnabled = true;

    @Column(name = "starttls_enabled", nullable = false)
    @Builder.Default
    private boolean starttlsEnabled = true;

    @Column(name = "is_enabled", nullable = false)
    @Builder.Default
    private boolean enabled = false;
}

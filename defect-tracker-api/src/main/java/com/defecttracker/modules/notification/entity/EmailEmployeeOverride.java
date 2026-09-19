package com.defecttracker.modules.notification.entity;

import com.defecttracker.common.entity.BaseAuditableEntity;
import com.defecttracker.modules.access.entity.Employee;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "email_employee_overrides",
        uniqueConstraints = @UniqueConstraint(columnNames = {"employee_id", "notification_type"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailEmployeeOverride extends BaseAuditableEntity {

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "notification_type", nullable = false, length = 100)
    private String notificationType;

    @Column(name = "is_enabled", nullable = false)
    private boolean enabled;
}

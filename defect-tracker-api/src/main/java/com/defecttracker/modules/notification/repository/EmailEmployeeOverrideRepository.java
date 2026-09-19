package com.defecttracker.modules.notification.repository;

import com.defecttracker.modules.notification.entity.EmailEmployeeOverride;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmailEmployeeOverrideRepository extends JpaRepository<EmailEmployeeOverride, Long> {
    List<EmailEmployeeOverride> findByNotificationType(String notificationType);
    List<EmailEmployeeOverride> findByEmployeeId(Long employeeId);
    Optional<EmailEmployeeOverride> findByEmployeeIdAndNotificationType(Long employeeId, String notificationType);
}

package com.defecttracker.modules.notification.repository;

import com.defecttracker.modules.notification.entity.EmailRoleRecipient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmailRoleRecipientRepository extends JpaRepository<EmailRoleRecipient, Long> {
    List<EmailRoleRecipient> findByNotificationTypeAndEnabledTrue(String notificationType);
    List<EmailRoleRecipient> findByRoleId(Long roleId);
    Optional<EmailRoleRecipient> findByRoleIdAndNotificationType(Long roleId, String notificationType);
}

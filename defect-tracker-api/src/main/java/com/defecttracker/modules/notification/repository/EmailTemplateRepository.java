package com.defecttracker.modules.notification.repository;

import com.defecttracker.modules.notification.entity.EmailTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmailTemplateRepository extends JpaRepository<EmailTemplate, Long> {
    Optional<EmailTemplate> findByNotificationType(String notificationType);
    boolean existsByNotificationType(String notificationType);
}

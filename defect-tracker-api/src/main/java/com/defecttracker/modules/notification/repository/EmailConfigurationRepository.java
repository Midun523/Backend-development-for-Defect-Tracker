package com.defecttracker.modules.notification.repository;

import com.defecttracker.modules.notification.entity.EmailConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmailConfigurationRepository extends JpaRepository<EmailConfiguration, Long> {
    Optional<EmailConfiguration> findFirstByOrderByIdAsc();
}

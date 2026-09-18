package com.defecttracker.repository;

import com.defecttracker.entity.EmailConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmailConfigRepository extends JpaRepository<EmailConfig, Long> {
    Optional<EmailConfig> findFirstByIsDefaultTrue();
    Optional<EmailConfig> findFirstByIsActiveTrue();
}

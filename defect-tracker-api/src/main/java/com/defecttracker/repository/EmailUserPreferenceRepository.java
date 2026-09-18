package com.defecttracker.repository;

import com.defecttracker.entity.EmailUserPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmailUserPreferenceRepository extends JpaRepository<EmailUserPreference, Long> {
    List<EmailUserPreference> findByEmployeeId(Long employeeId);
    List<EmailUserPreference> findByEmailTemplateId(Long emailTemplateId);
    Optional<EmailUserPreference> findByEmployeeIdAndEmailTemplateId(Long employeeId, Long emailTemplateId);
}

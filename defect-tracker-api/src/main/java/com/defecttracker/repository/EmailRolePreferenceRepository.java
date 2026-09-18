package com.defecttracker.repository;

import com.defecttracker.entity.EmailRolePreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmailRolePreferenceRepository extends JpaRepository<EmailRolePreference, Long> {
    List<EmailRolePreference> findByRoleId(Long roleId);
    List<EmailRolePreference> findByEmailTemplateId(Long emailTemplateId);
    Optional<EmailRolePreference> findByRoleIdAndEmailTemplateId(Long roleId, Long emailTemplateId);
}

package com.defecttracker.repository;

import com.defecttracker.entity.PrivilegeTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PrivilegeTemplateRepository extends JpaRepository<PrivilegeTemplate, Long> {
    Optional<PrivilegeTemplate> findByPrivilegesType(String privilegesType);
    List<PrivilegeTemplate> findByStatus(String status);
}

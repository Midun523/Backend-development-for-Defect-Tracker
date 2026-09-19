package com.defecttracker.modules.workflow.repository;

import com.defecttracker.modules.workflow.entity.StatusType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StatusTypeRepository extends JpaRepository<StatusType, Long> {
    Optional<StatusType> findByName(String name);
    boolean existsByName(String name);
    Optional<StatusType> findByDefaultInitialTrue();
}

package com.defecttracker.repository;

import com.defecttracker.entity.StatusType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StatusTypeRepository extends JpaRepository<StatusType, Long> {
    Optional<StatusType> findByName(String name);
    Optional<StatusType> findByNameIgnoreCase(String name);
    Optional<StatusType> findFirstByIsDefaultTrue();
    Optional<StatusType> findTopByOrderByIdAsc();
}

package com.defecttracker.repository;

import com.defecttracker.entity.Severity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SeverityRepository extends JpaRepository<Severity, Long> {
    Optional<Severity> findByName(String name);
    Optional<Severity> findTopByOrderByWeightAscIdAsc();
    Optional<Severity> findTopByOrderByIdAsc();
}

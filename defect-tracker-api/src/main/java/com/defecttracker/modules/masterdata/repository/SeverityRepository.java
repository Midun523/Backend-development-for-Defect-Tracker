package com.defecttracker.modules.masterdata.repository;

import com.defecttracker.modules.masterdata.entity.Severity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SeverityRepository extends JpaRepository<Severity, Long> {
    Optional<Severity> findByName(String name);
    boolean existsByName(String name);
    List<Severity> findAllByOrderByWeightAsc();
}

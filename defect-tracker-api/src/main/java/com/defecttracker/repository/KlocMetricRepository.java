package com.defecttracker.repository;

import com.defecttracker.entity.KlocMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KlocMetricRepository extends JpaRepository<KlocMetric, Long> {
    List<KlocMetric> findByProjectId(Long projectId);
    Optional<KlocMetric> findFirstByProjectIdOrderByCreatedAtDesc(Long projectId);
}

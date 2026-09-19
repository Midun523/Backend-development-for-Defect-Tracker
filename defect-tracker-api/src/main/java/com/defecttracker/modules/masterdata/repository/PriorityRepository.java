package com.defecttracker.modules.masterdata.repository;

import com.defecttracker.modules.masterdata.entity.Priority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PriorityRepository extends JpaRepository<Priority, Long> {
    Optional<Priority> findByName(String name);
    boolean existsByName(String name);
    List<Priority> findAllByOrderByWeightAsc();
}

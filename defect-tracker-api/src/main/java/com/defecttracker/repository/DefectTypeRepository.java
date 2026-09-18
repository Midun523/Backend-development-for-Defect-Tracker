package com.defecttracker.repository;

import com.defecttracker.entity.DefectType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DefectTypeRepository extends JpaRepository<DefectType, Long> {
    Optional<DefectType> findByName(String name);
}

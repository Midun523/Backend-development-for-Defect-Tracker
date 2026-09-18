package com.defecttracker.repository;

import com.defecttracker.entity.ReleaseType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReleaseTypeRepository extends JpaRepository<ReleaseType, Long> {
    Optional<ReleaseType> findByName(String name);
}

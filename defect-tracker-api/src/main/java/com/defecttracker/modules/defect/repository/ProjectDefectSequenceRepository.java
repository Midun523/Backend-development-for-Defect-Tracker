package com.defecttracker.modules.defect.repository;

import com.defecttracker.modules.defect.entity.ProjectDefectSequence;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProjectDefectSequenceRepository extends JpaRepository<ProjectDefectSequence, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM ProjectDefectSequence s WHERE s.projectId = :projectId")
    Optional<ProjectDefectSequence> findByProjectIdForUpdate(@Param("projectId") Long projectId);
}

package com.defecttracker.repository;

import com.defecttracker.entity.ProjectSequence;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProjectSequenceRepository extends JpaRepository<ProjectSequence, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT ps FROM ProjectSequence ps WHERE ps.project.id = :projectId AND ps.sequenceType = :sequenceType")
    Optional<ProjectSequence> findByProjectIdAndSequenceTypeForUpdate(
            @Param("projectId") Long projectId,
            @Param("sequenceType") String sequenceType
    );

    Optional<ProjectSequence> findByProjectIdAndSequenceType(Long projectId, String sequenceType);
}

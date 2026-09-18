package com.defecttracker.repository;

import com.defecttracker.entity.WorkflowPosition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkflowPositionRepository extends JpaRepository<WorkflowPosition, Long> {
    List<WorkflowPosition> findByProjectId(Long projectId);
    List<WorkflowPosition> findByStatusTypeId(Long statusTypeId);
    Optional<WorkflowPosition> findByStatusTypeIdAndProjectId(Long statusTypeId, Long projectId);
    Optional<WorkflowPosition> findByStatusTypeIdAndProjectIsNull(Long statusTypeId);
}

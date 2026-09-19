package com.defecttracker.modules.workflow.repository;

import com.defecttracker.modules.workflow.entity.WorkflowTransition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkflowTransitionRepository extends JpaRepository<WorkflowTransition, Long> {
    List<WorkflowTransition> findByFromStatusId(Long fromStatusId);
    List<WorkflowTransition> findByToStatusId(Long toStatusId);
    Optional<WorkflowTransition> findByFromStatusIdAndToStatusId(Long fromStatusId, Long toStatusId);
    boolean existsByFromStatusIdAndToStatusId(Long fromStatusId, Long toStatusId);
    void deleteByFromStatusIdAndToStatusId(Long fromStatusId, Long toStatusId);
}

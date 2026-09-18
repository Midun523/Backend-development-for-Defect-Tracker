package com.defecttracker.repository;

import com.defecttracker.entity.StatusTransition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StatusTransitionRepository extends JpaRepository<StatusTransition, Long> {
    List<StatusTransition> findByFromStatusId(Long fromStatusId);
    List<StatusTransition> findByToStatusId(Long toStatusId);
}

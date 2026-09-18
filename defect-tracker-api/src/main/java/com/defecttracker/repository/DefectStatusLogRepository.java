package com.defecttracker.repository;

import com.defecttracker.entity.DefectStatusLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DefectStatusLogRepository extends JpaRepository<DefectStatusLog, Long> {
    List<DefectStatusLog> findByProjectIdAndReleaseId(Long projectId, Long releaseId);
}

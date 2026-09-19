package com.defecttracker.modules.defect.repository;

import com.defecttracker.modules.defect.entity.DefectComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DefectCommentRepository extends JpaRepository<DefectComment, Long> {
    List<DefectComment> findByDefectIdAndParentCommentIsNullOrderByCreatedAtAsc(Long defectId);
    List<DefectComment> findByDefectIdOrderByCreatedAtAsc(Long defectId);
}

package com.defecttracker.repository;

import com.defecttracker.entity.DefectComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DefectCommentRepository extends JpaRepository<DefectComment, Long> {
    List<DefectComment> findByDefectIdOrderByCreatedAtAsc(Long defectId);
}

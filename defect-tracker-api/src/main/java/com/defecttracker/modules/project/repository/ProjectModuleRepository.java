package com.defecttracker.modules.project.repository;

import com.defecttracker.modules.project.entity.ProjectModule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectModuleRepository extends JpaRepository<ProjectModule, Long> {
    List<ProjectModule> findByProjectId(Long projectId);
    Optional<ProjectModule> findByProjectIdAndName(Long projectId, String name);
    boolean existsByProjectIdAndName(Long projectId, String name);
}

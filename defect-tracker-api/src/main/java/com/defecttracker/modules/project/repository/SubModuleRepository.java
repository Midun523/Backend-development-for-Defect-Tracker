package com.defecttracker.modules.project.repository;

import com.defecttracker.modules.project.entity.SubModule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubModuleRepository extends JpaRepository<SubModule, Long> {
    List<SubModule> findByModuleId(Long moduleId);
    Optional<SubModule> findByModuleIdAndName(Long moduleId, String name);
    boolean existsByModuleIdAndName(Long moduleId, String name);
}

package com.defecttracker.repository;

import com.defecttracker.entity.SubModule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubModuleRepository extends JpaRepository<SubModule, Long> {
    List<SubModule> findByModuleId(Long moduleId);
    List<SubModule> findByModuleIdIn(List<Long> moduleIds);
}

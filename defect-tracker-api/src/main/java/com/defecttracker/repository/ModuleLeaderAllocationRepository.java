package com.defecttracker.repository;

import com.defecttracker.entity.ModuleLeaderAllocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ModuleLeaderAllocationRepository extends JpaRepository<ModuleLeaderAllocation, Long> {
    List<ModuleLeaderAllocation> findByModuleId(Long moduleId);
    Optional<ModuleLeaderAllocation> findFirstByModuleIdOrderByAllocatedDateDesc(Long moduleId);
    void deleteByModuleId(Long moduleId);
}

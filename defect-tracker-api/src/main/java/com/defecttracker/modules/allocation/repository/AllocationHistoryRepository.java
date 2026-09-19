package com.defecttracker.modules.allocation.repository;

import com.defecttracker.modules.allocation.entity.AllocationHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AllocationHistoryRepository extends JpaRepository<AllocationHistory, Long> {
    List<AllocationHistory> findByEmployeeIdOrderByCreatedAtDesc(Long employeeId);
    List<AllocationHistory> findByProjectIdOrderByCreatedAtDesc(Long projectId);
    Page<AllocationHistory> findByEmployeeId(Long employeeId, Pageable pageable);
    Page<AllocationHistory> findByProjectId(Long projectId, Pageable pageable);
}

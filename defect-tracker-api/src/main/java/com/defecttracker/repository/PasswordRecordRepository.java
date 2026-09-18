package com.defecttracker.repository;

import com.defecttracker.entity.PasswordRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PasswordRecordRepository extends JpaRepository<PasswordRecord, Long> {
    List<PasswordRecord> findByEmployeeIdOrderByCreatedAtDesc(Long employeeId);
}

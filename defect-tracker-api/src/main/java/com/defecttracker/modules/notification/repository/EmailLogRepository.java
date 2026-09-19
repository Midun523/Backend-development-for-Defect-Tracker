package com.defecttracker.modules.notification.repository;

import com.defecttracker.modules.notification.entity.EmailLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmailLogRepository extends JpaRepository<EmailLog, Long> {
    Page<EmailLog> findAllByOrderBySentAtDesc(Pageable pageable);
}

package com.campusconnect.repository;

import com.campusconnect.entity.AdminActionLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminActionLogRepository extends JpaRepository<AdminActionLog, Long> {
    Page<AdminActionLog> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
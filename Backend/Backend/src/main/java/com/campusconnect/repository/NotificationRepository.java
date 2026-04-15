package com.campusconnect.repository;
import com.campusconnect.entity.Notification;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    @Query("SELECT n FROM Notification n WHERE n.user.id = :uid ORDER BY n.createdAt DESC")
    Page<Notification> findByUserId(@Param("uid") Long userId, Pageable pageable);
    int countByUserIdAndIsReadFalse(Long userId);
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.user.id = :uid AND n.isRead = false")
    void markAllRead(@Param("uid") Long userId);
}
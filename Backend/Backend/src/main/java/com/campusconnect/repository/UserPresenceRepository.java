package com.campusconnect.repository;

import com.campusconnect.entity.UserPresence;
import com.campusconnect.enums.PresenceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface UserPresenceRepository extends JpaRepository<UserPresence, Long> {

    @Query("SELECT p FROM UserPresence p WHERE p.userId IN :userIds")
    List<UserPresence> findByUserIds(@Param("userIds") Collection<Long> userIds);

    List<UserPresence> findByStatus(PresenceStatus status);
}
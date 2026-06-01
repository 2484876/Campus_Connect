package com.campusconnect.repository;

import com.campusconnect.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    @Query("SELECT r FROM ChatRoom r JOIN ChatRoomMember m ON m.room.id = r.id " +
            "WHERE m.user.id = :userId AND r.isActive = true ORDER BY r.updatedAt DESC")
    List<ChatRoom> findRoomsForUser(@Param("userId") Long userId);
}
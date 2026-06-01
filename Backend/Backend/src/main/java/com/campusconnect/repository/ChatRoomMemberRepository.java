package com.campusconnect.repository;

import com.campusconnect.entity.ChatRoomMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ChatRoomMemberRepository extends JpaRepository<ChatRoomMember, Long> {

    List<ChatRoomMember> findByRoomId(Long roomId);

    Optional<ChatRoomMember> findByRoomIdAndUserId(Long roomId, Long userId);

    boolean existsByRoomIdAndUserId(Long roomId, Long userId);

    @Query("SELECT m.user.id FROM ChatRoomMember m WHERE m.room.id = :roomId")
    List<Long> findUserIdsByRoomId(@Param("roomId") Long roomId);

    @Modifying
    @Query("DELETE FROM ChatRoomMember m WHERE m.room.id = :roomId AND m.user.id = :userId")
    void deleteByRoomIdAndUserId(@Param("roomId") Long roomId, @Param("userId") Long userId);

    @Modifying
    @Query("UPDATE ChatRoomMember m SET m.lastReadAt = :ts WHERE m.room.id = :roomId AND m.user.id = :userId")
    void updateLastRead(@Param("roomId") Long roomId, @Param("userId") Long userId, @Param("ts") LocalDateTime ts);
}
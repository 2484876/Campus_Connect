package com.campusconnect.repository;

import com.campusconnect.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    @Query("SELECT m FROM Message m WHERE m.chatRoomId IS NULL AND " +
            "((m.sender.id = :u1 AND m.receiver.id = :u2) OR " +
            "(m.sender.id = :u2 AND m.receiver.id = :u1)) " +
            "AND (m.hiddenFor IS NULL OR m.hiddenFor != :u1) " +
            "ORDER BY m.createdAt DESC")
    Page<Message> findConversation(@Param("u1") Long u1, @Param("u2") Long u2, Pageable pageable);

    @Query("SELECT m FROM Message m WHERE m.chatRoomId = :roomId " +
            "AND (m.hiddenFor IS NULL OR m.hiddenFor != :viewerId) " +
            "ORDER BY m.createdAt DESC")
    Page<Message> findRoomMessages(@Param("roomId") Long roomId, @Param("viewerId") Long viewerId, Pageable pageable);

    @Query("SELECT DISTINCT CASE WHEN m.sender.id = :uid THEN m.receiver.id ELSE m.sender.id END " +
            "FROM Message m WHERE m.chatRoomId IS NULL AND m.receiver IS NOT NULL " +
            "AND (m.sender.id = :uid OR m.receiver.id = :uid)")
    List<Long> findConversationPartnerIds(@Param("uid") Long userId);

    @Query("SELECT DISTINCT CASE WHEN m.sender.id = :uid THEN m.receiver.id ELSE m.sender.id END " +
            "FROM Message m WHERE m.chatRoomId IS NULL AND m.receiver IS NOT NULL " +
            "AND (m.sender.id = :uid OR m.receiver.id = :uid) AND m.createdAt >= :since")
    List<Long> findRecentChatPartnerIds(@Param("uid") Long userId, @Param("since") LocalDateTime since);

    @Query("SELECT m FROM Message m WHERE m.chatRoomId IS NULL AND " +
            "((m.sender.id = :u1 AND m.receiver.id = :u2) OR " +
            "(m.sender.id = :u2 AND m.receiver.id = :u1)) " +
            "AND (m.hiddenFor IS NULL OR m.hiddenFor != :u1) " +
            "ORDER BY m.createdAt DESC LIMIT 1")
    Message findLastMessage(@Param("u1") Long u1, @Param("u2") Long u2);

    @Query("SELECT m FROM Message m WHERE m.chatRoomId = :roomId ORDER BY m.createdAt DESC LIMIT 1")
    Message findLastRoomMessage(@Param("roomId") Long roomId);

    int countByReceiverIdAndSenderIdAndReadStatusFalse(Long receiverId, Long senderId);

    @Query("SELECT COUNT(m) FROM Message m WHERE m.chatRoomId = :roomId AND m.createdAt > :since AND m.sender.id <> :userId")
    int countRoomUnreadAfter(@Param("roomId") Long roomId, @Param("userId") Long userId, @Param("since") LocalDateTime since);

    @Query("SELECT m.id FROM Message m WHERE m.receiver.id = :receiverId " +
            "AND m.sender.id = :senderId AND m.readStatus = false")
    List<Long> findUnreadMessageIds(@Param("receiverId") Long receiverId, @Param("senderId") Long senderId);

    @Modifying
    @Query("UPDATE Message m SET m.readStatus = true, m.readAt = :readAt " +
            "WHERE m.receiver.id = :receiverId AND m.sender.id = :senderId AND m.readStatus = false")
    void markAsReadWithTimestamp(@Param("receiverId") Long receiverId,
                                 @Param("senderId") Long senderId,
                                 @Param("readAt") LocalDateTime readAt);

    @Query(value =
            "SELECT m.* FROM messages m " +
                    "WHERE m.deleted = 0 AND m.content LIKE CONCAT('%', :q, '%') AND ( " +
                    "  (m.chat_room_id IS NULL AND (m.sender_id = :userId OR m.receiver_id = :userId)) " +
                    "  OR m.chat_room_id IN (SELECT room_id FROM chat_room_members WHERE user_id = :userId) " +
                    ") " +
                    "ORDER BY m.created_at DESC LIMIT 50",
            nativeQuery = true)
    List<Message> searchMessages(@Param("userId") Long userId, @Param("q") String q);
}
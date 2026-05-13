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

    @Query("SELECT m FROM Message m WHERE " +
            "((m.sender.id = :u1 AND m.receiver.id = :u2) OR " +
            "(m.sender.id = :u2 AND m.receiver.id = :u1)) " +
            "AND (m.hiddenFor IS NULL OR m.hiddenFor != :u1) " +
            "ORDER BY m.createdAt DESC")
    Page<Message> findConversation(@Param("u1") Long u1, @Param("u2") Long u2, Pageable pageable);

    @Query("SELECT DISTINCT CASE WHEN m.sender.id = :uid THEN m.receiver.id ELSE m.sender.id END " +
            "FROM Message m WHERE m.sender.id = :uid OR m.receiver.id = :uid")
    List<Long> findConversationPartnerIds(@Param("uid") Long userId);

    @Query("SELECT DISTINCT CASE WHEN m.sender.id = :uid THEN m.receiver.id ELSE m.sender.id END " +
            "FROM Message m WHERE (m.sender.id = :uid OR m.receiver.id = :uid) AND m.createdAt >= :since")
    List<Long> findRecentChatPartnerIds(@Param("uid") Long userId, @Param("since") LocalDateTime since);

    @Query("SELECT m FROM Message m WHERE " +
            "((m.sender.id = :u1 AND m.receiver.id = :u2) OR " +
            "(m.sender.id = :u2 AND m.receiver.id = :u1)) " +
            "AND (m.hiddenFor IS NULL OR m.hiddenFor != :u1) " +
            "ORDER BY m.createdAt DESC LIMIT 1")
    Message findLastMessage(@Param("u1") Long u1, @Param("u2") Long u2);

    int countByReceiverIdAndSenderIdAndReadStatusFalse(Long receiverId, Long senderId);

    @Query("SELECT m.id FROM Message m WHERE m.receiver.id = :receiverId " +
            "AND m.sender.id = :senderId AND m.readStatus = false")
    List<Long> findUnreadMessageIds(@Param("receiverId") Long receiverId, @Param("senderId") Long senderId);

    @Modifying
    @Query("UPDATE Message m SET m.readStatus = true, m.readAt = :readAt " +
            "WHERE m.receiver.id = :receiverId AND m.sender.id = :senderId AND m.readStatus = false")
    void markAsReadWithTimestamp(@Param("receiverId") Long receiverId,
                                 @Param("senderId") Long senderId,
                                 @Param("readAt") LocalDateTime readAt);
}
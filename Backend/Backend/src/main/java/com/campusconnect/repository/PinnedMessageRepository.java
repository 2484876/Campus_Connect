package com.campusconnect.repository;

import com.campusconnect.entity.PinnedMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PinnedMessageRepository extends JpaRepository<PinnedMessage, Long> {

    @Query("SELECT p FROM PinnedMessage p WHERE p.chatKey = :chatKey ORDER BY p.pinnedAt DESC")
    List<PinnedMessage> findByChatKey(@Param("chatKey") String chatKey);

    long countByChatKey(String chatKey);

    Optional<PinnedMessage> findByMessageId(Long messageId);

    void deleteByMessageId(Long messageId);
}
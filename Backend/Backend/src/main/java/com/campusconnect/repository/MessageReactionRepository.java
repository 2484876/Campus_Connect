package com.campusconnect.repository;

import com.campusconnect.entity.MessageReaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface MessageReactionRepository extends JpaRepository<MessageReaction, Long> {

    List<MessageReaction> findByMessageId(Long messageId);

    Optional<MessageReaction> findByMessageIdAndUserIdAndEmoji(Long messageId, Long userId, String emoji);

    @Query("SELECT r FROM MessageReaction r WHERE r.message.id IN :messageIds")
    List<MessageReaction> findByMessageIdIn(@Param("messageIds") List<Long> messageIds);

    int countByMessageIdAndEmoji(Long messageId, String emoji);

    void deleteByMessageId(Long messageId);
}
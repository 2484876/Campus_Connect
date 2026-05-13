package com.campusconnect.repository;

import com.campusconnect.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    @Query("SELECT p FROM Post p WHERE p.isActive = true AND (p.user.id = :uid OR p.user.id IN (SELECT CASE WHEN c.sender.id = :uid THEN c.receiver.id ELSE c.sender.id END FROM Connection c WHERE (c.sender.id = :uid OR c.receiver.id = :uid) AND c.status = 'ACCEPTED')) ORDER BY p.createdAt DESC")
    Page<Post> findFeedForUser(@Param("uid") Long userId, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.isActive = true AND p.visibility = com.campusconnect.enums.Visibility.PUBLIC ORDER BY p.createdAt DESC")
    Page<Post> findPublicFeed(Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.user.id = :uid AND p.isActive = true ORDER BY p.createdAt DESC")
    Page<Post> findByUserId(@Param("uid") Long userId, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.isActive = true AND p.createdAt >= :since ORDER BY p.createdAt DESC")
    List<Post> findActiveSince(@Param("since") LocalDateTime since);

    @Query("SELECT p FROM Post p WHERE p.isActive = true ORDER BY p.createdAt DESC")
    List<Post> findAllActive();

    @Query("SELECT p FROM Post p WHERE p.isActive = true AND p.user.id IN :userIds AND p.createdAt >= :since ORDER BY p.createdAt DESC")
    List<Post> findActiveByUserIdsSince(@Param("userIds") Collection<Long> userIds, @Param("since") LocalDateTime since);

    @Query(value =
            "SELECT DISTINCT LOWER(h.tag) " +
                    "FROM hashtags h " +
                    "JOIN post_hashtags ph ON ph.hashtag_id = h.id " +
                    "JOIN posts p ON p.id = ph.post_id " +
                    "LEFT JOIN likes l ON l.post_id = p.id AND l.user_id = :userId " +
                    "LEFT JOIN comments c ON c.post_id = p.id AND c.user_id = :userId AND c.is_active = 1 " +
                    "WHERE (l.id IS NOT NULL OR c.id IS NOT NULL) " +
                    "AND p.created_at >= :since",
            nativeQuery = true)
    List<String> findHashtagsViewerEngagedWith(@Param("userId") Long userId, @Param("since") LocalDateTime since);

    @Query(value = "SELECT LOWER(h.tag) FROM hashtags h JOIN post_hashtags ph ON ph.hashtag_id = h.id WHERE ph.post_id = :postId", nativeQuery = true)
    List<String> findHashtagsByPostId(@Param("postId") Long postId);
}
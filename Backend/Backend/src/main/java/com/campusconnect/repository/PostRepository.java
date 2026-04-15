package com.campusconnect.repository;
import com.campusconnect.entity.Post;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
public interface PostRepository extends JpaRepository<Post, Long> {
    @Query("SELECT p FROM Post p WHERE p.isActive = true AND (p.user.id = :uid OR p.user.id IN (SELECT CASE WHEN c.sender.id = :uid THEN c.receiver.id ELSE c.sender.id END FROM Connection c WHERE (c.sender.id = :uid OR c.receiver.id = :uid) AND c.status = 'ACCEPTED')) ORDER BY p.createdAt DESC")
    Page<Post> findFeedForUser(@Param("uid") Long userId, Pageable pageable);
    @Query("SELECT p FROM Post p WHERE p.isActive = true AND p.visibility = com.campusconnect.enums.Visibility.PUBLIC ORDER BY p.createdAt DESC")
    Page<Post> findPublicFeed(Pageable pageable);
    @Query("SELECT p FROM Post p WHERE p.user.id = :uid AND p.isActive = true ORDER BY p.createdAt DESC")
    Page<Post> findByUserId(@Param("uid") Long userId, Pageable pageable);
}

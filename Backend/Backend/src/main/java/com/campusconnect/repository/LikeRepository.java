package com.campusconnect.repository;
import com.campusconnect.entity.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface LikeRepository extends JpaRepository<Like, Long> {
    Optional<Like> findByPostIdAndUserId(Long postId, Long userId);
    int countByPostId(Long postId);
    boolean existsByPostIdAndUserId(Long postId, Long userId);
}
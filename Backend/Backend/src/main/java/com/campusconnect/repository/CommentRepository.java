package com.campusconnect.repository;
import com.campusconnect.entity.Comment;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
public interface CommentRepository extends JpaRepository<Comment, Long> {
    @Query("SELECT c FROM Comment c WHERE c.post.id = :pid AND c.isActive = true ORDER BY c.createdAt DESC")
    Page<Comment> findByPostId(@Param("pid") Long postId, Pageable pageable);
    @Query("SELECT c FROM Comment c WHERE c.post.id = :pid AND c.isActive = true ORDER BY c.createdAt DESC")
    List<Comment> findTop3ByPostId(@Param("pid") Long postId, Pageable pageable);
    int countByPostIdAndIsActiveTrue(Long postId);
}
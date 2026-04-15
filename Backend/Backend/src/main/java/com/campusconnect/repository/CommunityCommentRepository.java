package com.campusconnect.repository;

import com.campusconnect.entity.CommunityComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface CommunityCommentRepository extends JpaRepository<CommunityComment, Long> {
    @Query("SELECT c FROM CommunityComment c WHERE c.post.id = :pid AND c.parentComment IS NULL AND c.isActive = true ORDER BY c.createdAt DESC")
    Page<CommunityComment> findTopLevelByPostId(@Param("pid") Long postId, Pageable pageable);

    @Query("SELECT c FROM CommunityComment c WHERE c.parentComment.id = :pid AND c.isActive = true ORDER BY c.createdAt ASC")
    List<CommunityComment> findReplies(@Param("pid") Long parentId);

    int countByPostIdAndIsActiveTrue(Long postId);
    int countByParentCommentIdAndIsActiveTrue(Long parentCommentId);
}
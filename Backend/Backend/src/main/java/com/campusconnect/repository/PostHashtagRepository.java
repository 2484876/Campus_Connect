package com.campusconnect.repository;

import com.campusconnect.entity.PostHashtag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostHashtagRepository extends JpaRepository<PostHashtag, Long> {

    List<PostHashtag> findByPostId(Long postId);

    boolean existsByPostIdAndHashtagId(Long postId, Long hashtagId);

    @Modifying
    @Query("DELETE FROM PostHashtag ph WHERE ph.post.id = :postId")
    void deleteByPostId(@Param("postId") Long postId);

    @Query("SELECT ph.post.id FROM PostHashtag ph WHERE ph.hashtag.id = :hashtagId ORDER BY ph.post.createdAt DESC")
    List<Long> findPostIdsByHashtagId(@Param("hashtagId") Long hashtagId);
}
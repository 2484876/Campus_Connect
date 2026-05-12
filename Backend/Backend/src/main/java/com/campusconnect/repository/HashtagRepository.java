package com.campusconnect.repository;

import com.campusconnect.entity.Hashtag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface HashtagRepository extends JpaRepository<Hashtag, Long> {
    Optional<Hashtag> findByTag(String tag);

    @Query("SELECT h FROM Hashtag h WHERE h.lastUsed >= :since ORDER BY h.usageCount DESC")
    List<Hashtag> findTrending(LocalDateTime since, Pageable pageable);

    @Query("SELECT h FROM Hashtag h WHERE LOWER(h.tag) LIKE LOWER(CONCAT('%', :q, '%')) ORDER BY h.usageCount DESC")
    Page<Hashtag> searchByTag(String q, Pageable pageable);
}
package com.campusconnect.service;

import com.campusconnect.dto.HashtagDTO;
import com.campusconnect.entity.Hashtag;
import com.campusconnect.repository.HashtagRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HashtagService {

    private final HashtagRepository hashtagRepository;

    @PersistenceContext
    private EntityManager em;

    private static final Pattern TAG_PATTERN = Pattern.compile("#([A-Za-z0-9_]{2,50})");

    public Set<String> extractTags(String text) {
        Set<String> tags = new HashSet<>();
        if (text == null) return tags;
        Matcher m = TAG_PATTERN.matcher(text);
        while (m.find()) tags.add(m.group(1).toLowerCase());
        return tags;
    }

    @Transactional
    public void processPostTags(Long postId, String content) {
        em.createNativeQuery("DELETE FROM post_hashtags WHERE post_id = ?1")
                .setParameter(1, postId).executeUpdate();

        Set<String> tags = extractTags(content);
        if (tags.isEmpty()) return;

        for (String tag : tags) {
            Hashtag h = hashtagRepository.findByTag(tag).orElseGet(() -> {
                Hashtag nh = Hashtag.builder().tag(tag).usageCount(0L).build();
                return hashtagRepository.save(nh);
            });
            h.setUsageCount(h.getUsageCount() + 1);
            hashtagRepository.save(h);

            em.createNativeQuery("INSERT INTO post_hashtags (post_id, hashtag_id) VALUES (?1, ?2)")
                    .setParameter(1, postId)
                    .setParameter(2, h.getId())
                    .executeUpdate();
        }
    }

    public List<HashtagDTO> getTrending(int limit) {
        LocalDateTime since = LocalDateTime.now().minusDays(14);
        return hashtagRepository.findTrending(since, PageRequest.of(0, limit))
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<HashtagDTO> search(String q, int limit) {
        return hashtagRepository.searchByTag(q, PageRequest.of(0, limit))
                .map(this::toDTO).getContent();
    }

    @SuppressWarnings("unchecked")
    public List<Long> findPostIdsByTag(String tag) {
        List<Number> result = em.createNativeQuery(
                        "SELECT ph.post_id FROM post_hashtags ph JOIN hashtags h ON h.id = ph.hashtag_id WHERE h.tag = ?1 ORDER BY ph.post_id DESC")
                .setParameter(1, tag.toLowerCase())
                .getResultList();
        return result.stream().map(Number::longValue).collect(Collectors.toList());
    }

    private HashtagDTO toDTO(Hashtag h) {
        return HashtagDTO.builder().id(h.getId()).tag(h.getTag()).usageCount(h.getUsageCount()).build();
    }
}
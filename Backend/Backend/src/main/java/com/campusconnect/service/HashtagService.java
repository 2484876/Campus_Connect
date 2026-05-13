package com.campusconnect.service;

import com.campusconnect.dto.HashtagDTO;
import com.campusconnect.dto.PostDTO;
import com.campusconnect.entity.Hashtag;
import com.campusconnect.entity.Post;
import com.campusconnect.entity.PostHashtag;
import com.campusconnect.repository.HashtagRepository;
import com.campusconnect.repository.PostHashtagRepository;
import com.campusconnect.repository.PostRepository;
import lombok.RequiredArgsConstructor;
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

    private static final Pattern HASHTAG_PATTERN = Pattern.compile("#([A-Za-z0-9_]{2,50})");

    private final HashtagRepository hashtagRepository;
    private final PostHashtagRepository postHashtagRepository;
    private final PostRepository postRepository;
    private final FeedService feedService;

    public Set<String> extractTags(String content) {
        Set<String> tags = new LinkedHashSet<>();
        if (content == null || content.isBlank()) return tags;
        Matcher m = HASHTAG_PATTERN.matcher(content);
        while (m.find()) {
            tags.add(m.group(1).toLowerCase());
        }
        return tags;
    }

    @Transactional
    public void linkPostToHashtags(Post post) {
        if (post == null) return;
        Set<String> tags = extractTags(post.getContent());
        if (tags.isEmpty()) return;

        LocalDateTime now = LocalDateTime.now();
        for (String tag : tags) {
            Hashtag h = hashtagRepository.findByTag(tag).orElseGet(() -> {
                Hashtag fresh = Hashtag.builder()
                        .tag(tag)
                        .usageCount(0)
                        .lastUsed(now)
                        .build();
                return hashtagRepository.save(fresh);
            });

            if (!postHashtagRepository.existsByPostIdAndHashtagId(post.getId(), h.getId())) {
                postHashtagRepository.save(PostHashtag.builder()
                        .post(post)
                        .hashtag(h)
                        .build());
                h.setUsageCount(h.getUsageCount() + 1);
                h.setLastUsed(now);
                hashtagRepository.save(h);
            }
        }
    }

    @Transactional
    public void relinkPostHashtags(Post post) {
        postHashtagRepository.deleteByPostId(post.getId());
        linkPostToHashtags(post);
    }

    public List<HashtagDTO> trending(int limit) {
        List<Hashtag> all = hashtagRepository.findTopTrending();
        return all.stream()
                .limit(limit)
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<HashtagDTO> search(String q, int limit) {
        if (q == null || q.isBlank()) return List.of();
        String clean = q.startsWith("#") ? q.substring(1) : q;
        return hashtagRepository.searchByPrefix(clean.toLowerCase()).stream()
                .limit(limit)
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<PostDTO> postsByTag(String tag, Long currentUserId) {
        if (tag == null) return List.of();
        String clean = tag.startsWith("#") ? tag.substring(1) : tag;
        Optional<Hashtag> hOpt = hashtagRepository.findByTag(clean.toLowerCase());
        if (hOpt.isEmpty()) return List.of();

        List<Long> postIds = postHashtagRepository.findPostIdsByHashtagId(hOpt.get().getId());
        if (postIds.isEmpty()) return List.of();

        List<Post> posts = postRepository.findAllById(postIds);
        posts.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));

        return posts.stream()
                .filter(Post::isActive)
                .map(p -> feedService.mapToDTO(p, currentUserId))
                .collect(Collectors.toList());
    }

    private HashtagDTO toDTO(Hashtag h) {
        return HashtagDTO.builder()
                .id(h.getId())
                .tag(h.getTag())
                .usageCount(h.getUsageCount())
                .build();
    }
}
package com.campusconnect.service;

import com.campusconnect.entity.Post;
import com.campusconnect.entity.User;
import com.campusconnect.repository.CommentRepository;
import com.campusconnect.repository.ConnectionRepository;
import com.campusconnect.repository.LikeRepository;
import com.campusconnect.repository.MessageRepository;
import com.campusconnect.repository.PostRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class FeedScoringService {

    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;
    private final ConnectionRepository connectionRepository;
    private final MessageRepository messageRepository;
    private final PostRepository postRepository;

    public FeedScoringService(LikeRepository likeRepository,
                              CommentRepository commentRepository,
                              ConnectionRepository connectionRepository,
                              MessageRepository messageRepository,
                              PostRepository postRepository) {
        this.likeRepository = likeRepository;
        this.commentRepository = commentRepository;
        this.connectionRepository = connectionRepository;
        this.messageRepository = messageRepository;
        this.postRepository = postRepository;
    }

    public static class ScoringContext {
        public Long viewerId;
        public String viewerDepartment;
        public String viewerRole;
        public Set<Long> connectionIds = new HashSet<>();
        public Set<Long> secondDegreeIds = new HashSet<>();
        public Set<Long> messagedUserIds = new HashSet<>();
        public Set<String> affinityHashtags = new HashSet<>();
    }

    public ScoringContext buildContext(User viewer) {
        ScoringContext ctx = new ScoringContext();
        ctx.viewerId = viewer.getId();
        ctx.viewerDepartment = viewer.getDepartment();
        ctx.viewerRole = viewer.getRole() != null ? viewer.getRole().name() : null;

        try {
            ctx.connectionIds = new HashSet<>(connectionRepository.findConnectedUserIds(viewer.getId()));
        } catch (Exception ignored) {}

        if (!ctx.connectionIds.isEmpty()) {
            Set<Long> second = new HashSet<>();
            for (Long cid : ctx.connectionIds) {
                try {
                    second.addAll(connectionRepository.findConnectedUserIds(cid));
                } catch (Exception ignored) {}
            }
            second.removeAll(ctx.connectionIds);
            second.remove(viewer.getId());
            ctx.secondDegreeIds = second;
        }

        LocalDateTime since30 = LocalDateTime.now().minusDays(30);
        try {
            ctx.messagedUserIds = new HashSet<>(messageRepository.findRecentChatPartnerIds(viewer.getId(), since30));
        } catch (Exception ignored) {}

        try {
            ctx.affinityHashtags = new HashSet<>(postRepository.findHashtagsViewerEngagedWith(viewer.getId(), since30));
        } catch (Exception ignored) {}

        return ctx;
    }

    public double scorePost(Post post, ScoringContext ctx) {
        if (post == null || ctx == null) return 0;

        double score = 100.0;

        long likes = 0, comments = 0;
        try { likes = likeRepository.countByPostId(post.getId()); } catch (Exception ignored) {}
        try { comments = commentRepository.countByPostIdAndIsActiveTrue(post.getId()); } catch (Exception ignored) {}
        score += likes * 3.0;
        score += comments * 5.0;

        Long authorId = post.getUser() != null ? post.getUser().getId() : null;
        if (authorId != null) {
            if (authorId.equals(ctx.viewerId)) {
                score += 50;
            } else if (ctx.connectionIds.contains(authorId)) {
                score += 200;
            } else if (ctx.secondDegreeIds.contains(authorId)) {
                score += 80;
            }

            if (ctx.messagedUserIds.contains(authorId)) {
                score += 50;
            }
        }

        User author = post.getUser();
        if (author != null) {
            if (ctx.viewerDepartment != null && ctx.viewerDepartment.equalsIgnoreCase(author.getDepartment())) {
                score += 60;
            }
            if (ctx.viewerRole != null && author.getRole() != null && ctx.viewerRole.equals(author.getRole().name())) {
                score += 40;
            }
        }

        if (!ctx.affinityHashtags.isEmpty()) {
            try {
                List<String> postTags = postRepository.findHashtagsByPostId(post.getId());
                if (postTags != null) {
                    int matches = 0;
                    for (String t : postTags) {
                        if (t != null && ctx.affinityHashtags.contains(t.toLowerCase())) {
                            matches++;
                        }
                    }
                    score += matches * 30.0;
                }
            } catch (Exception ignored) {}
        }

        if (post.getCreatedAt() != null) {
            long hours = Math.max(0, Duration.between(post.getCreatedAt(), LocalDateTime.now()).toHours());
            double decay = Math.min(hours * 2.0, 150.0);
            score -= decay;
        }

        return Math.max(0, score);
    }
}
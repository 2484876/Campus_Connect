package com.campusconnect.service;

import com.campusconnect.dto.*;
import com.campusconnect.entity.*;
import com.campusconnect.enums.*;
import com.campusconnect.exception.*;
import com.campusconnect.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FeedService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;
    private final NotificationService notificationService;
    private final FeedScoringService scoringService;
    private final ConnectionRepository connectionRepository;

    @Autowired(required = false)
    private com.campusconnect.repository.BookmarkRepository bookmarkRepository;

    @Autowired(required = false)
    @org.springframework.context.annotation.Lazy
    private HashtagService hashtagService;

    private static final int RANKING_WINDOW_DAYS = 180;
    private static final int RANKING_POOL_LIMIT = 1000;
    private static final int RANKING_MIN_POOL = 20;

    public Page<PostDTO> getFeed(Long userId, int page, int size, String mode) {
        String m = mode == null ? "ALL" : mode.toUpperCase();
        if ("FOR_YOU".equals(m)) {
            return getForYouFeed(userId, page, size);
        }
        return getAllRankedFeed(userId, page, size);
    }

    public Page<PostDTO> getFeed(Long userId, int page, int size) {
        return getFeed(userId, page, size, "ALL");
    }

    private Page<PostDTO> getAllRankedFeed(Long userId, int page, int size) {
        User viewer = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        FeedScoringService.ScoringContext ctx = scoringService.buildContext(viewer);

        LocalDateTime since = LocalDateTime.now().minusDays(RANKING_WINDOW_DAYS);
        List<Post> pool = postRepository.findActiveSince(since);
        if (pool.size() < RANKING_MIN_POOL) {
            pool = postRepository.findAllActive();
        }
        if (pool.size() > RANKING_POOL_LIMIT) {
            pool = pool.subList(0, RANKING_POOL_LIMIT);
        }

        List<Scored> connectionPool = new ArrayList<>();
        List<Scored> deptRolePool = new ArrayList<>();
        List<Scored> globalPool = new ArrayList<>();

        for (Post p : pool) {
            User author = p.getUser();
            if (author == null) continue;
            double score = scoringService.scorePost(p, ctx);
            Scored s = new Scored(p, score);

            Long aid = author.getId();
            boolean isConnection = ctx.connectionIds.contains(aid);
            boolean sameDept = ctx.viewerDepartment != null && ctx.viewerDepartment.equalsIgnoreCase(author.getDepartment());
            boolean sameRole = ctx.viewerRole != null && author.getRole() != null && ctx.viewerRole.equals(author.getRole().name());

            if (isConnection) {
                connectionPool.add(s);
            } else if (sameDept || sameRole) {
                deptRolePool.add(s);
            } else {
                globalPool.add(s);
            }
        }

        Comparator<Scored> byScore = (a, b) -> Double.compare(b.score, a.score);
        connectionPool.sort(byScore);
        deptRolePool.sort(byScore);
        globalPool.sort(byScore);

        List<Post> ranked = interleave(connectionPool, deptRolePool, globalPool, 0.40, 0.30, 0.30);

        return paginate(ranked, userId, page, size);
    }

    private Page<PostDTO> getForYouFeed(Long userId, int page, int size) {
        User viewer = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        FeedScoringService.ScoringContext ctx = scoringService.buildContext(viewer);

        Set<Long> personalIds = new HashSet<>();
        personalIds.add(userId);
        personalIds.addAll(ctx.connectionIds);
        personalIds.addAll(ctx.secondDegreeIds);
        personalIds.addAll(ctx.messagedUserIds);

        LocalDateTime since = LocalDateTime.now().minusDays(RANKING_WINDOW_DAYS);
        List<Post> pool;
        if (personalIds.isEmpty()) {
            pool = postRepository.findActiveSince(since);
            if (pool.size() < RANKING_MIN_POOL) {
                pool = postRepository.findAllActive();
            }
        } else {
            pool = postRepository.findActiveByUserIdsSince(personalIds, since);
            if (pool.size() < RANKING_MIN_POOL) {
                pool = postRepository.findAllActive();
            }
        }
        if (pool.size() > RANKING_POOL_LIMIT) {
            pool = pool.subList(0, RANKING_POOL_LIMIT);
        }

        List<Scored> scored = new ArrayList<>();
        for (Post p : pool) {
            scored.add(new Scored(p, scoringService.scorePost(p, ctx)));
        }
        scored.sort((a, b) -> Double.compare(b.score, a.score));

        List<Post> ranked = scored.stream().map(s -> s.post).collect(Collectors.toList());
        return paginate(ranked, userId, page, size);
    }

    private Page<PostDTO> paginate(List<Post> ranked, Long userId, int page, int size) {
        int total = ranked.size();
        int from = Math.min(page * size, total);
        int to = Math.min(from + size, total);
        List<Post> slice = ranked.subList(from, to);
        List<PostDTO> dtos = slice.stream().map(p -> mapToDTO(p, userId)).collect(Collectors.toList());
        return new PageImpl<>(dtos, PageRequest.of(page, size), total);
    }

    private List<Post> interleave(List<Scored> a, List<Scored> b, List<Scored> c,
                                  double wa, double wb, double wc) {
        List<Post> out = new ArrayList<>();
        int ia = 0, ib = 0, ic = 0;
        Set<Long> seen = new HashSet<>();
        double da = 0, db = 0, dc = 0;

        while (ia < a.size() || ib < b.size() || ic < c.size()) {
            da += wa;
            db += wb;
            dc += wc;

            boolean took = false;
            if (da >= 1 && ia < a.size()) {
                Post p = a.get(ia++).post;
                if (seen.add(p.getId())) { out.add(p); took = true; }
                da -= 1;
            }
            if (db >= 1 && ib < b.size()) {
                Post p = b.get(ib++).post;
                if (seen.add(p.getId())) { out.add(p); took = true; }
                db -= 1;
            }
            if (dc >= 1 && ic < c.size()) {
                Post p = c.get(ic++).post;
                if (seen.add(p.getId())) { out.add(p); took = true; }
                dc -= 1;
            }

            if (!took) {
                if (ia < a.size()) { Post p = a.get(ia++).post; if (seen.add(p.getId())) out.add(p); }
                else if (ib < b.size()) { Post p = b.get(ib++).post; if (seen.add(p.getId())) out.add(p); }
                else if (ic < c.size()) { Post p = c.get(ic++).post; if (seen.add(p.getId())) out.add(p); }
                else break;
            }
        }
        return out;
    }

    public Page<PostDTO> getPublicFeed(int page, int size) {
        return postRepository.findPublicFeed(PageRequest.of(page, size))
                .map(post -> mapToDTO(post, null));
    }

    public Page<PostDTO> getUserPosts(Long userId, Long currentId, int page, int size) {
        return postRepository.findByUserId(userId, PageRequest.of(page, size))
                .map(post -> mapToDTO(post, currentId));
    }

    public PostDTO getPostById(Long postId, Long currentId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));
        return mapToDTO(post, currentId);
    }

    @Transactional
    public PostDTO createPost(Long userId, CreatePostRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Post post = Post.builder()
                .user(user).content(req.getContent()).imageUrl(req.getImageUrl()).videoUrl(req.getVideoUrl())
                .postType(req.getPostType() != null ? PostType.valueOf(req.getPostType()) : PostType.GENERAL)
                .isActive(true).build();
        Post saved = postRepository.save(post);
        if (hashtagService != null) {
            try { hashtagService.linkPostToHashtags(saved); } catch (Exception ignored) {}
        }
        return mapToDTO(saved, userId);
    }

    @Transactional
    public void deletePost(Long userId, Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));
        if (!post.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("Cannot delete another user's post");
        }
        post.setActive(false);
        postRepository.save(post);
    }

    @Transactional
    public Map<String, Object> toggleLike(Long userId, Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        var existing = likeRepository.findByPostIdAndUserId(postId, userId);
        if (existing.isPresent()) {
            likeRepository.delete(existing.get());
            return Map.of("liked", false, "likeCount", likeRepository.countByPostId(postId));
        }
        likeRepository.save(com.campusconnect.entity.Like.builder().post(post).user(user).build());
        if (!post.getUser().getId().equals(userId)) {
            notificationService.createNotification(post.getUser().getId(), userId, NotificationType.LIKE, postId);
        }
        return Map.of("liked", true, "likeCount", likeRepository.countByPostId(postId));
    }

    @Transactional
    public CommentDTO addComment(Long userId, Long postId, String content) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Comment comment = commentRepository.save(
                Comment.builder().post(post).user(user).content(content).isActive(true).build());
        if (!post.getUser().getId().equals(userId)) {
            notificationService.createNotification(post.getUser().getId(), userId, NotificationType.COMMENT, postId);
        }
        return CommentDTO.builder()
                .id(comment.getId()).content(comment.getContent())
                .authorId(user.getId()).authorName(user.getName())
                .authorProfilePic(user.getProfilePicUrl())
                .createdAt(comment.getCreatedAt()).build();
    }

    public Page<CommentDTO> getComments(Long postId, int page, int size) {
        return commentRepository.findByPostId(postId, PageRequest.of(page, size))
                .map(c -> CommentDTO.builder()
                        .id(c.getId()).content(c.getContent())
                        .authorId(c.getUser().getId()).authorName(c.getUser().getName())
                        .authorProfilePic(c.getUser().getProfilePicUrl())
                        .createdAt(c.getCreatedAt()).build());
    }

    public PostDTO mapToDTO(Post post, Long currentUserId) {
        User author = post.getUser();
        int likeCount = likeRepository.countByPostId(post.getId());
        int commentCount = commentRepository.countByPostIdAndIsActiveTrue(post.getId());
        boolean likedByMe = currentUserId != null && likeRepository.existsByPostIdAndUserId(post.getId(), currentUserId);

        boolean bookmarkedByMe = false;
        if (currentUserId != null && bookmarkRepository != null) {
            try {
                bookmarkedByMe = bookmarkRepository.existsByUserIdAndPostId(currentUserId, post.getId());
            } catch (Exception ignored) {}
        }

        List<CommentDTO> recentComments = commentRepository.findTop3ByPostId(post.getId(), PageRequest.of(0, 3))
                .stream()
                .map(c -> CommentDTO.builder()
                        .id(c.getId()).content(c.getContent())
                        .authorId(c.getUser().getId()).authorName(c.getUser().getName())
                        .authorProfilePic(c.getUser().getProfilePicUrl())
                        .createdAt(c.getCreatedAt()).build())
                .collect(Collectors.toList());

        return PostDTO.builder()
                .id(post.getId()).content(post.getContent()).imageUrl(post.getImageUrl())
                .videoUrl(post.getVideoUrl())
                .postType(post.getPostType().name()).createdAt(post.getCreatedAt())
                .authorId(author.getId()).authorName(author.getName())
                .authorProfilePic(author.getProfilePicUrl()).authorPosition(author.getPosition())
                .authorRole(author.getRole().name()).authorDepartment(author.getDepartment())
                .likeCount(likeCount).commentCount(commentCount).likedByMe(likedByMe)
                .bookmarkedByMe(bookmarkedByMe)
                .recentComments(recentComments).build();
    }

    private static class Scored {
        final Post post;
        final double score;
        Scored(Post post, double score) { this.post = post; this.score = score; }
    }
}
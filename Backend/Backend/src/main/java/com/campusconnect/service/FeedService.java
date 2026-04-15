package com.campusconnect.service;

import com.campusconnect.dto.*;
import com.campusconnect.entity.*;
import com.campusconnect.enums.*;
import com.campusconnect.exception.*;
import com.campusconnect.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

    public Page<PostDTO> getFeed(Long userId, int page, int size) {
        return postRepository.findFeedForUser(userId, PageRequest.of(page, size))
                .map(post -> mapToDTO(post, userId));
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
        return mapToDTO(postRepository.save(post), userId);
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

    private PostDTO mapToDTO(Post post, Long currentUserId) {
        User author = post.getUser();
        int likeCount = likeRepository.countByPostId(post.getId());
        int commentCount = commentRepository.countByPostIdAndIsActiveTrue(post.getId());
        boolean likedByMe = currentUserId != null && likeRepository.existsByPostIdAndUserId(post.getId(), currentUserId);

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
                .recentComments(recentComments).build();
    }
}
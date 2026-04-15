package com.campusconnect.service;

import com.campusconnect.dto.*;
import com.campusconnect.entity.*;
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
public class CommunityService {

    private final CommunityRepository communityRepository;
    private final CommunityMemberRepository memberRepository;
    private final CommunityPostRepository postRepository;
    private final CommunityCommentRepository commentRepository;
    private final CommunityVoteRepository voteRepository;
    private final UserRepository userRepository;
    private final ChatService chatService;

    @Transactional
    public CommunityDTO createCommunity(Long userId, CreateCommunityRequest req) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Community community = Community.builder()
                .name(req.getName()).description(req.getDescription())
                .iconUrl(req.getIconUrl()).bannerUrl(req.getBannerUrl())
                .createdBy(user).isPrivate(req.isPrivate())
                .memberCount(1).isActive(true).build();
        Community saved = communityRepository.save(community);
        memberRepository.save(CommunityMember.builder().community(saved).user(user).role("OWNER").build());
        return mapCommunityDTO(saved, userId);
    }

    @Transactional
    public CommunityDTO updateCommunity(Long communityId, Long userId, CreateCommunityRequest req) {
        Community c = communityRepository.findById(communityId).orElseThrow(() -> new ResourceNotFoundException("Community not found"));
        assertRole(communityId, userId, "OWNER", "ADMIN");
        if (req.getName() != null) c.setName(req.getName());
        if (req.getDescription() != null) c.setDescription(req.getDescription());
        c.setPrivate(req.isPrivate());
        communityRepository.save(c);
        return mapCommunityDTO(c, userId);
    }

    @Transactional
    public void deleteCommunity(Long communityId, Long userId) {
        Community c = communityRepository.findById(communityId).orElseThrow(() -> new ResourceNotFoundException("Community not found"));
        assertRole(communityId, userId, "OWNER");
        c.setActive(false);
        communityRepository.save(c);
    }

    @Transactional
    public CommunityDTO updateIcon(Long communityId, Long userId, String url) {
        Community c = communityRepository.findById(communityId).orElseThrow(() -> new ResourceNotFoundException("Community not found"));
        assertRole(communityId, userId, "OWNER", "ADMIN");
        c.setIconUrl(url);
        communityRepository.save(c);
        return mapCommunityDTO(c, userId);
    }

    @Transactional
    public CommunityDTO updateBanner(Long communityId, Long userId, String url) {
        Community c = communityRepository.findById(communityId).orElseThrow(() -> new ResourceNotFoundException("Community not found"));
        assertRole(communityId, userId, "OWNER", "ADMIN");
        c.setBannerUrl(url);
        communityRepository.save(c);
        return mapCommunityDTO(c, userId);
    }

    public Page<CommunityDTO> getAllCommunities(Long userId, int page, int size) {
        return communityRepository.findAllActive(PageRequest.of(page, size)).map(c -> mapCommunityDTO(c, userId));
    }

    public Page<CommunityDTO> getMyCommunities(Long userId, int page, int size) {
        return communityRepository.findByMember(userId, PageRequest.of(page, size)).map(c -> mapCommunityDTO(c, userId));
    }

    public Page<CommunityDTO> searchCommunities(String query, Long userId, int page, int size) {
        return communityRepository.search(query, PageRequest.of(page, size)).map(c -> mapCommunityDTO(c, userId));
    }

    public CommunityDTO getCommunityById(Long communityId, Long userId) {
        Community c = communityRepository.findById(communityId).orElseThrow(() -> new ResourceNotFoundException("Community not found"));
        return mapCommunityDTO(c, userId);
    }

    @Transactional
    public CommunityDTO joinCommunity(Long communityId, Long userId) {
        Community community = communityRepository.findById(communityId).orElseThrow(() -> new ResourceNotFoundException("Community not found"));
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (memberRepository.existsByCommunityIdAndUserId(communityId, userId)) throw new BadRequestException("Already a member");
        memberRepository.save(CommunityMember.builder().community(community).user(user).role("MEMBER").build());
        community.setMemberCount(community.getMemberCount() + 1);
        communityRepository.save(community);
        return mapCommunityDTO(community, userId);
    }

    @Transactional
    public void leaveCommunity(Long communityId, Long userId) {
        var member = memberRepository.findByCommunityIdAndUserId(communityId, userId).orElseThrow(() -> new BadRequestException("Not a member"));
        if ("OWNER".equals(member.getRole())) throw new BadRequestException("Owner cannot leave. Transfer ownership or delete the community.");
        memberRepository.delete(member);
        Community c = communityRepository.findById(communityId).orElseThrow();
        c.setMemberCount(Math.max(0, c.getMemberCount() - 1));
        communityRepository.save(c);
    }

    public Page<UserDTO> getMembers(Long communityId, int page, int size) {
        return memberRepository.findByCommunityId(communityId, PageRequest.of(page, size))
                .map(cm -> UserDTO.builder().id(cm.getUser().getId()).name(cm.getUser().getName())
                        .profilePicUrl(cm.getUser().getProfilePicUrl()).role(cm.getRole())
                        .position(cm.getUser().getPosition()).department(cm.getUser().getDepartment()).build());
    }

    @Transactional
    public void updateMemberRole(Long communityId, Long requesterId, Long targetUserId, String newRole) {
        assertRole(communityId, requesterId, "OWNER");
        var member = memberRepository.findByCommunityIdAndUserId(communityId, targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found"));
        if ("OWNER".equals(member.getRole())) throw new BadRequestException("Cannot change owner role");
        member.setRole(newRole);
        memberRepository.save(member);
    }

    @Transactional
    public void removeMember(Long communityId, Long requesterId, Long targetUserId) {
        assertRole(communityId, requesterId, "OWNER", "ADMIN");
        var member = memberRepository.findByCommunityIdAndUserId(communityId, targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found"));
        if ("OWNER".equals(member.getRole())) throw new BadRequestException("Cannot remove owner");
        memberRepository.delete(member);
        Community c = communityRepository.findById(communityId).orElseThrow();
        c.setMemberCount(Math.max(0, c.getMemberCount() - 1));
        communityRepository.save(c);
    }

    @Transactional
    public CommunityPostDTO createPost(Long communityId, Long userId, CreateCommunityPostRequest req) {
        Community community = communityRepository.findById(communityId).orElseThrow(() -> new ResourceNotFoundException("Community not found"));
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (!memberRepository.existsByCommunityIdAndUserId(communityId, userId)) throw new BadRequestException("Must be a member to post");
        CommunityPost post = CommunityPost.builder()
                .community(community).user(user).content(req.getContent())
                .imageUrl(req.getImageUrl()).videoUrl(req.getVideoUrl())
                .upvotes(0).downvotes(0).isActive(true).build();
        return mapPostDTO(postRepository.save(post), userId);
    }

    @Transactional
    public void deletePost(Long postId, Long userId) {
        CommunityPost post = postRepository.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Post not found"));
        boolean isAuthor = post.getUser().getId().equals(userId);
        boolean isAdminOrOwner = false;
        var member = memberRepository.findByCommunityIdAndUserId(post.getCommunity().getId(), userId);
        if (member.isPresent()) {
            String role = member.get().getRole();
            isAdminOrOwner = "OWNER".equals(role) || "ADMIN".equals(role);
        }
        if (!isAuthor && !isAdminOrOwner) throw new UnauthorizedException("Not authorized to delete this post");
        post.setActive(false);
        postRepository.save(post);
    }

    public Page<CommunityPostDTO> getCommunityPosts(Long communityId, Long userId, int page, int size) {
        return postRepository.findByCommunityId(communityId, PageRequest.of(page, size)).map(p -> mapPostDTO(p, userId));
    }

    public Page<CommunityPostDTO> getCommunityFeed(Long userId, int page, int size) {
        return postRepository.findFeedForUser(userId, PageRequest.of(page, size)).map(p -> mapPostDTO(p, userId));
    }

    public CommunityPostDTO getPostById(Long postId, Long userId) {
        CommunityPost post = postRepository.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Post not found"));
        return mapPostDTO(post, userId);
    }

    @Transactional
    public Map<String, Object> votePost(Long postId, Long userId, int value) {
        CommunityPost post = postRepository.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Post not found"));
        User user = userRepository.findById(userId).orElseThrow();
        var existing = voteRepository.findByUserIdAndPostIdAndCommentIdIsNull(userId, postId);
        if (existing.isPresent()) {
            CommunityVote vote = existing.get();
            if (vote.getValue() == value) {
                if (value == 1) post.setUpvotes(post.getUpvotes() - 1); else post.setDownvotes(post.getDownvotes() - 1);
                voteRepository.delete(vote); postRepository.save(post);
                return Map.of("upvotes", post.getUpvotes(), "downvotes", post.getDownvotes(), "userVote", 0);
            }
            if (vote.getValue() == 1) post.setUpvotes(post.getUpvotes() - 1); else post.setDownvotes(post.getDownvotes() - 1);
            vote.setValue(value); voteRepository.save(vote);
        } else {
            voteRepository.save(CommunityVote.builder().user(user).post(post).value(value).build());
        }
        if (value == 1) post.setUpvotes(post.getUpvotes() + 1); else post.setDownvotes(post.getDownvotes() + 1);
        postRepository.save(post);
        return Map.of("upvotes", post.getUpvotes(), "downvotes", post.getDownvotes(), "userVote", value);
    }

    @Transactional
    public CommunityCommentDTO addComment(Long postId, Long userId, CreateCommunityCommentRequest req) {
        CommunityPost post = postRepository.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Post not found"));
        User user = userRepository.findById(userId).orElseThrow();
        CommunityComment parent = req.getParentCommentId() != null ? commentRepository.findById(req.getParentCommentId()).orElse(null) : null;
        CommunityComment comment = CommunityComment.builder()
                .post(post).user(user).parentComment(parent).content(req.getContent())
                .upvotes(0).downvotes(0).isActive(true).build();
        return mapCommentDTO(commentRepository.save(comment), userId, 0);
    }

    public Page<CommunityCommentDTO> getComments(Long postId, Long userId, int page, int size) {
        return commentRepository.findTopLevelByPostId(postId, PageRequest.of(page, size)).map(c -> mapCommentDTO(c, userId, 2));
    }

    @Transactional
    public Map<String, Object> voteComment(Long commentId, Long userId, int value) {
        CommunityComment comment = commentRepository.findById(commentId).orElseThrow(() -> new ResourceNotFoundException("Comment not found"));
        User user = userRepository.findById(userId).orElseThrow();
        var existing = voteRepository.findByUserIdAndCommentIdAndPostIdIsNull(userId, commentId);
        if (existing.isPresent()) {
            CommunityVote vote = existing.get();
            if (vote.getValue() == value) {
                if (value == 1) comment.setUpvotes(comment.getUpvotes() - 1); else comment.setDownvotes(comment.getDownvotes() - 1);
                voteRepository.delete(vote); commentRepository.save(comment);
                return Map.of("upvotes", comment.getUpvotes(), "downvotes", comment.getDownvotes(), "userVote", 0);
            }
            if (vote.getValue() == 1) comment.setUpvotes(comment.getUpvotes() - 1); else comment.setDownvotes(comment.getDownvotes() - 1);
            vote.setValue(value); voteRepository.save(vote);
        } else {
            voteRepository.save(CommunityVote.builder().user(user).comment(comment).value(value).build());
        }
        if (value == 1) comment.setUpvotes(comment.getUpvotes() + 1); else comment.setDownvotes(comment.getDownvotes() + 1);
        commentRepository.save(comment);
        return Map.of("upvotes", comment.getUpvotes(), "downvotes", comment.getDownvotes(), "userVote", value);
    }

    @Transactional
    public void inviteUser(Long communityId, Long senderId, Long receiverId) {
        Community community = communityRepository.findById(communityId).orElseThrow(() -> new ResourceNotFoundException("Community not found"));
        if (!memberRepository.existsByCommunityIdAndUserId(communityId, senderId)) throw new BadRequestException("Must be a member to invite");
        if (memberRepository.existsByCommunityIdAndUserId(communityId, receiverId)) throw new BadRequestException("User is already a member");
        String message = "Hey! Join our community \"" + community.getName() + "\" — /communities/" + communityId;
        SendMessageRequest msgReq = new SendMessageRequest();
        msgReq.setReceiverId(receiverId);
        msgReq.setContent(message);
        chatService.sendMessage(senderId, msgReq);
    }

    private void assertRole(Long communityId, Long userId, String... allowedRoles) {
        var member = memberRepository.findByCommunityIdAndUserId(communityId, userId)
                .orElseThrow(() -> new BadRequestException("Not a member"));
        boolean allowed = false;
        for (String role : allowedRoles) { if (role.equals(member.getRole())) { allowed = true; break; } }
        if (!allowed) throw new UnauthorizedException("Insufficient permissions");
    }

    private CommunityDTO mapCommunityDTO(Community c, Long userId) {
        boolean isMember = memberRepository.existsByCommunityIdAndUserId(c.getId(), userId);
        String role = null;
        if (isMember) { var m = memberRepository.findByCommunityIdAndUserId(c.getId(), userId); role = m.map(CommunityMember::getRole).orElse(null); }
        return CommunityDTO.builder().id(c.getId()).name(c.getName()).description(c.getDescription())
                .bannerUrl(c.getBannerUrl()).iconUrl(c.getIconUrl())
                .createdById(c.getCreatedBy().getId()).createdByName(c.getCreatedBy().getName())
                .isPrivate(c.isPrivate()).memberCount(c.getMemberCount())
                .isMember(isMember).memberRole(role).createdAt(c.getCreatedAt()).build();
    }

    private CommunityPostDTO mapPostDTO(CommunityPost p, Long userId) {
        int commentCount = commentRepository.countByPostIdAndIsActiveTrue(p.getId());
        int userVote = 0;
        var vote = voteRepository.findByUserIdAndPostIdAndCommentIdIsNull(userId, p.getId());
        if (vote.isPresent()) userVote = vote.get().getValue();
        return CommunityPostDTO.builder().id(p.getId()).communityId(p.getCommunity().getId())
                .communityName(p.getCommunity().getName()).communityIconUrl(p.getCommunity().getIconUrl())
                .authorId(p.getUser().getId()).authorName(p.getUser().getName())
                .authorProfilePic(p.getUser().getProfilePicUrl()).authorRole(p.getUser().getRole().name())
                .content(p.getContent()).imageUrl(p.getImageUrl()).videoUrl(p.getVideoUrl())
                .upvotes(p.getUpvotes()).downvotes(p.getDownvotes()).score(p.getUpvotes() - p.getDownvotes())
                .userVote(userVote).commentCount(commentCount).createdAt(p.getCreatedAt()).build();
    }

    private CommunityCommentDTO mapCommentDTO(CommunityComment c, Long userId, int depth) {
        int userVote = 0;
        var vote = voteRepository.findByUserIdAndCommentIdAndPostIdIsNull(userId, c.getId());
        if (vote.isPresent()) userVote = vote.get().getValue();
        int replyCount = commentRepository.countByParentCommentIdAndIsActiveTrue(c.getId());
        List<CommunityCommentDTO> replies = new ArrayList<>();
        if (depth > 0) { replies = commentRepository.findReplies(c.getId()).stream().map(r -> mapCommentDTO(r, userId, depth - 1)).collect(Collectors.toList()); }
        return CommunityCommentDTO.builder().id(c.getId()).postId(c.getPost().getId())
                .authorId(c.getUser().getId()).authorName(c.getUser().getName()).authorProfilePic(c.getUser().getProfilePicUrl())
                .parentCommentId(c.getParentComment() != null ? c.getParentComment().getId() : null)
                .content(c.getContent()).upvotes(c.getUpvotes()).downvotes(c.getDownvotes())
                .score(c.getUpvotes() - c.getDownvotes()).userVote(userVote)
                .replyCount(replyCount).replies(replies).createdAt(c.getCreatedAt()).build();
    }
}
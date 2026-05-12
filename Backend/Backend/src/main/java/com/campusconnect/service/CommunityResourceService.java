package com.campusconnect.service;

import com.campusconnect.dto.CommunityResourceDTO;
import com.campusconnect.dto.CreateResourceRequest;
import com.campusconnect.entity.Community;
import com.campusconnect.entity.CommunityMember;
import com.campusconnect.entity.CommunityResource;
import com.campusconnect.entity.User;
import com.campusconnect.enums.ResourceType;
import com.campusconnect.exception.BadRequestException;
import com.campusconnect.exception.ResourceNotFoundException;
import com.campusconnect.exception.UnauthorizedException;
import com.campusconnect.repository.CommunityMemberRepository;
import com.campusconnect.repository.CommunityRepository;
import com.campusconnect.repository.CommunityResourceRepository;
import com.campusconnect.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CommunityResourceService {

    private final CommunityResourceRepository resourceRepository;
    private final CommunityRepository communityRepository;
    private final CommunityMemberRepository memberRepository;
    private final UserRepository userRepository;

    @Transactional
    public CommunityResourceDTO add(Long communityId, Long userId, CreateResourceRequest req) {
        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new ResourceNotFoundException("Community not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (!memberRepository.existsByCommunityIdAndUserId(communityId, userId)) {
            throw new UnauthorizedException("Only members can add resources");
        }
        ResourceType type;
        try { type = ResourceType.valueOf(req.getResourceType().toUpperCase()); }
        catch (Exception ex) { throw new BadRequestException("Invalid resource type"); }

        CommunityResource r = CommunityResource.builder()
                .community(community).uploadedBy(user)
                .title(req.getTitle()).description(req.getDescription())
                .resourceType(type).url(req.getUrl())
                .fileSizeBytes(req.getFileSizeBytes())
                .mimeType(req.getMimeType())
                .tags(req.getTags()).clickCount(0).build();

        return toDTO(resourceRepository.save(r));
    }

    public Page<CommunityResourceDTO> list(Long communityId, String q, int page, int size) {
        if (q != null && !q.isBlank()) {
            return resourceRepository.searchByCommunity(communityId, q.trim(), PageRequest.of(page, size))
                    .map(this::toDTO);
        }
        return resourceRepository.findByCommunity(communityId, PageRequest.of(page, size))
                .map(this::toDTO);
    }

    @Transactional
    public void delete(Long resourceId, Long userId) {
        CommunityResource r = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found"));
        boolean isOwner = r.getUploadedBy().getId().equals(userId);
        Optional<CommunityMember> member = memberRepository.findByCommunityIdAndUserId(r.getCommunity().getId(), userId);
        boolean isAdmin = member.isPresent() &&
                ("OWNER".equals(member.get().getRole()) || "ADMIN".equals(member.get().getRole()));
        if (!isOwner && !isAdmin)
            throw new UnauthorizedException("Cannot delete this resource");
        resourceRepository.delete(r);
    }

    @Transactional
    public CommunityResourceDTO trackClick(Long resourceId) {
        CommunityResource r = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found"));
        r.setClickCount(r.getClickCount() + 1);
        return toDTO(resourceRepository.save(r));
    }

    private CommunityResourceDTO toDTO(CommunityResource r) {
        User u = r.getUploadedBy();
        return CommunityResourceDTO.builder()
                .id(r.getId()).communityId(r.getCommunity().getId())
                .uploadedById(u.getId()).uploadedByName(u.getName())
                .uploadedByProfilePic(u.getProfilePicUrl())
                .title(r.getTitle()).description(r.getDescription())
                .resourceType(r.getResourceType().name()).url(r.getUrl())
                .fileSizeBytes(r.getFileSizeBytes()).mimeType(r.getMimeType())
                .tags(r.getTags()).clickCount(r.getClickCount())
                .createdAt(r.getCreatedAt()).build();
    }
}
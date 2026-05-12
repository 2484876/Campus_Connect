package com.campusconnect.service;

import com.campusconnect.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final UserService userService;
    private final CommunityService communityService;
    private final HashtagService hashtagService;

    public SearchResultDTO universalSearch(String q, Long userId) {
        List<UserDTO> users = new ArrayList<>();
        List<CommunityDTO> communities = new ArrayList<>();
        List<HashtagDTO> hashtags = new ArrayList<>();
        List<PostDTO> posts = new ArrayList<>();
        List<EventDTO> events = new ArrayList<>();

        try {
            users = userService.searchUsers(q, userId, 0, 5).getContent();
        } catch (Exception ignored) {}
        try {
            communities = communityService.searchCommunities(q, userId, 0, 5).getContent();
        } catch (Exception ignored) {}
        try {
            hashtags = hashtagService.search(q, 5);
        } catch (Exception ignored) {}

        return SearchResultDTO.builder()
                .users(users)
                .communities(communities)
                .hashtags(hashtags)
                .posts(posts)
                .events(events)
                .build();
    }
}
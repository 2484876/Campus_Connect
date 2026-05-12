package com.campusconnect.service;

import com.campusconnect.dto.PostDTO;
import com.campusconnect.entity.Bookmark;
import com.campusconnect.entity.Post;
import com.campusconnect.entity.User;
import com.campusconnect.exception.ResourceNotFoundException;
import com.campusconnect.repository.BookmarkRepository;
import com.campusconnect.repository.PostRepository;
import com.campusconnect.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final FeedService feedService;

    @Transactional
    public boolean toggleBookmark(Long userId, Long postId) {
        if (bookmarkRepository.existsByUserIdAndPostId(userId, postId)) {
            bookmarkRepository.deleteByUserIdAndPostId(userId, postId);
            return false;
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));
        Bookmark b = Bookmark.builder().user(user).post(post).build();
        bookmarkRepository.save(b);
        return true;
    }

    public Page<PostDTO> getMyBookmarks(Long userId, int page, int size) {
        return bookmarkRepository.findByUserId(userId, PageRequest.of(page, size))
                .map(b -> feedService.mapToDTO(b.getPost(), userId));
    }

    public boolean isBookmarked(Long userId, Long postId) {
        return bookmarkRepository.existsByUserIdAndPostId(userId, postId);
    }

    public long count(Long userId) {
        return bookmarkRepository.countByUserId(userId);
    }
}
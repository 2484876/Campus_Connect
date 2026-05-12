package com.campusconnect.service;

import com.campusconnect.dto.CreatePollRequest;
import com.campusconnect.dto.PollDTO;
import com.campusconnect.dto.PollOptionDTO;
import com.campusconnect.entity.*;
import com.campusconnect.exception.BadRequestException;
import com.campusconnect.exception.ResourceNotFoundException;
import com.campusconnect.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PollService {

    private final PollRepository pollRepository;
    private final PollVoteRepository voteRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Transactional
    public PollDTO createPoll(Long userId, Long postId, CreatePollRequest req) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));
        if (!post.getUser().getId().equals(userId))
            throw new BadRequestException("Only the post author can attach a poll");
        if (pollRepository.findByPostId(postId).isPresent())
            throw new BadRequestException("This post already has a poll");
        if (req.getOptions() == null || req.getOptions().size() < 2 || req.getOptions().size() > 6)
            throw new BadRequestException("Polls need 2 to 6 options");

        Poll poll = Poll.builder()
                .post(post)
                .question(req.getQuestion())
                .multiChoice(req.isMultiChoice())
                .expiresAt(req.getExpiresAt())
                .build();

        List<PollOption> opts = new ArrayList<>();
        int order = 0;
        for (String text : req.getOptions()) {
            if (text == null || text.isBlank()) continue;
            opts.add(PollOption.builder().poll(poll).optionText(text.trim()).displayOrder(order++).build());
        }
        poll.setOptions(opts);
        Poll saved = pollRepository.save(poll);
        return toDTO(saved, userId);
    }

    public PollDTO getPollForPost(Long postId, Long userId) {
        return pollRepository.findByPostId(postId).map(p -> toDTO(p, userId)).orElse(null);
    }

    @Transactional
    public PollDTO vote(Long pollId, Long optionId, Long userId) {
        Poll poll = pollRepository.findById(pollId)
                .orElseThrow(() -> new ResourceNotFoundException("Poll not found"));
        if (poll.getExpiresAt() != null && poll.getExpiresAt().isBefore(LocalDateTime.now()))
            throw new BadRequestException("Poll has expired");
        PollOption option = poll.getOptions().stream()
                .filter(o -> o.getId().equals(optionId)).findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Option not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!poll.isMultiChoice()) voteRepository.deleteByPollIdAndUserId(pollId, userId);

        if (voteRepository.findByPollIdAndOptionIdAndUserId(pollId, optionId, userId).isPresent()) {
            voteRepository.deleteByPollIdAndOptionIdAndUserId(pollId, optionId, userId);
        } else {
            voteRepository.save(PollVote.builder().poll(poll).option(option).user(user).build());
        }
        return toDTO(poll, userId);
    }

    public PollDTO toDTO(Poll poll, Long userId) {
        long total = voteRepository.countUniqueVoters(poll.getId());
        List<Long> myVotes = userId == null ? List.of() :
                voteRepository.findByPollIdAndUserId(poll.getId(), userId)
                        .stream().map(v -> v.getOption().getId()).collect(Collectors.toList());

        List<PollOptionDTO> opts = poll.getOptions().stream()
                .sorted(Comparator.comparingInt(PollOption::getDisplayOrder))
                .map(o -> {
                    long count = voteRepository.countByPollIdAndOptionId(poll.getId(), o.getId());
                    double pct = total > 0 ? (count * 100.0 / total) : 0.0;
                    return PollOptionDTO.builder()
                            .id(o.getId()).optionText(o.getOptionText())
                            .voteCount(count).percentage(Math.round(pct * 10) / 10.0)
                            .build();
                }).collect(Collectors.toList());

        boolean expired = poll.getExpiresAt() != null && poll.getExpiresAt().isBefore(LocalDateTime.now());

        return PollDTO.builder()
                .id(poll.getId())
                .postId(poll.getPost().getId())
                .question(poll.getQuestion())
                .multiChoice(poll.isMultiChoice())
                .expiresAt(poll.getExpiresAt())
                .expired(expired)
                .totalVotes(total)
                .options(opts)
                .hasVoted(!myVotes.isEmpty())
                .myVotedOptionIds(myVotes)
                .build();
    }
}
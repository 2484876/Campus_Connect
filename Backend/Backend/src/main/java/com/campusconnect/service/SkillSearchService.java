package com.campusconnect.service;

import com.campusconnect.dto.UserDTO;
import com.campusconnect.entity.User;
import com.campusconnect.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SkillSearchService {

    private final UserSkillRepository userSkillRepository;
    private final UserRepository userRepository;

    public Page<UserDTO> findUsersBySkill(String skill, int page, int size) {
        Page<Long> ids = userSkillRepository.findUserIdsBySkill(skill, PageRequest.of(page, size));
        List<User> users = userRepository.findAllById(ids.getContent());
        // preserve order from query
        Map<Long, User> byId = new HashMap<>();
        for (User u : users) byId.put(u.getId(), u);
        List<UserDTO> ordered = ids.getContent().stream()
                .map(byId::get)
                .filter(Objects::nonNull)
                .map(this::toDTO)
                .collect(Collectors.toList());
        return new PageImpl<>(ordered, ids.getPageable(), ids.getTotalElements());
    }

    public List<Map<String, Object>> autocompleteSkills(String q, int limit) {
        return userSkillRepository.searchSkillNames(q, PageRequest.of(0, limit)).stream()
                .map(row -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("skill", row[0]);
                    m.put("count", ((Number) row[1]).longValue());
                    return m;
                })
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> trendingSkills(int limit) {
        return userSkillRepository.findTrendingSkills(PageRequest.of(0, limit)).stream()
                .map(row -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("skill", row[0]);
                    m.put("count", ((Number) row[1]).longValue());
                    return m;
                })
                .collect(Collectors.toList());
    }

    private UserDTO toDTO(User u) {
        return UserDTO.builder()
                .id(u.getId())
                .name(u.getName())
                .email(u.getEmail())
                .role(u.getRole() != null ? u.getRole().name() : null)
                .department(u.getDepartment())
                .position(u.getPosition())
                .bio(u.getBio())
                .profilePicUrl(u.getProfilePicUrl())
                .build();
    }
}
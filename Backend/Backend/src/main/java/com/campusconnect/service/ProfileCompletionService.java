package com.campusconnect.service;

import com.campusconnect.dto.ProfileCompletionDTO;
import com.campusconnect.entity.User;
import com.campusconnect.exception.ResourceNotFoundException;
import com.campusconnect.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ProfileCompletionService {

    private final UserRepository userRepository;
    private final UserSkillRepository userSkillRepository;
    private final ExperienceRepository experienceRepository;

    public ProfileCompletionDTO getCompletion(Long userId) {
        User u = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<Check> checks = new ArrayList<>();
        checks.add(new Check("avatar", "Profile picture", 15, u.getProfilePicUrl() != null && !u.getProfilePicUrl().isBlank()));
        checks.add(new Check("banner", "Cover banner", 10, u.getBannerUrl() != null && !u.getBannerUrl().isBlank()));
        checks.add(new Check("bio", "Bio / about", 15, u.getBio() != null && u.getBio().length() >= 20));
        checks.add(new Check("position", "Position / title", 10, u.getPosition() != null && !u.getPosition().isBlank()));
        checks.add(new Check("department", "Department", 5, u.getDepartment() != null && !u.getDepartment().isBlank()));
        checks.add(new Check("phone", "Phone number", 5, u.getPhone() != null && !u.getPhone().isBlank()));

        boolean hasSkills = !userSkillRepository.findByUserId(userId).isEmpty();
        checks.add(new Check("skills", "Add at least 1 skill", 15, hasSkills));

        boolean hasExp = !experienceRepository.findByUserId(userId).isEmpty();
        checks.add(new Check("experience", "Work experience", 25, hasExp));

        int percent = 0;
        List<ProfileCompletionDTO.MissingField> missing = new ArrayList<>();
        for (Check c : checks) {
            if (c.complete) {
                percent += c.weight;
            } else {
                missing.add(ProfileCompletionDTO.MissingField.builder()
                        .key(c.key)
                        .label(c.label)
                        .weight(c.weight)
                        .build());
            }
        }

        return ProfileCompletionDTO.builder()
                .percent(Math.min(100, percent))
                .missing(missing)
                .build();
    }

    private static class Check {
        final String key, label;
        final int weight;
        final boolean complete;
        Check(String key, String label, int weight, boolean complete) {
            this.key = key; this.label = label; this.weight = weight; this.complete = complete;
        }
    }
}
package com.campusconnect.service;

import com.campusconnect.dto.*;
import com.campusconnect.entity.*;
import com.campusconnect.exception.ResourceNotFoundException;
import com.campusconnect.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserSkillRepository userSkillRepository;
    private final ConnectionRepository connectionRepository;
    private final ExperienceRepository experienceRepository;

    public UserDTO getUserById(Long id, Long currentId) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return mapToDTO(user, currentId);
    }

    public UserDTO getProfile(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return mapToDTO(user, id);
    }

    @Transactional
    public UserDTO updateProfile(Long id, UpdateUserRequest req) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (req.getName() != null) user.setName(req.getName());
        if (req.getDepartment() != null) user.setDepartment(req.getDepartment());
        if (req.getPosition() != null) user.setPosition(req.getPosition());
        if (req.getBio() != null) user.setBio(req.getBio());
        if (req.getPhone() != null) user.setPhone(req.getPhone());
        userRepository.save(user);
        if (req.getSkills() != null) {
            userSkillRepository.deleteByUserId(id);
            for (String skill : req.getSkills()) {
                userSkillRepository.save(UserSkill.builder().user(user).skillName(skill).build());
            }
        }
        return mapToDTO(user, id);
    }

    @Transactional
    public void updateProfilePicUrl(Long userId, String url) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setProfilePicUrl(url);
        userRepository.save(user);
    }

    @Transactional
    public void updateBannerUrl(Long userId, String url) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setBannerUrl(url);
        userRepository.save(user);
    }

    @Transactional
    public ExperienceDTO addExperience(Long userId, CreateExperienceRequest req) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Experience exp = Experience.builder()
                .user(user).title(req.getTitle()).employmentType(req.getEmploymentType())
                .company(req.getCompany()).location(req.getLocation())
                .startDate(req.getStartDate()).endDate(req.getEndDate())
                .isCurrent(req.isCurrent()).description(req.getDescription()).build();
        Experience saved = experienceRepository.save(exp);
        return mapExperienceDTO(saved);
    }

    @Transactional
    public void deleteExperience(Long userId, Long experienceId) {
        Experience exp = experienceRepository.findById(experienceId).orElseThrow(() -> new ResourceNotFoundException("Experience not found"));
        if (!exp.getUser().getId().equals(userId)) throw new ResourceNotFoundException("Not authorized");
        experienceRepository.delete(exp);
    }

    public Page<UserDTO> searchUsers(String query, Long currentId, int page, int size) {
        return userRepository.searchUsers(query, PageRequest.of(page, size)).map(user -> mapToDTO(user, currentId));
    }

    public Page<UserDTO> getSuggestions(Long userId, int page, int size) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (user.getDepartment() == null) return Page.empty();
        return userRepository.findByDepartment(user.getDepartment(), userId, PageRequest.of(page, size)).map(u -> mapToDTO(u, userId));
    }

    public Page<UserDTO> getUsersByRole(String role, int page, int size) {
        return userRepository.findByRoleAndIsActiveTrue(role, PageRequest.of(page, size)).map(u -> mapToDTO(u, null));
    }

    private UserDTO mapToDTO(User user, Long currentId) {
        List<String> skills = userSkillRepository.findByUserId(user.getId()).stream().map(UserSkill::getSkillName).collect(Collectors.toList());
        int connCount = connectionRepository.countAcceptedConnections(user.getId());
        String connStatus = null;
        if (currentId != null && !user.getId().equals(currentId)) {
            var conn = connectionRepository.findBetweenUsers(user.getId(), currentId);
            if (conn.isPresent()) connStatus = conn.get().getStatus().name();
        }
        List<ExperienceDTO> experiences = experienceRepository.findByUserId(user.getId()).stream().map(this::mapExperienceDTO).collect(Collectors.toList());
        return UserDTO.builder()
                .id(user.getId()).name(user.getName()).email(user.getEmail())
                .role(user.getRole().name()).department(user.getDepartment())
                .position(user.getPosition()).bio(user.getBio())
                .profilePicUrl(user.getProfilePicUrl()).bannerUrl(user.getBannerUrl())
                .phone(user.getPhone()).createdAt(user.getCreatedAt())
                .skills(skills).connectionCount(connCount).connectionStatus(connStatus)
                .experiences(experiences).build();
    }

    private ExperienceDTO mapExperienceDTO(Experience e) {
        return ExperienceDTO.builder()
                .id(e.getId()).title(e.getTitle()).employmentType(e.getEmploymentType())
                .company(e.getCompany()).location(e.getLocation())
                .startDate(e.getStartDate()).endDate(e.getEndDate())
                .isCurrent(e.isCurrent()).description(e.getDescription()).build();
    }
}
package com.campusconnect.service;

import com.campusconnect.config.JwtService;
import com.campusconnect.dto.*;
import com.campusconnect.entity.User;
import com.campusconnect.enums.Role;
import com.campusconnect.exception.BadRequestException;
import com.campusconnect.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already registered");
        }
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole() != null ? Role.valueOf(request.getRole().toUpperCase()) : Role.PROGRAMMER_ANALYST_TRAINEE)
                .department(request.getDepartment())
                .position(request.getPosition())
                .phone(request.getPhone())
                .isActive(true)
                .build();
        User saved = userRepository.save(user);
        String token = jwtService.generateToken(saved.getId(), saved.getEmail());
        return AuthResponse.builder()
                .token(token).userId(saved.getId()).name(saved.getName())
                .email(saved.getEmail()).role(saved.getRole().name()).build();
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("Invalid credentials"));
        String token = jwtService.generateToken(user.getId(), user.getEmail());
        return AuthResponse.builder()
                .token(token).userId(user.getId()).name(user.getName())
                .email(user.getEmail()).role(user.getRole().name()).build();
    }
}
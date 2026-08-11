package com.jobsearch.user_service.service;

import com.jobsearch.user_service.dto.LoginRequest;
import com.jobsearch.user_service.dto.LoginResponse;
import com.jobsearch.user_service.dto.RegisterRequest;
import com.jobsearch.user_service.dto.RegisterResponse;
import com.jobsearch.user_service.entity.User;
import com.jobsearch.user_service.enums.UserType;
import com.jobsearch.user_service.exception.UserAlreadyExistsException;
import com.jobsearch.user_service.repository.UserRepository;
import com.jobsearch.user_service.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public RegisterResponse register(RegisterRequest request) {
        log.info("Register attempt — username: {}, userType: {}",
                request.getUsername(), request.getUserType());

        if(userRepository.existsByUsername(request.getUsername())) {
            log.warn("Registration failed — username already taken: {}",
                    request.getUsername());
            throw new UserAlreadyExistsException("Username already taken: " + request.getUsername());
        }
        if(userRepository.existsByEmail(request.getEmail())) {
            log.warn("Registration failed — email already registered: {}",
                    request.getEmail());
            throw new UserAlreadyExistsException("Email already registered: " + request.getEmail());
        }

        if(request.getUserType() == UserType.EMPLOYER && (request.getOrgName() == null || request.getOrgName().isBlank())) {
            log.warn("Registration failed — orgName missing for EMPLOYER");
            throw new IllegalArgumentException("Organization name is required for Employer");
        }
        if(request.getUserType() == UserType.JOB_SEEKER && (request.getName() == null || request.getName().isBlank())) {
            log.warn("Registration failed — name missing for JOB_SEEKER");
            throw new IllegalArgumentException("Name is required for Job Seeker");
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .contactNo(request.getContactNo())
                .address(request.getAddress())
                .userType(request.getUserType())
                .orgName(request.getOrgName())
                .name(request.getName())
                .skillSet(request.getSkillSet())
                .build();

        User saved = userRepository.save(user);
        log.info("User registered successfully — id: {}, username: {}, userType: {}",
                saved.getId(), saved.getUsername(), saved.getUserType());

        return RegisterResponse.builder()
                .id(saved.getId())
                .username(saved.getUsername())
                .email(saved.getEmail())
                .userType(saved.getUserType())
                .message("User registered successfully")
                .build();
    }

    public LoginResponse login(LoginRequest request) {
        log.info("Login attempt — username: {}", request.getUsername());
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        User user = userRepository.findByUsername(request.getUsername()).orElseThrow();

        String token = jwtUtil.generateToken(
                user.getUsername(),
                user.getUserType().name(),
                user.getId()
        );

        return LoginResponse.builder()
                .token(token)
                .username(user.getUsername())
                .userType(user.getUserType())
                .userId(user.getId())
                .build();
    }
}

package com.jobsearch.user_service.controller;

import com.jobsearch.user_service.dto.LoginRequest;
import com.jobsearch.user_service.dto.LoginResponse;
import com.jobsearch.user_service.dto.RegisterRequest;
import com.jobsearch.user_service.dto.RegisterResponse;
import com.jobsearch.user_service.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("POST /api/auth/register — userType: {}, username: {}",
                request.getUserType(), request.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("POST /api/auth/login — username: {}", request.getUsername());
        return ResponseEntity.status(HttpStatus.OK).body(authService.login(request));
    }
}

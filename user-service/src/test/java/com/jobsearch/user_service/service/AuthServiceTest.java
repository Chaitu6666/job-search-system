package com.jobsearch.user_service.service;

import com.jobsearch.user_service.dto.*;
import com.jobsearch.user_service.entity.User;
import com.jobsearch.user_service.enums.UserType;
import com.jobsearch.user_service.exception.UserAlreadyExistsException;
import com.jobsearch.user_service.repository.UserRepository;
import com.jobsearch.user_service.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtil jwtUtil;
    @Mock private AuthenticationManager authenticationManager;

    @InjectMocks private AuthService authService;

    private RegisterRequest employerRequest;
    private RegisterRequest jobSeekerRequest;
    private User employerUser;
    private User jobSeekerUser;

    @BeforeEach
    void setUp() {
        employerRequest = new RegisterRequest();
        employerRequest.setUserType(UserType.EMPLOYER);
        employerRequest.setUsername("emp1");
        employerRequest.setPassword("pass123");
        employerRequest.setEmail("emp1@gmail.com");
        employerRequest.setContactNo("9999999999");
        employerRequest.setAddress("Hyderabad");
        employerRequest.setOrgName("Tech Corp");

        jobSeekerRequest = new RegisterRequest();
        jobSeekerRequest.setUserType(UserType.JOB_SEEKER);
        jobSeekerRequest.setUsername("seeker1");
        jobSeekerRequest.setPassword("pass123");
        jobSeekerRequest.setEmail("seeker1@gmail.com");
        jobSeekerRequest.setContactNo("8888888888");
        jobSeekerRequest.setAddress("Hyderabad");
        jobSeekerRequest.setName("Ravi Kumar");
        jobSeekerRequest.setSkillSet("Java,Spring Boot");

        employerUser = User.builder()
                .id(1L)
                .username("emp1")
                .password("encodedPass")
                .email("emp1@gmail.com")
                .contactNo("9999999999")
                .address("Hyderabad")
                .userType(UserType.EMPLOYER)
                .orgName("Tech Corp")
                .build();

        jobSeekerUser = User.builder()
                .id(2L)
                .username("seeker1")
                .password("encodedPass")
                .email("seeker1@gmail.com")
                .contactNo("8888888888")
                .address("Hyderabad")
                .userType(UserType.JOB_SEEKER)
                .name("Ravi Kumar")
                .skillSet("Java,Spring Boot")
                .build();
    }

    // ── Register Tests ────────────────────────────────────────────────────────

    @Test
    void register_Employer_Success() {
        when(userRepository.existsByUsername("emp1")).thenReturn(false);
        when(userRepository.existsByEmail("emp1@gmail.com")).thenReturn(false);
        when(passwordEncoder.encode("pass123")).thenReturn("encodedPass");
        when(userRepository.save(any(User.class))).thenReturn(employerUser);

        RegisterResponse response = authService.register(employerRequest);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("emp1", response.getUsername());
        assertEquals(UserType.EMPLOYER, response.getUserType());
        assertEquals("User registered successfully", response.getMessage());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_JobSeeker_Success() {
        when(userRepository.existsByUsername("seeker1")).thenReturn(false);
        when(userRepository.existsByEmail("seeker1@gmail.com")).thenReturn(false);
        when(passwordEncoder.encode("pass123")).thenReturn("encodedPass");
        when(userRepository.save(any(User.class))).thenReturn(jobSeekerUser);

        RegisterResponse response = authService.register(jobSeekerRequest);

        assertNotNull(response);
        assertEquals(2L, response.getId());
        assertEquals("seeker1", response.getUsername());
        assertEquals(UserType.JOB_SEEKER, response.getUserType());
    }

    @Test
    void register_DuplicateUsername_ThrowsException() {
        when(userRepository.existsByUsername("emp1")).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class,
                () -> authService.register(employerRequest));
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_DuplicateEmail_ThrowsException() {
        when(userRepository.existsByUsername("emp1")).thenReturn(false);
        when(userRepository.existsByEmail("emp1@gmail.com")).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class,
                () -> authService.register(employerRequest));
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_Employer_MissingOrgName_ThrowsException() {
        employerRequest.setOrgName(null);
        when(userRepository.existsByUsername("emp1")).thenReturn(false);
        when(userRepository.existsByEmail("emp1@gmail.com")).thenReturn(false);

        assertThrows(IllegalArgumentException.class,
                () -> authService.register(employerRequest));
    }

    @Test
    void register_JobSeeker_MissingName_ThrowsException() {
        jobSeekerRequest.setName(null);
        when(userRepository.existsByUsername("seeker1")).thenReturn(false);
        when(userRepository.existsByEmail("seeker1@gmail.com")).thenReturn(false);

        assertThrows(IllegalArgumentException.class,
                () -> authService.register(jobSeekerRequest));
    }

    // ── Login Tests ───────────────────────────────────────────────────────────

    @Test
    void login_Success() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("emp1");
        loginRequest.setPassword("pass123");

        when(userRepository.findByUsername("emp1"))
                .thenReturn(Optional.of(employerUser));
        when(jwtUtil.generateToken("emp1", "EMPLOYER", 1L))
                .thenReturn("mocked.jwt.token");

        LoginResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("mocked.jwt.token", response.getToken());
        assertEquals("emp1", response.getUsername());
        assertEquals(UserType.EMPLOYER, response.getUserType());
        assertEquals(1L, response.getUserId());
    }

    @Test
    void login_InvalidCredentials_ThrowsException() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("emp1");
        loginRequest.setPassword("wrongpass");

        doThrow(new BadCredentialsException("Bad credentials"))
                .when(authenticationManager)
                .authenticate(any(UsernamePasswordAuthenticationToken.class));

        assertThrows(BadCredentialsException.class,
                () -> authService.login(loginRequest));
    }

    @Test
    void login_UserNotFound_ThrowsException() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("unknown");
        loginRequest.setPassword("pass123");

        when(userRepository.findByUsername("unknown"))
                .thenReturn(Optional.empty());

        assertThrows(Exception.class,
                () -> authService.login(loginRequest));
    }
}
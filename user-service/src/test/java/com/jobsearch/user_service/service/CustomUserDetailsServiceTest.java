package com.jobsearch.user_service.service;

import com.jobsearch.user_service.entity.User;
import com.jobsearch.user_service.enums.UserType;
import com.jobsearch.user_service.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock private UserRepository userRepository;
    @InjectMocks private CustomUserDetailsService service;

    @Test
    void loadUserByUsername_Success() {
        User user = User.builder()
                .id(1L)
                .username("emp1")
                .password("encodedPass")
                .userType(UserType.EMPLOYER)
                .build();

        when(userRepository.findByUsername("emp1"))
                .thenReturn(Optional.of(user));

        UserDetails details = service.loadUserByUsername("emp1");

        assertNotNull(details);
        assertEquals("emp1", details.getUsername());
        assertEquals("encodedPass", details.getPassword());
    }

    @Test
    void loadUserByUsername_NotFound_ThrowsException() {
        when(userRepository.findByUsername("unknown"))
                .thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> service.loadUserByUsername("unknown"));
    }
}
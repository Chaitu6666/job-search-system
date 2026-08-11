package com.jobsearch.user_service.dto;

import com.jobsearch.user_service.enums.UserType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterResponse {
    private Long id;
    private String username;
    private String email;
    private UserType userType;
    private String message;
}

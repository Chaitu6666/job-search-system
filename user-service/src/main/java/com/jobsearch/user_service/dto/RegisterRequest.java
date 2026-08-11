package com.jobsearch.user_service.dto;

import com.jobsearch.user_service.enums.UserType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotNull(message = "User type is required EMPLOYER OR JOB_SEEKER")
    private UserType userType;

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;

    @Email(message = "Invalid email")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Contact number is required")
    @Pattern(
            regexp = "^[6-9]\\d{9}$",
            message = "Contact number must be a valid 10-digit Indian mobile number"
    )
    private String contactNo;

    @NotBlank(message = "Address is required")
    private String address;

    // Employer specific
    private String orgName;

    // Job Seeker specific
    private String name;
    private String skillSet;
}

package com.jobsearch.application_service.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ApplicationRequest {

    @NotNull(message = "Job ID is required")
    private Long jobId;

    // jobSeekerId comes from X-User-Id header — NOT from body
    // coverLetter is optional
    private String coverLetter;
}
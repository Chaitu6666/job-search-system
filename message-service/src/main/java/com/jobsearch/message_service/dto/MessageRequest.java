package com.jobsearch.message_service.dto;

import jakarta.validation.constraints.NotBlank;

import lombok.Data;

@Data
public class MessageRequest {

    // For JOBSEEKER_TO_EMPLOYER: employerId is required
    // For EMPLOYER_TO_JOBSEEKER: jobSeekerId is required
    // The sender ID always comes from X-User-Id header

    private Long employerId;    // required when job seeker sends
    private Long jobSeekerId;   // required when employer sends

    // The job this conversation is about (optional)
    private Long jobId;

    @NotBlank(message = "Message description is required")
    private String description;
}

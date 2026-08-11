package com.jobserach.job_service.dto;

import com.jobserach.job_service.enums.JobStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class JobRequest {
    @NotBlank(message = "Job title is required")
    private String jobTitle;

    @NotBlank(message = "Location is required")
    private String location;
    private String description;
    private String experience;
    private String salary;
    private String noticePeriod;

    @Email(message = "Invalid contact email")
    private String contactEmail;

    private JobStatus status;

    private String requiredSkills;
    private String companyName;
    private String designation;
}

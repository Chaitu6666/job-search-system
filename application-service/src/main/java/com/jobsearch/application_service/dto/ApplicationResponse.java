package com.jobsearch.application_service.dto;

import com.jobsearch.application_service.enums.ApplicationStatus;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApplicationResponse {

    private Long applicationId;

    // Job details (from job-service via Feign)
    private Long jobId;
    private String jobTitle;
    private String companyName;
    private String location;
    private String salary;

    // Job Seeker details (from user-service via Feign)
    private Long jobSeekerId;
    private String jobSeekerName;
    private String jobSeekerEmail;
    private String jobSeekerSkillSet;

    // Employer details
    private Long employerId;
    private String employerOrgName;

    private String coverLetter;
    private ApplicationStatus status;
    private LocalDateTime appliedAt;
}

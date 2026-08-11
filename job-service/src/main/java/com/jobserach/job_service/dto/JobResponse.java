package com.jobserach.job_service.dto;

import com.jobserach.job_service.enums.JobStatus;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JobResponse {

    private Long jobId;
    private Long employerId;
    private String employerName;
    private String jobTitle;
    private String location;
    private String description;
    private String experience;
    private String salary;
    private String noticePeriod;
    private String contactEmail;
    private JobStatus status;
    private String requiredSkills;
    private String companyName;
    private String designation;
    private LocalDateTime createdAt;
}

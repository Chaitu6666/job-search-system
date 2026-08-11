package com.jobsearch.jobbasket_service.dto;

import com.jobsearch.jobbasket_service.enums.BasketStatus;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BasketResponse {

    private Long basketId;

    // Job Seeker info
    private Long jobSeekerId;
    private String jobSeekerName;

    // Job info (fetched from job-service via Feign)
    private Long jobId;
    private String jobTitle;
    private String companyName;
    private String location;
    private String experience;
    private String salary;
    private String requiredSkills;
    private String designation;
    private String jobStatus;       // ACTIVE / CLOSED / DRAFT

    // Basket info
    private BasketStatus status;    // SAVED or APPLIED
    private LocalDateTime addedAt;
}

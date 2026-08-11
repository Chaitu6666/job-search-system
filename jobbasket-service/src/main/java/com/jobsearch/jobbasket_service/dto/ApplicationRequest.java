package com.jobsearch.jobbasket_service.dto;

import lombok.Data;

// Mirrors the request body expected by application-service
@Data
public class ApplicationRequest {
    private Long jobId;
    private String coverLetter;
}

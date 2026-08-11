package com.jobsearch.message_service.dto;

import lombok.Data;

@Data
public class JobResponse {
    private Long jobId;
    private Long employerId;
    private String jobTitle;
    private String companyName;
    private String location;
    private String status;
}

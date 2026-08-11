package com.jobsearch.application_service.dto;

import lombok.Data;

@Data
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
    private String status;
    private String requiredSkills;
    private String companyName;
    private String designation;
}
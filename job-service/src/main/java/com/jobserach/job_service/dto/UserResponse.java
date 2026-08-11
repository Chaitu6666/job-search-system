package com.jobserach.job_service.dto;

import lombok.Data;

@Data
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private String orgName;
    private String name;
    private String contactNo;
    private String address;
    private String skillSet;
    private String userType;
}

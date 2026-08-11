package com.jobsearch.message_service.dto;

import lombok.Data;

@Data
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private String orgName;     // employer
    private String name;        // job seeker
    private String contactNo;
    private String address;
    private String skillSet;
    private String userType;
}

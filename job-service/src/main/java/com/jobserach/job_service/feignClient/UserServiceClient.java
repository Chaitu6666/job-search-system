package com.jobserach.job_service.feignClient;

import com.jobserach.job_service.dto.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "user-service")
public interface UserServiceClient {

    @GetMapping("/api/users/{id}")
    UserResponse getUserById(@PathVariable("id") Long id, @RequestHeader("Authorization") String authHeader);

    @GetMapping("/api/users/jobseekers/search")
    List<UserResponse> searchJobSeekersBySkill(@RequestParam("skill") String skill, @RequestHeader("Authorization") String authHeader);

}

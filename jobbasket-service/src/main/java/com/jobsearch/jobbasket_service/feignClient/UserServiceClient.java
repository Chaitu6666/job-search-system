package com.jobsearch.jobbasket_service.feignClient;

import com.jobsearch.jobbasket_service.dto.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "user-service")
public interface UserServiceClient {

    @GetMapping("/api/users/{id}")
    UserResponse getUserById(
            @PathVariable("id") Long id,
            @RequestHeader("Authorization") String authHeader
    );
}

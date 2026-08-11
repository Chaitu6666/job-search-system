package com.jobsearch.jobbasket_service.feignClient;

import com.jobsearch.jobbasket_service.dto.ApplicationRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

// Calls application-service to apply for a job from basket
@FeignClient(name = "application-service")
public interface ApplicationServiceClient {

    @PostMapping("/api/applications")
    Object applyForJob(
            @RequestBody ApplicationRequest request,
            @RequestHeader("Authorization") String authHeader,
            @RequestHeader("X-User-Id") Long jobSeekerId
    );
}

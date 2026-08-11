package com.jobsearch.jobbasket_service.feignClient;

import com.jobsearch.jobbasket_service.dto.JobResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "job-service")
public interface JobServiceClient {

    @GetMapping("/api/jobs/{jobId}")
    JobResponse getJobById(
            @PathVariable("jobId") Long jobId,
            @RequestHeader("Authorization") String authHeader
    );
}
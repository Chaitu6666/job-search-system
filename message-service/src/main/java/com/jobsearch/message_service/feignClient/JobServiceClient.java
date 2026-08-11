package com.jobsearch.message_service.feignClient;

import com.jobsearch.message_service.dto.JobResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "job-service")
public interface JobServiceClient {
    @GetMapping("/api/jobs/{jobId}")
    JobResponse getJobById(@PathVariable("jobId") Long jobId, @RequestHeader("Authorization") String authHeader);
}

package com.jobserach.job_service.controller;

import com.jobserach.job_service.dto.JobRequest;
import com.jobserach.job_service.dto.JobResponse;
import com.jobserach.job_service.dto.UserResponse;
import com.jobserach.job_service.service.JobService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController {
    private final JobService jobService;

    @PostMapping
    public ResponseEntity<JobResponse> postJob(@Valid @RequestBody JobRequest request, @RequestHeader("Authorization") String authHeader, @RequestHeader("X-User-Id") Long employerId) {
        log.info("POST /api/jobs — employerId: {}", employerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(jobService.postJob(request, employerId, authHeader));
    }

    @PutMapping("{jobId}")
    public ResponseEntity<JobResponse> editJob(@PathVariable Long jobId, @Valid @RequestBody JobRequest request, @RequestHeader("Authorization") String authHeader, @RequestHeader("X-User-Id") Long employerId) {
        log.info("PUT /api/jobs/{} — employerId: {}", jobId, employerId);
        return  ResponseEntity.status(HttpStatus.OK).body(jobService.editJob(jobId, request, employerId, authHeader));
    }

    @DeleteMapping("{jobId}")
    public ResponseEntity<Map<String, String>> deleteJob(@PathVariable Long jobId, @RequestHeader("X-User-Id") Long employerId) {
        log.info("DELETE /api/jobs/{} — employerId: {}", jobId, employerId);
        return  ResponseEntity.status(HttpStatus.OK).body(jobService.deleteJob(jobId, employerId));
    }

    @GetMapping("/{jobId}")
    public ResponseEntity<JobResponse> getJobById(@PathVariable Long jobId, @RequestHeader("Authorization") String authHeader) {
        log.debug("GET /api/jobs/{}", jobId);
        return ResponseEntity.status(HttpStatus.OK).body(jobService.getJobById(jobId, authHeader));
    }

    @GetMapping("/employer/{employerId}")
    public ResponseEntity<List<JobResponse>> getJobsByEmployer(@PathVariable Long employerId, @RequestHeader("Authorization") String authHeader) {
        log.info("GET /api/jobs/employer/{}", employerId);
        return ResponseEntity.status(HttpStatus.OK).body(jobService.getJobsByEmployer(employerId, authHeader));
    }

    @GetMapping("/search")
    public ResponseEntity<List<JobResponse>> searchByKeyword(@RequestParam("keyword") String keyword, @RequestHeader("Authorization") String authHeader) {
        log.info("GET /api/jobs/search?keyword={}", keyword);
        return ResponseEntity.status(HttpStatus.OK).body(jobService.searchByKeyword(keyword, authHeader));
    }

    @GetMapping("/search/location")
    public ResponseEntity<List<JobResponse>> searchByLocation(@RequestParam("location") String location, @RequestHeader("Authorization") String authHeader) {
        log.info("GET /api/jobs/search/location?location={}", location);
        return ResponseEntity.status(HttpStatus.OK).body(jobService.searchByLocation(location, authHeader));
    }

    @GetMapping("/seekers/search")
    public ResponseEntity<List<UserResponse>> searchJobSeekersBySkill(@RequestParam("skill") String skill, @RequestHeader("Authorization") String authHeader) {
        log.info("GET /api/jobs/seekers/search?skill={}", skill);
        return ResponseEntity.status(HttpStatus.OK).body(jobService.searchJobSeekersBySkill(skill, authHeader));
    }

    @GetMapping("/{jobId}/seekers")
    public ResponseEntity<List<UserResponse>> searchJobSeekersByJobId(@PathVariable Long jobId, @RequestHeader("Authorization") String authHeader) {
        log.info("GET /api/jobs/{}/seekers", jobId);
        return ResponseEntity.status(HttpStatus.OK).body(jobService.searchJobSeekersByJobId(jobId, authHeader));
    }
}

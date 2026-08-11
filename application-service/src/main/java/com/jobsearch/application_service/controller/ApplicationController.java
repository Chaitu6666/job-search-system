package com.jobsearch.application_service.controller;

import com.jobsearch.application_service.dto.ApplicationRequest;
import com.jobsearch.application_service.dto.ApplicationResponse;
import com.jobsearch.application_service.service.ApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;

    // ── Job Seeker: Apply for a job ───────────────────────────────────────────
    @PostMapping
    public ResponseEntity<ApplicationResponse> applyForJob(
            @Valid @RequestBody ApplicationRequest request,
            @RequestHeader("Authorization") String authHeader,
            @RequestHeader("X-User-Id") Long jobSeekerId) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(applicationService.applyForJob(
                        request, jobSeekerId, authHeader));
    }

    // ── Job Seeker: View all my applications ──────────────────────────────────
    @GetMapping("/my")
    public ResponseEntity<List<ApplicationResponse>> getMyApplications(
            @RequestHeader("Authorization") String authHeader,
            @RequestHeader("X-User-Id") Long jobSeekerId) {

        return ResponseEntity.ok(
                applicationService.getApplicationsByJobSeeker(
                        jobSeekerId, authHeader));
    }

    // ── Job Seeker: Withdraw an application ───────────────────────────────────
    @DeleteMapping("/{applicationId}/withdraw")
    public ResponseEntity<Map<String, String>> withdrawApplication(
            @PathVariable Long applicationId,
            @RequestHeader("X-User-Id") Long jobSeekerId) {

        return ResponseEntity.ok(
                applicationService.withdrawApplication(
                        applicationId, jobSeekerId));
    }

    // ── Common: Get single application by ID ─────────────────────────────────
    @GetMapping("/{applicationId}")
    public ResponseEntity<ApplicationResponse> getApplicationById(
            @PathVariable Long applicationId,
            @RequestHeader("Authorization") String authHeader) {

        return ResponseEntity.ok(
                applicationService.getApplicationById(
                        applicationId, authHeader));
    }

    // ── Employer: View all applications for a specific job ────────────────────
    @GetMapping("/job/{jobId}")
    public ResponseEntity<List<ApplicationResponse>> getApplicationsByJob(
            @PathVariable Long jobId,
            @RequestHeader("Authorization") String authHeader) {

        return ResponseEntity.ok(
                applicationService.getApplicationsByJobId(jobId, authHeader));
    }

    // ── Employer: View all applications across all their jobs ─────────────────
    @GetMapping("/employer/all")
    public ResponseEntity<List<ApplicationResponse>> getApplicationsByEmployer(
            @RequestHeader("Authorization") String authHeader,
            @RequestHeader("X-User-Id") Long employerId) {

        return ResponseEntity.ok(
                applicationService.getApplicationsByEmployer(
                        employerId, authHeader));
    }

    // ── Employer: Shortlist an application ────────────────────────────────────
    @PutMapping("/{applicationId}/shortlist")
    public ResponseEntity<ApplicationResponse> shortlistApplication(
            @PathVariable Long applicationId,
            @RequestHeader("Authorization") String authHeader,
            @RequestHeader("X-User-Id") Long employerId) {

        return ResponseEntity.ok(
                applicationService.shortlistApplication(
                        applicationId, employerId, authHeader));
    }

    // ── Employer: Reject an application ──────────────────────────────────────
    @PutMapping("/{applicationId}/reject")
    public ResponseEntity<ApplicationResponse> rejectApplication(
            @PathVariable Long applicationId,
            @RequestHeader("Authorization") String authHeader,
            @RequestHeader("X-User-Id") Long employerId) {

        return ResponseEntity.ok(
                applicationService.rejectApplication(
                        applicationId, employerId, authHeader));
    }

    // ── Common: Count applications for a job ──────────────────────────────────
    @GetMapping("/job/{jobId}/count")
    public ResponseEntity<Map<String, Long>> countApplications(
            @PathVariable Long jobId) {

        return ResponseEntity.ok(
                applicationService.countApplicationsForJob(jobId));
    }
}
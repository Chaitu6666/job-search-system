package com.jobsearch.application_service.service;

import com.jobsearch.application_service.dto.ApplicationRequest;
import com.jobsearch.application_service.dto.ApplicationResponse;
import com.jobsearch.application_service.dto.JobResponse;
import com.jobsearch.application_service.dto.UserResponse;
import com.jobsearch.application_service.entity.JobApplication;
import com.jobsearch.application_service.enums.ApplicationStatus;
import com.jobsearch.application_service.exception.ApplicationNotFoundException;
import com.jobsearch.application_service.feignClient.JobServiceClient;
import com.jobsearch.application_service.feignClient.UserServiceClient;
import com.jobsearch.application_service.repository.ApplicationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final JobServiceClient jobServiceClient;
    private final UserServiceClient userServiceClient;

    // ── APPLY FOR A JOB (Job Seeker) ────────────────────────────────────────
    public ApplicationResponse applyForJob(ApplicationRequest request, Long jobSeekerId, String authHeader) {
        log.info("Apply for job — jobSeekerId: {}, jobId: {}",
                jobSeekerId, request.getJobId());
        // Verify job seeker exists and is correct type
        UserResponse jobSeeker = userServiceClient.getUserById(
                jobSeekerId, authHeader);
        if (!"JOB_SEEKER".equals(jobSeeker.getUserType())) {
            throw new IllegalArgumentException(
                    "Only job seekers can apply for jobs");
        }

        // Fetch job details from job-service via Feign
        JobResponse job = jobServiceClient.getJobById(
                request.getJobId(), authHeader);

        // Check if job is still active
        if (!"ACTIVE".equals(job.getStatus())) {
            throw new IllegalStateException(
                    "Cannot apply — this job is no longer active");
        }

        // Prevent duplicate applications
        if (applicationRepository.existsByJobIdAndJobSeekerId(
                request.getJobId(), jobSeekerId)) {
            throw new IllegalStateException(
                    "You have already applied for this job");
        }

        JobApplication application = JobApplication.builder()
                .jobId(request.getJobId())
                .jobSeekerId(jobSeekerId)
                .employerId(job.getEmployerId())
                .coverLetter(request.getCoverLetter())
                .build();

        JobApplication saved = applicationRepository.save(application);
        log.info("Application submitted — applicationId: {}, jobId: {}, jobSeekerId: {}",
                saved.getApplicationId(), request.getJobId(), jobSeekerId);

        // Fetch employer for response
        UserResponse employer = userServiceClient.getUserById(
                job.getEmployerId(), authHeader);

        return mapToResponse(saved, job, jobSeeker, employer);
    }

    // ── VIEW ALL APPLICATIONS BY JOB SEEKER ──────────────────────────────────
    public List<ApplicationResponse> getApplicationsByJobSeeker(
            Long jobSeekerId, String authHeader) {

        UserResponse jobSeeker = userServiceClient.getUserById(
                jobSeekerId, authHeader);

        return applicationRepository.findByJobSeekerId(jobSeekerId)
                .stream()
                .map(app -> {
                    JobResponse job = jobServiceClient.getJobById(
                            app.getJobId(), authHeader);
                    UserResponse employer = userServiceClient.getUserById(
                            app.getEmployerId(), authHeader);
                    return mapToResponse(app, job, jobSeeker, employer);
                })
                .collect(Collectors.toList());
    }

    // ── VIEW ALL APPLICATIONS FOR A JOB (Employer) ───────────────────────────
    public List<ApplicationResponse> getApplicationsByJobId(
            Long jobId, String authHeader) {

        JobResponse job = jobServiceClient.getJobById(jobId, authHeader);

        return applicationRepository.findByJobId(jobId)
                .stream()
                .map(app -> {
                    UserResponse jobSeeker = userServiceClient.getUserById(
                            app.getJobSeekerId(), authHeader);
                    UserResponse employer = userServiceClient.getUserById(
                            app.getEmployerId(), authHeader);
                    return mapToResponse(app, job, jobSeeker, employer);
                })
                .collect(Collectors.toList());
    }

    // ── VIEW ALL APPLICATIONS FOR AN EMPLOYER ─────────────────────────────────
    public List<ApplicationResponse> getApplicationsByEmployer(
            Long employerId, String authHeader) {

        return applicationRepository.findByEmployerId(employerId)
                .stream()
                .map(app -> {
                    JobResponse job = jobServiceClient.getJobById(
                            app.getJobId(), authHeader);
                    UserResponse jobSeeker = userServiceClient.getUserById(
                            app.getJobSeekerId(), authHeader);
                    UserResponse employer = userServiceClient.getUserById(
                            employerId, authHeader);
                    return mapToResponse(app, job, jobSeeker, employer);
                })
                .collect(Collectors.toList());
    }

    // ── SHORTLIST APPLICATION (Employer) ──────────────────────────────────────
    public ApplicationResponse shortlistApplication(Long applicationId,
                                                    Long employerId,
                                                    String authHeader) {
        log.info("Shortlist application — applicationId: {}, employerId: {}",
                applicationId, employerId);
        JobApplication application = applicationRepository
                .findByApplicationIdAndEmployerId(applicationId, employerId)
                .orElseThrow(() -> new ApplicationNotFoundException(
                        "Application not found or you are not authorized"));

        if (application.getStatus() == ApplicationStatus.SHORTLISTED) {
            throw new IllegalStateException(
                    "Application is already shortlisted");
        }

        application.setStatus(ApplicationStatus.SHORTLISTED);
        JobApplication updated = applicationRepository.save(application);
        log.info("Application shortlisted — applicationId: {}", applicationId);

        JobResponse job = jobServiceClient.getJobById(
                updated.getJobId(), authHeader);
        UserResponse jobSeeker = userServiceClient.getUserById(
                updated.getJobSeekerId(), authHeader);
        UserResponse employer = userServiceClient.getUserById(
                employerId, authHeader);

        return mapToResponse(updated, job, jobSeeker, employer);
    }

    // ── REJECT APPLICATION (Employer) ─────────────────────────────────────────
    public ApplicationResponse rejectApplication(Long applicationId,
                                                 Long employerId,
                                                 String authHeader) {
        log.info("Reject application — applicationId: {}, employerId: {}",
                applicationId, employerId);
        JobApplication application = applicationRepository
                .findByApplicationIdAndEmployerId(applicationId, employerId)
                .orElseThrow(() -> new ApplicationNotFoundException(
                        "Application not found or you are not authorized"));

        if (application.getStatus() == ApplicationStatus.REJECTED) {
            throw new IllegalStateException(
                    "Application is already rejected");
        }

        application.setStatus(ApplicationStatus.REJECTED);
        JobApplication updated = applicationRepository.save(application);
        log.info("Application rejected — applicationId: {}", applicationId);

        JobResponse job = jobServiceClient.getJobById(
                updated.getJobId(), authHeader);
        UserResponse jobSeeker = userServiceClient.getUserById(
                updated.getJobSeekerId(), authHeader);
        UserResponse employer = userServiceClient.getUserById(
                employerId, authHeader);

        return mapToResponse(updated, job, jobSeeker, employer);
    }

    // ── GET SINGLE APPLICATION BY ID ─────────────────────────────────────────
    public ApplicationResponse getApplicationById(Long applicationId,
                                                  String authHeader) {
        JobApplication application = applicationRepository
                .findById(applicationId)
                .orElseThrow(() -> new ApplicationNotFoundException(
                        "Application not found with ID: " + applicationId));

        JobResponse job = jobServiceClient.getJobById(
                application.getJobId(), authHeader);
        UserResponse jobSeeker = userServiceClient.getUserById(
                application.getJobSeekerId(), authHeader);
        UserResponse employer = userServiceClient.getUserById(
                application.getEmployerId(), authHeader);

        return mapToResponse(application, job, jobSeeker, employer);
    }

    // ── WITHDRAW APPLICATION (Job Seeker) ─────────────────────────────────────
    public Map<String, String> withdrawApplication(Long applicationId,
                                                   Long jobSeekerId) {
        log.info("Withdraw application — applicationId: {}, jobSeekerId: {}",
                applicationId, jobSeekerId);
        JobApplication application = applicationRepository
                .findByApplicationIdAndJobSeekerId(applicationId, jobSeekerId)
                .orElseThrow(() -> new ApplicationNotFoundException(
                        "Application not found or you are not authorized"));

        // Can only withdraw if still in APPLIED status
        if (application.getStatus() != ApplicationStatus.APPLIED) {
            throw new IllegalStateException(
                    "Cannot withdraw — application is already " +
                            application.getStatus().name());
        }

        applicationRepository.delete(application);
        log.info("Application withdrawn — applicationId: {}", applicationId);
        return Map.of("message", "Application withdrawn successfully");
    }

    // ── COUNT APPLICATIONS FOR A JOB ─────────────────────────────────────────
    public Map<String, Long> countApplicationsForJob(Long jobId) {
        long count = applicationRepository.countByJobId(jobId);
        return Map.of("jobId", jobId, "totalApplications", count);
    }

    // ── MAPPER ────────────────────────────────────────────────────────────────
    private ApplicationResponse mapToResponse(JobApplication app,
                                              JobResponse job,
                                              UserResponse jobSeeker,
                                              UserResponse employer) {
        return ApplicationResponse.builder()
                .applicationId(app.getApplicationId())
                .jobId(app.getJobId())
                .jobTitle(job != null ? job.getJobTitle() : "N/A")
                .companyName(job != null ? job.getCompanyName() : "N/A")
                .location(job != null ? job.getLocation() : "N/A")
                .salary(job != null ? job.getSalary() : "N/A")
                .jobSeekerId(app.getJobSeekerId())
                .jobSeekerName(jobSeeker != null ? jobSeeker.getName() : "N/A")
                .jobSeekerEmail(jobSeeker != null ? jobSeeker.getEmail() : "N/A")
                .jobSeekerSkillSet(jobSeeker != null ? jobSeeker.getSkillSet() : "N/A")
                .employerId(app.getEmployerId())
                .employerOrgName(employer != null ? employer.getOrgName() : "N/A")
                .coverLetter(app.getCoverLetter())
                .status(app.getStatus())
                .appliedAt(app.getAppliedAt())
                .build();
    }
}

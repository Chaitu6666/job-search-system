package com.jobsearch.application_service.service;

import com.jobsearch.application_service.dto.*;
import com.jobsearch.application_service.entity.JobApplication;
import com.jobsearch.application_service.enums.ApplicationStatus;
import com.jobsearch.application_service.exception.ApplicationNotFoundException;
import com.jobsearch.application_service.feignClient.JobServiceClient;
import com.jobsearch.application_service.feignClient.UserServiceClient;
import com.jobsearch.application_service.repository.ApplicationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {

    @Mock private ApplicationRepository applicationRepository;
    @Mock private JobServiceClient jobServiceClient;
    @Mock private UserServiceClient userServiceClient;

    @InjectMocks private ApplicationService applicationService;

    private static final String AUTH = "Bearer test.token";
    private ApplicationRequest applicationRequest;
    private JobApplication jobApplication;
    private UserResponse jobSeekerResponse;
    private UserResponse employerResponse;
    private JobResponse jobResponse;

    @BeforeEach
    void setUp() {
        applicationRequest = new ApplicationRequest();
        applicationRequest.setJobId(1L);
        applicationRequest.setCoverLetter("I am interested");

        jobApplication = JobApplication.builder()
                .applicationId(1L)
                .jobId(1L)
                .jobSeekerId(2L)
                .employerId(1L)
                .status(ApplicationStatus.APPLIED)
                .coverLetter("I am interested")
                .appliedAt(LocalDateTime.now())
                .build();

        jobSeekerResponse = new UserResponse();
        jobSeekerResponse.setId(2L);
        jobSeekerResponse.setName("Ravi Kumar");
        jobSeekerResponse.setEmail("seeker1@gmail.com");
        jobSeekerResponse.setSkillSet("Java,Spring Boot");
        jobSeekerResponse.setUserType("JOB_SEEKER");

        employerResponse = new UserResponse();
        employerResponse.setId(1L);
        employerResponse.setOrgName("Tech Corp");
        employerResponse.setUserType("EMPLOYER");

        jobResponse = new JobResponse();
        jobResponse.setJobId(1L);
        jobResponse.setEmployerId(1L);
        jobResponse.setJobTitle("Java Backend Developer");
        jobResponse.setCompanyName("Tech Corp");
        jobResponse.setLocation("Hyderabad");
        jobResponse.setSalary("8-15 LPA");
        jobResponse.setStatus("ACTIVE");
    }

    // ── Apply Tests ───────────────────────────────────────────────────────────

    @Test
    void applyForJob_Success() {
        when(userServiceClient.getUserById(2L, AUTH))
                .thenReturn(jobSeekerResponse);
        when(jobServiceClient.getJobById(1L, AUTH))
                .thenReturn(jobResponse);
        when(applicationRepository.existsByJobIdAndJobSeekerId(1L, 2L))
                .thenReturn(false);
        when(userServiceClient.getUserById(1L, AUTH))
                .thenReturn(employerResponse);
        when(applicationRepository.save(any(JobApplication.class)))
                .thenReturn(jobApplication);

        ApplicationResponse response =
                applicationService.applyForJob(applicationRequest, 2L, AUTH);

        assertNotNull(response);
        assertEquals(1L, response.getApplicationId());
        assertEquals(ApplicationStatus.APPLIED, response.getStatus());
        verify(applicationRepository).save(any(JobApplication.class));
    }

    @Test
    void applyForJob_NotJobSeeker_ThrowsException() {
        when(userServiceClient.getUserById(1L, AUTH))
                .thenReturn(employerResponse);

        assertThrows(IllegalArgumentException.class,
                () -> applicationService.applyForJob(
                        applicationRequest, 1L, AUTH));
        verify(applicationRepository, never()).save(any());
    }

    @Test
    void applyForJob_JobNotActive_ThrowsException() {
        jobResponse.setStatus("CLOSED");
        when(userServiceClient.getUserById(2L, AUTH))
                .thenReturn(jobSeekerResponse);
        when(jobServiceClient.getJobById(1L, AUTH))
                .thenReturn(jobResponse);

        assertThrows(IllegalStateException.class,
                () -> applicationService.applyForJob(
                        applicationRequest, 2L, AUTH));
    }

    @Test
    void applyForJob_DuplicateApplication_ThrowsException() {
        when(userServiceClient.getUserById(2L, AUTH))
                .thenReturn(jobSeekerResponse);
        when(jobServiceClient.getJobById(1L, AUTH))
                .thenReturn(jobResponse);
        when(applicationRepository.existsByJobIdAndJobSeekerId(1L, 2L))
                .thenReturn(true);

        assertThrows(IllegalStateException.class,
                () -> applicationService.applyForJob(
                        applicationRequest, 2L, AUTH));
    }

    // ── Shortlist Tests ───────────────────────────────────────────────────────

    @Test
    void shortlistApplication_Success() {
        JobApplication shortlisted = JobApplication.builder()
                .applicationId(1L)
                .jobId(1L)
                .jobSeekerId(2L)
                .employerId(1L)
                .status(ApplicationStatus.SHORTLISTED)
                .appliedAt(LocalDateTime.now())
                .build();

        when(applicationRepository
                .findByApplicationIdAndEmployerId(1L, 1L))
                .thenReturn(Optional.of(jobApplication));
        when(applicationRepository.save(any())).thenReturn(shortlisted);
        when(jobServiceClient.getJobById(1L, AUTH)).thenReturn(jobResponse);
        when(userServiceClient.getUserById(2L, AUTH))
                .thenReturn(jobSeekerResponse);
        when(userServiceClient.getUserById(1L, AUTH))
                .thenReturn(employerResponse);

        ApplicationResponse response =
                applicationService.shortlistApplication(1L, 1L, AUTH);

        assertEquals(ApplicationStatus.SHORTLISTED, response.getStatus());
    }

    @Test
    void shortlistApplication_AlreadyShortlisted_ThrowsException() {
        jobApplication.setStatus(ApplicationStatus.SHORTLISTED);
        when(applicationRepository
                .findByApplicationIdAndEmployerId(1L, 1L))
                .thenReturn(Optional.of(jobApplication));

        assertThrows(IllegalStateException.class,
                () -> applicationService.shortlistApplication(1L, 1L, AUTH));
    }

    @Test
    void shortlistApplication_NotFound_ThrowsException() {
        when(applicationRepository
                .findByApplicationIdAndEmployerId(99L, 1L))
                .thenReturn(Optional.empty());

        assertThrows(ApplicationNotFoundException.class,
                () -> applicationService.shortlistApplication(99L, 1L, AUTH));
    }

    // ── Reject Tests ──────────────────────────────────────────────────────────

    @Test
    void rejectApplication_Success() {
        JobApplication rejected = JobApplication.builder()
                .applicationId(1L)
                .jobId(1L)
                .jobSeekerId(2L)
                .employerId(1L)
                .status(ApplicationStatus.REJECTED)
                .appliedAt(LocalDateTime.now())
                .build();

        when(applicationRepository
                .findByApplicationIdAndEmployerId(1L, 1L))
                .thenReturn(Optional.of(jobApplication));
        when(applicationRepository.save(any())).thenReturn(rejected);
        when(jobServiceClient.getJobById(1L, AUTH)).thenReturn(jobResponse);
        when(userServiceClient.getUserById(2L, AUTH))
                .thenReturn(jobSeekerResponse);
        when(userServiceClient.getUserById(1L, AUTH))
                .thenReturn(employerResponse);

        ApplicationResponse response =
                applicationService.rejectApplication(1L, 1L, AUTH);

        assertEquals(ApplicationStatus.REJECTED, response.getStatus());
    }

    @Test
    void rejectApplication_AlreadyRejected_ThrowsException() {
        jobApplication.setStatus(ApplicationStatus.REJECTED);
        when(applicationRepository
                .findByApplicationIdAndEmployerId(1L, 1L))
                .thenReturn(Optional.of(jobApplication));

        assertThrows(IllegalStateException.class,
                () -> applicationService.rejectApplication(1L, 1L, AUTH));
    }

    // ── Withdraw Tests ────────────────────────────────────────────────────────

    @Test
    void withdrawApplication_Success() {
        when(applicationRepository
                .findByApplicationIdAndJobSeekerId(1L, 2L))
                .thenReturn(Optional.of(jobApplication));

        Map<String, String> response =
                applicationService.withdrawApplication(1L, 2L);

        assertEquals("Application withdrawn successfully",
                response.get("message"));
        verify(applicationRepository).delete(jobApplication);
    }

    @Test
    void withdrawApplication_AlreadyShortlisted_ThrowsException() {
        jobApplication.setStatus(ApplicationStatus.SHORTLISTED);
        when(applicationRepository
                .findByApplicationIdAndJobSeekerId(1L, 2L))
                .thenReturn(Optional.of(jobApplication));

        assertThrows(IllegalStateException.class,
                () -> applicationService.withdrawApplication(1L, 2L));
        verify(applicationRepository, never()).delete(any());
    }

    // ── Count Tests ───────────────────────────────────────────────────────────

    @Test
    void countApplicationsForJob_ReturnsCount() {
        when(applicationRepository.countByJobId(1L)).thenReturn(5L);

        Map<String, Long> result =
                applicationService.countApplicationsForJob(1L);

        assertEquals(5L, result.get("totalApplications"));
        assertEquals(1L, result.get("jobId"));
    }
}
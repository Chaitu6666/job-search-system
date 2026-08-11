package com.jobserach.job_service.service;


import com.jobserach.job_service.dto.JobRequest;
import com.jobserach.job_service.dto.JobResponse;
import com.jobserach.job_service.dto.UserResponse;
import com.jobserach.job_service.entity.Job;
import com.jobserach.job_service.enums.JobStatus;
import com.jobserach.job_service.exception.JobNotFoundException;
import com.jobserach.job_service.feignClient.UserServiceClient;
import com.jobserach.job_service.repository.JobRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobServiceTest {

    @Mock private JobRepository jobRepository;
    @Mock private UserServiceClient userServiceClient;

    @InjectMocks private JobService jobService;

    private static final String AUTH_HEADER = "Bearer test.token";
    private Job job;
    private JobRequest jobRequest;
    private UserResponse employerResponse;

    @BeforeEach
    void setUp() {
        jobRequest = new JobRequest();
        jobRequest.setJobTitle("Java Backend Developer");
        jobRequest.setLocation("Hyderabad");
        jobRequest.setDescription("Looking for Java developer");
        jobRequest.setExperience("2-4 years");
        jobRequest.setSalary("8-15 LPA");
        jobRequest.setNoticePeriod("30 days");
        jobRequest.setContactEmail("hr@techcorp.com");
        jobRequest.setRequiredSkills("Java,Spring Boot");
        jobRequest.setCompanyName("Tech Corp");
        jobRequest.setDesignation("Software Engineer");

        job = Job.builder()
                .jobId(1L)
                .employerId(1L)
                .jobTitle("Java Backend Developer")
                .location("Hyderabad")
                .description("Looking for Java developer")
                .experience("2-4 years")
                .salary("8-15 LPA")
                .noticePeriod("30 days")
                .contactEmail("hr@techcorp.com")
                .status(JobStatus.ACTIVE)
                .requiredSkills("Java,Spring Boot")
                .companyName("Tech Corp")
                .designation("Software Engineer")
                .createdAt(LocalDateTime.now())
                .build();

        employerResponse = new UserResponse();
        employerResponse.setId(1L);
        employerResponse.setUsername("emp1");
        employerResponse.setEmail("emp1@gmail.com");
        employerResponse.setOrgName("Tech Corp");
        employerResponse.setUserType("EMPLOYER");
    }

    // ── Post Job Tests ────────────────────────────────────────────────────────

    @Test
    void postJob_Success() {
        when(userServiceClient.getUserById(1L, AUTH_HEADER))
                .thenReturn(employerResponse);
        when(jobRepository.save(any(Job.class))).thenReturn(job);

        JobResponse response = jobService.postJob(jobRequest, 1L, AUTH_HEADER);

        assertNotNull(response);
        assertEquals(1L, response.getJobId());
        assertEquals("Java Backend Developer", response.getJobTitle());
        assertEquals("Tech Corp", response.getEmployerName());
        assertEquals(JobStatus.ACTIVE, response.getStatus());
        verify(jobRepository).save(any(Job.class));
    }

    @Test
    void postJob_NotEmployer_ThrowsException() {
        UserResponse jobSeeker = new UserResponse();
        jobSeeker.setUserType("JOB_SEEKER");

        when(userServiceClient.getUserById(2L, AUTH_HEADER))
                .thenReturn(jobSeeker);

        assertThrows(IllegalArgumentException.class,
                () -> jobService.postJob(jobRequest, 2L, AUTH_HEADER));
        verify(jobRepository, never()).save(any());
    }

    // ── Edit Job Tests ────────────────────────────────────────────────────────

    @Test
    void editJob_Success() {
        jobRequest.setJobTitle("Senior Java Developer");
        Job updatedJob = Job.builder()
                .jobId(1L)
                .employerId(1L)
                .jobTitle("Senior Java Developer")
                .location("Hyderabad")
                .status(JobStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();

        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));
        when(jobRepository.save(any(Job.class))).thenReturn(updatedJob);
        when(userServiceClient.getUserById(1L, AUTH_HEADER))
                .thenReturn(employerResponse);

        JobResponse response = jobService.editJob(1L, jobRequest, 1L, AUTH_HEADER);

        assertNotNull(response);
        assertEquals("Senior Java Developer", response.getJobTitle());
    }

    @Test
    void editJob_JobNotFound_ThrowsException() {
        when(jobRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(JobNotFoundException.class,
                () -> jobService.editJob(99L, jobRequest, 1L, AUTH_HEADER));
    }

    @Test
    void editJob_NotOwner_ThrowsSecurityException() {
        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));

        // employerId in job is 1L but we pass 2L
        assertThrows(SecurityException.class,
                () -> jobService.editJob(1L, jobRequest, 2L, AUTH_HEADER));
    }

    // ── Delete Job Tests ──────────────────────────────────────────────────────

    @Test
    void deleteJob_Success() {
        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));

        Map<String, String> response = jobService.deleteJob(1L, 1L);

        assertEquals("Job deleted successfully", response.get("message: "));
        verify(jobRepository).delete(job);
    }

    @Test
    void deleteJob_NotOwner_ThrowsSecurityException() {
        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));

        assertThrows(SecurityException.class,
                () -> jobService.deleteJob(1L, 2L));
        verify(jobRepository, never()).delete(any());
    }

    @Test
    void deleteJob_NotFound_ThrowsException() {
        when(jobRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(JobNotFoundException.class,
                () -> jobService.deleteJob(99L, 1L));
    }

    // ── Get Job Tests ─────────────────────────────────────────────────────────

    @Test
    void getJobById_Success() {
        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));
        when(userServiceClient.getUserById(1L, AUTH_HEADER))
                .thenReturn(employerResponse);

        JobResponse response = jobService.getJobById(1L, AUTH_HEADER);

        assertNotNull(response);
        assertEquals(1L, response.getJobId());
        assertEquals("Java Backend Developer", response.getJobTitle());
    }

    @Test
    void getJobById_NotFound_ThrowsException() {
        when(jobRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(JobNotFoundException.class,
                () -> jobService.getJobById(99L, AUTH_HEADER));
    }

    // ── Search Tests ──────────────────────────────────────────────────────────

    @Test
    void searchByKeyword_ReturnsResults() {
        when(jobRepository.searchByKeyword("Java"))
                .thenReturn(List.of(job));
        when(userServiceClient.getUserById(1L, AUTH_HEADER))
                .thenReturn(employerResponse);

        List<JobResponse> results = jobService.searchByKeyword("Java", AUTH_HEADER);

        assertEquals(1, results.size());
        assertEquals("Java Backend Developer", results.get(0).getJobTitle());
    }

    @Test
    void searchByKeyword_NoResults_ReturnsEmptyList() {
        when(jobRepository.searchByKeyword("Python")).thenReturn(List.of());

        List<JobResponse> results = jobService.searchByKeyword("Python", AUTH_HEADER);

        assertTrue(results.isEmpty());
    }

    @Test
    void searchByLocation_ReturnsResults() {
        when(jobRepository.findByLocationContainingIgnoreCaseAndStatus(
                "Hyderabad", JobStatus.ACTIVE))
                .thenReturn(List.of(job));
        when(userServiceClient.getUserById(1L, AUTH_HEADER))
                .thenReturn(employerResponse);

        List<JobResponse> results =
                jobService.searchByLocation("Hyderabad", AUTH_HEADER);

        assertEquals(1, results.size());
    }

    @Test
    void getJobsByEmployer_ReturnsResults() {
        when(userServiceClient.getUserById(1L, AUTH_HEADER))
                .thenReturn(employerResponse);
        when(jobRepository.findByEmployerId(1L)).thenReturn(List.of(job));

        List<JobResponse> results =
                jobService.getJobsByEmployer(1L, AUTH_HEADER);

        assertEquals(1, results.size());
    }
}
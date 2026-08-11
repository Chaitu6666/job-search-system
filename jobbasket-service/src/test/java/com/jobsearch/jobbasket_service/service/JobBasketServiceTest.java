package com.jobsearch.jobbasket_service.service;

import com.jobsearch.jobbasket_service.dto.*;
import com.jobsearch.jobbasket_service.entity.JobBasket;
import com.jobsearch.jobbasket_service.enums.BasketStatus;
import com.jobsearch.jobbasket_service.exception.BasketNotFoundException;

import com.jobsearch.jobbasket_service.feignClient.ApplicationServiceClient;
import com.jobsearch.jobbasket_service.feignClient.JobServiceClient;
import com.jobsearch.jobbasket_service.feignClient.UserServiceClient;
import com.jobsearch.jobbasket_service.repository.JobBasketRepository;
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
class JobBasketServiceTest {

    @Mock private JobBasketRepository jobBasketRepository;
    @Mock private UserServiceClient userServiceClient;
    @Mock private JobServiceClient jobServiceClient;
    @Mock private ApplicationServiceClient applicationServiceClient;

    @InjectMocks private JobBasketService jobBasketService;

    private static final String AUTH = "Bearer test.token";
    private BasketRequest basketRequest;
    private JobBasket savedBasket;
    private JobBasket appliedBasket;
    private UserResponse jobSeekerResponse;
    private JobResponse jobResponse;

    @BeforeEach
    void setUp() {
        basketRequest = new BasketRequest();
        basketRequest.setJobId(1L);

        savedBasket = JobBasket.builder()
                .basketId(1L)
                .jobSeekerId(2L)
                .jobId(1L)
                .status(BasketStatus.SAVED)
                .addedAt(LocalDateTime.now())
                .build();

        appliedBasket = JobBasket.builder()
                .basketId(1L)
                .jobSeekerId(2L)
                .jobId(1L)
                .status(BasketStatus.APPLIED)
                .addedAt(LocalDateTime.now())
                .build();

        jobSeekerResponse = new UserResponse();
        jobSeekerResponse.setId(2L);
        jobSeekerResponse.setName("Ravi Kumar");
        jobSeekerResponse.setUserType("JOB_SEEKER");

        jobResponse = new JobResponse();
        jobResponse.setJobId(1L);
        jobResponse.setJobTitle("Java Backend Developer");
        jobResponse.setCompanyName("Tech Corp");
        jobResponse.setLocation("Hyderabad");
        jobResponse.setExperience("2-4 years");
        jobResponse.setSalary("8-15 LPA");
        jobResponse.setStatus("ACTIVE");
        jobResponse.setRequiredSkills("Java,Spring Boot");
        jobResponse.setDesignation("Software Engineer");
    }

    // ── Add to Basket Tests ───────────────────────────────────────────────────

    @Test
    void addToBasket_Success() {
        when(userServiceClient.getUserById(2L, AUTH))
                .thenReturn(jobSeekerResponse);
        when(jobServiceClient.getJobById(1L, AUTH))
                .thenReturn(jobResponse);
        when(jobBasketRepository.existsByJobIdAndJobSeekerId(1L, 2L))
                .thenReturn(false);
        when(jobBasketRepository.save(any(JobBasket.class)))
                .thenReturn(savedBasket);

        BasketResponse response =
                jobBasketService.addToBasket(basketRequest, 2L, AUTH);

        assertNotNull(response);
        assertEquals(1L, response.getBasketId());
        assertEquals(BasketStatus.SAVED, response.getStatus());
        assertEquals("Java Backend Developer", response.getJobTitle());
        verify(jobBasketRepository).save(any(JobBasket.class));
    }

    @Test
    void addToBasket_NotJobSeeker_ThrowsException() {
        UserResponse employer = new UserResponse();
        employer.setUserType("EMPLOYER");
        when(userServiceClient.getUserById(1L, AUTH)).thenReturn(employer);

        assertThrows(IllegalArgumentException.class,
                () -> jobBasketService.addToBasket(basketRequest, 1L, AUTH));
        verify(jobBasketRepository, never()).save(any());
    }

    @Test
    void addToBasket_JobNotActive_ThrowsException() {
        jobResponse.setStatus("CLOSED");
        when(userServiceClient.getUserById(2L, AUTH))
                .thenReturn(jobSeekerResponse);
        when(jobServiceClient.getJobById(1L, AUTH))
                .thenReturn(jobResponse);

        assertThrows(IllegalStateException.class,
                () -> jobBasketService.addToBasket(basketRequest, 2L, AUTH));
    }

    @Test
    void addToBasket_AlreadyInBasket_ThrowsException() {
        when(userServiceClient.getUserById(2L, AUTH))
                .thenReturn(jobSeekerResponse);
        when(jobServiceClient.getJobById(1L, AUTH))
                .thenReturn(jobResponse);
        when(jobBasketRepository.existsByJobIdAndJobSeekerId(1L, 2L))
                .thenReturn(true);

        assertThrows(IllegalStateException.class,
                () -> jobBasketService.addToBasket(basketRequest, 2L, AUTH));
    }

    // ── View Basket Tests ─────────────────────────────────────────────────────

    @Test
    void viewBasket_ReturnsItems() {
        when(userServiceClient.getUserById(2L, AUTH))
                .thenReturn(jobSeekerResponse);
        when(jobBasketRepository.findByJobSeekerId(2L))
                .thenReturn(List.of(savedBasket));
        when(jobServiceClient.getJobById(1L, AUTH))
                .thenReturn(jobResponse);

        List<BasketResponse> result =
                jobBasketService.viewBasket(2L, AUTH);

        assertEquals(1, result.size());
        assertEquals(BasketStatus.SAVED, result.get(0).getStatus());
    }

    @Test
    void viewBasket_EmptyBasket_ReturnsEmptyList() {
        when(userServiceClient.getUserById(2L, AUTH))
                .thenReturn(jobSeekerResponse);
        when(jobBasketRepository.findByJobSeekerId(2L))
                .thenReturn(List.of());

        List<BasketResponse> result =
                jobBasketService.viewBasket(2L, AUTH);

        assertTrue(result.isEmpty());
    }

    // ── Remove from Basket Tests ──────────────────────────────────────────────

    @Test
    void removeFromBasket_Success() {
        when(jobBasketRepository
                .findByBasketIdAndJobSeekerId(1L, 2L))
                .thenReturn(Optional.of(savedBasket));

        Map<String, String> result =
                jobBasketService.removeFromBasket(1L, 2L);

        assertEquals("Job removed from basket successfully",
                result.get("message"));
        verify(jobBasketRepository).delete(savedBasket);
    }

    @Test
    void removeFromBasket_NotFound_ThrowsException() {
        when(jobBasketRepository
                .findByBasketIdAndJobSeekerId(99L, 2L))
                .thenReturn(Optional.empty());

        assertThrows(BasketNotFoundException.class,
                () -> jobBasketService.removeFromBasket(99L, 2L));
        verify(jobBasketRepository, never()).delete(any());
    }

    // ── Apply from Basket Tests ───────────────────────────────────────────────

    @Test
    void applyFromBasket_Success() {
        when(jobBasketRepository
                .findByBasketIdAndJobSeekerId(1L, 2L))
                .thenReturn(Optional.of(savedBasket));
        when(jobBasketRepository.save(any())).thenReturn(appliedBasket);

        Map<String, String> result =
                jobBasketService.applyFromBasket(1L, 2L, "Cover letter", AUTH);

        assertEquals("Successfully applied for the job from basket",
                result.get("message"));
        verify(applicationServiceClient).applyForJob(any(), eq(AUTH), eq(2L));
        verify(jobBasketRepository).save(any());
    }

    @Test
    void applyFromBasket_AlreadyApplied_ThrowsException() {
        when(jobBasketRepository
                .findByBasketIdAndJobSeekerId(1L, 2L))
                .thenReturn(Optional.of(appliedBasket));

        assertThrows(IllegalStateException.class,
                () -> jobBasketService.applyFromBasket(
                        1L, 2L, "Cover letter", AUTH));
        verify(applicationServiceClient, never()).applyForJob(any(), any(), any());
    }

    @Test
    void applyFromBasket_NotFound_ThrowsException() {
        when(jobBasketRepository
                .findByBasketIdAndJobSeekerId(99L, 2L))
                .thenReturn(Optional.empty());

        assertThrows(BasketNotFoundException.class,
                () -> jobBasketService.applyFromBasket(
                        99L, 2L, "Cover letter", AUTH));
    }

    // ── Check in Basket Tests ─────────────────────────────────────────────────

    @Test
    void isJobInBasket_True() {
        when(jobBasketRepository.existsByJobIdAndJobSeekerId(1L, 2L))
                .thenReturn(true);
        when(jobBasketRepository.findByJobIdAndJobSeekerId(1L, 2L))
                .thenReturn(Optional.of(savedBasket));

        Map<String, Object> result =
                jobBasketService.isJobInBasket(1L, 2L);

        assertEquals(true, result.get("inBasket"));
        assertEquals("SAVED", result.get("status"));
    }

    @Test
    void isJobInBasket_False() {
        when(jobBasketRepository.existsByJobIdAndJobSeekerId(1L, 2L))
                .thenReturn(false);

        Map<String, Object> result =
                jobBasketService.isJobInBasket(1L, 2L);

        assertEquals(false, result.get("inBasket"));
        assertEquals("NOT_IN_BASKET", result.get("status"));
    }

    // ── Summary Tests ─────────────────────────────────────────────────────────

    @Test
    void getBasketSummary_ReturnsCorrectCounts() {
        when(jobBasketRepository.countByJobSeekerIdAndStatus(
                2L, BasketStatus.SAVED)).thenReturn(3L);
        when(jobBasketRepository.countByJobSeekerIdAndStatus(
                2L, BasketStatus.APPLIED)).thenReturn(1L);

        Map<String, Long> result =
                jobBasketService.getBasketSummary(2L);

        assertEquals(3L, result.get("savedJobs"));
        assertEquals(1L, result.get("appliedJobs"));
        assertEquals(4L, result.get("totalJobs"));
    }

    // ── Clear Basket Tests ────────────────────────────────────────────────────

    @Test
    void clearBasket_Success() {
        Map<String, String> result =
                jobBasketService.clearBasket(2L);

        assertEquals("Basket cleared successfully", result.get("message"));
        verify(jobBasketRepository).deleteAllByJobSeekerId(2L);
    }
}
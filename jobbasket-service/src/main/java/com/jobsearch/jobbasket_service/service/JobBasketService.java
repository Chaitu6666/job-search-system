package com.jobsearch.jobbasket_service.service;

import com.jobsearch.jobbasket_service.dto.*;
import com.jobsearch.jobbasket_service.entity.JobBasket;
import com.jobsearch.jobbasket_service.enums.BasketStatus;
import com.jobsearch.jobbasket_service.exception.BasketNotFoundException;
import com.jobsearch.jobbasket_service.feignClient.ApplicationServiceClient;
import com.jobsearch.jobbasket_service.feignClient.JobServiceClient;
import com.jobsearch.jobbasket_service.feignClient.UserServiceClient;
import com.jobsearch.jobbasket_service.repository.JobBasketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobBasketService {

    private final JobBasketRepository jobBasketRepository;
    private final UserServiceClient userServiceClient;
    private final JobServiceClient jobServiceClient;
    private final ApplicationServiceClient applicationServiceClient;

    // ── ADD JOB TO BASKET ─────────────────────────────────────────────────────
    public BasketResponse addToBasket(BasketRequest request,
                                      Long jobSeekerId,
                                      String authHeader) {
        log.info("Add to basket — jobSeekerId: {}, jobId: {}",
                jobSeekerId, request.getJobId());
        // Verify user is a JOB_SEEKER
        UserResponse jobSeeker = userServiceClient.getUserById(
                jobSeekerId, authHeader);
        if (!"JOB_SEEKER".equals(jobSeeker.getUserType())) {
            throw new IllegalArgumentException(
                    "Only job seekers can add jobs to basket");
        }

        // Verify job exists and is ACTIVE
        JobResponse job = jobServiceClient.getJobById(
                request.getJobId(), authHeader);
        if (!"ACTIVE".equals(job.getStatus())) {
            throw new IllegalStateException(
                    "Cannot save — this job is no longer active");
        }

        // Prevent duplicate basket entries
        if (jobBasketRepository.existsByJobIdAndJobSeekerId(
                request.getJobId(), jobSeekerId)) {
            throw new IllegalStateException(
                    "This job is already in your basket");
        }

        JobBasket basket = JobBasket.builder()
                .jobSeekerId(jobSeekerId)
                .jobId(request.getJobId())
                .build();

        JobBasket saved = jobBasketRepository.save(basket);
        log.info("Job added to basket — basketId: {}, jobSeekerId: {}, jobId: {}",
                saved.getBasketId(), jobSeekerId, request.getJobId());
        return mapToResponse(saved, job, jobSeeker);
    }

    // ── VIEW FULL BASKET ──────────────────────────────────────────────────────
    public List<BasketResponse> viewBasket(Long jobSeekerId,
                                           String authHeader) {
        UserResponse jobSeeker = userServiceClient.getUserById(
                jobSeekerId, authHeader);

        return jobBasketRepository.findByJobSeekerId(jobSeekerId)
                .stream()
                .map(basket -> {
                    JobResponse job = jobServiceClient.getJobById(
                            basket.getJobId(), authHeader);
                    return mapToResponse(basket, job, jobSeeker);
                })
                .collect(Collectors.toList());
    }

    // ── VIEW BASKET BY STATUS ─────────────────────────────────────────────────
    public List<BasketResponse> viewBasketByStatus(Long jobSeekerId,
                                                   BasketStatus status,
                                                   String authHeader) {
        UserResponse jobSeeker = userServiceClient.getUserById(
                jobSeekerId, authHeader);

        return jobBasketRepository.findByJobSeekerIdAndStatus(
                        jobSeekerId, status)
                .stream()
                .map(basket -> {
                    JobResponse job = jobServiceClient.getJobById(
                            basket.getJobId(), authHeader);
                    return mapToResponse(basket, job, jobSeeker);
                })
                .collect(Collectors.toList());
    }

    // ── REMOVE JOB FROM BASKET ────────────────────────────────────────────────
    public Map<String, String> removeFromBasket(Long basketId,
                                                Long jobSeekerId) {
        JobBasket basket = jobBasketRepository
                .findByBasketIdAndJobSeekerId(basketId, jobSeekerId)
                .orElseThrow(() -> new BasketNotFoundException(
                        "Basket item not found or you are not authorized"));

        jobBasketRepository.delete(basket);
        return Map.of("message", "Job removed from basket successfully");
    }

    // ── APPLY FOR A JOB FROM BASKET ───────────────────────────────────────────
    // Delegates to application-service via Feign
    public Map<String, String> applyFromBasket(Long basketId,
                                               Long jobSeekerId,
                                               String coverLetter,
                                               String authHeader) {
        log.info("Apply from basket — basketId: {}, jobSeekerId: {}",
                basketId, jobSeekerId);
        JobBasket basket = jobBasketRepository
                .findByBasketIdAndJobSeekerId(basketId, jobSeekerId)
                .orElseThrow(() -> new BasketNotFoundException(
                        "Basket item not found or you are not authorized"));

        if (basket.getStatus() == BasketStatus.APPLIED) {
            throw new IllegalStateException(
                    "You have already applied for this job from the basket");
        }

        // Call application-service via Feign to create the application
        ApplicationRequest appRequest = new ApplicationRequest();
        appRequest.setJobId(basket.getJobId());
        appRequest.setCoverLetter(coverLetter);

        applicationServiceClient.applyForJob(
                appRequest, authHeader, jobSeekerId);

        // Update basket status to APPLIED
        basket.setStatus(BasketStatus.APPLIED);
        jobBasketRepository.save(basket);
        log.info("Applied from basket — basketId: {}, jobId: {}",
                basket.getBasketId(), basket.getJobId());
        return Map.of("message",
                "Successfully applied for the job from basket");
    }

    // ── GET BASKET ITEM BY ID ─────────────────────────────────────────────────
    public BasketResponse getBasketItemById(Long basketId,
                                            Long jobSeekerId,
                                            String authHeader) {
        JobBasket basket = jobBasketRepository
                .findByBasketIdAndJobSeekerId(basketId, jobSeekerId)
                .orElseThrow(() -> new BasketNotFoundException(
                        "Basket item not found or you are not authorized"));

        UserResponse jobSeeker = userServiceClient.getUserById(
                jobSeekerId, authHeader);
        JobResponse job = jobServiceClient.getJobById(
                basket.getJobId(), authHeader);

        return mapToResponse(basket, job, jobSeeker);
    }

    // ── CHECK IF JOB IS IN BASKET ─────────────────────────────────────────────
    public Map<String, Object> isJobInBasket(Long jobId,
                                             Long jobSeekerId) {
        boolean exists = jobBasketRepository.existsByJobIdAndJobSeekerId(
                jobId, jobSeekerId);

        BasketStatus status = null;
        if (exists) {
            status = jobBasketRepository
                    .findByJobIdAndJobSeekerId(jobId, jobSeekerId)
                    .map(JobBasket::getStatus)
                    .orElse(null);
        }

        return Map.of(
                "jobId",     jobId,
                "inBasket",  exists,
                "status",    status != null ? status.name() : "NOT_IN_BASKET"
        );
    }

    // ── BASKET SUMMARY ────────────────────────────────────────────────────────
    public Map<String, Long> getBasketSummary(Long jobSeekerId) {
        long saved = jobBasketRepository.countByJobSeekerIdAndStatus(
                jobSeekerId, BasketStatus.SAVED);
        long applied = jobBasketRepository.countByJobSeekerIdAndStatus(
                jobSeekerId, BasketStatus.APPLIED);
        return Map.of(
                "savedJobs",   saved,
                "appliedJobs", applied,
                "totalJobs",   saved + applied
        );
    }

    // ── CLEAR BASKET ──────────────────────────────────────────────────────────
    @Transactional
    public Map<String, String> clearBasket(Long jobSeekerId) {
        log.info("Clear basket — jobSeekerId: {}", jobSeekerId);
        jobBasketRepository.deleteAllByJobSeekerId(jobSeekerId);
        log.info("Basket cleared for jobSeekerId: {}", jobSeekerId);
        return Map.of("message", "Basket cleared successfully");
    }

    // ── MAPPER ────────────────────────────────────────────────────────────────
    private BasketResponse mapToResponse(JobBasket basket,
                                         JobResponse job,
                                         UserResponse jobSeeker) {
        return BasketResponse.builder()
                .basketId(basket.getBasketId())
                .jobSeekerId(basket.getJobSeekerId())
                .jobSeekerName(jobSeeker != null ? jobSeeker.getName() : "N/A")
                .jobId(basket.getJobId())
                .jobTitle(job != null ? job.getJobTitle() : "N/A")
                .companyName(job != null ? job.getCompanyName() : "N/A")
                .location(job != null ? job.getLocation() : "N/A")
                .experience(job != null ? job.getExperience() : "N/A")
                .salary(job != null ? job.getSalary() : "N/A")
                .requiredSkills(job != null ? job.getRequiredSkills() : "N/A")
                .designation(job != null ? job.getDesignation() : "N/A")
                .jobStatus(job != null ? job.getStatus() : "N/A")
                .status(basket.getStatus())
                .addedAt(basket.getAddedAt())
                .build();
    }
}
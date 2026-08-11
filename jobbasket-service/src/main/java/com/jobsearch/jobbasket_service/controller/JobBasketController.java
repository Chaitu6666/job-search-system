package com.jobsearch.jobbasket_service.controller;

import com.jobsearch.jobbasket_service.dto.BasketRequest;
import com.jobsearch.jobbasket_service.dto.BasketResponse;
import com.jobsearch.jobbasket_service.enums.BasketStatus;
import com.jobsearch.jobbasket_service.service.JobBasketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/basket")
@RequiredArgsConstructor
public class JobBasketController {

    private final JobBasketService jobBasketService;

    // ── Add a job to basket ───────────────────────────────────────────────────
    @PostMapping
    public ResponseEntity<BasketResponse> addToBasket(
            @Valid @RequestBody BasketRequest request,
            @RequestHeader("Authorization") String authHeader,
            @RequestHeader("X-User-Id") Long jobSeekerId) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(jobBasketService.addToBasket(
                        request, jobSeekerId, authHeader));
    }

    // ── View full basket ──────────────────────────────────────────────────────
    @GetMapping
    public ResponseEntity<List<BasketResponse>> viewBasket(
            @RequestHeader("Authorization") String authHeader,
            @RequestHeader("X-User-Id") Long jobSeekerId) {

        return ResponseEntity.ok(
                jobBasketService.viewBasket(jobSeekerId, authHeader));
    }

    // ── View basket filtered by status ────────────────────────────────────────
    @GetMapping("/status/{status}")
    public ResponseEntity<List<BasketResponse>> viewBasketByStatus(
            @PathVariable BasketStatus status,
            @RequestHeader("Authorization") String authHeader,
            @RequestHeader("X-User-Id") Long jobSeekerId) {

        return ResponseEntity.ok(
                jobBasketService.viewBasketByStatus(
                        jobSeekerId, status, authHeader));
    }

    // ── Get single basket item by ID ──────────────────────────────────────────
    @GetMapping("/{basketId}")
    public ResponseEntity<BasketResponse> getBasketItemById(
            @PathVariable Long basketId,
            @RequestHeader("Authorization") String authHeader,
            @RequestHeader("X-User-Id") Long jobSeekerId) {

        return ResponseEntity.ok(
                jobBasketService.getBasketItemById(
                        basketId, jobSeekerId, authHeader));
    }

    // ── Check if a specific job is in the basket ──────────────────────────────
    @GetMapping("/check/{jobId}")
    public ResponseEntity<Map<String, Object>> checkJobInBasket(
            @PathVariable Long jobId,
            @RequestHeader("X-User-Id") Long jobSeekerId) {

        return ResponseEntity.ok(
                jobBasketService.isJobInBasket(jobId, jobSeekerId));
    }

    // ── Basket summary (saved count, applied count, total) ────────────────────
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Long>> getBasketSummary(
            @RequestHeader("X-User-Id") Long jobSeekerId) {

        return ResponseEntity.ok(
                jobBasketService.getBasketSummary(jobSeekerId));
    }

    // ── Remove a job from basket ──────────────────────────────────────────────
    @DeleteMapping("/{basketId}")
    public ResponseEntity<Map<String, String>> removeFromBasket(
            @PathVariable Long basketId,
            @RequestHeader("X-User-Id") Long jobSeekerId) {

        return ResponseEntity.ok(
                jobBasketService.removeFromBasket(basketId, jobSeekerId));
    }

    // ── Apply for a single job from basket ────────────────────────────────────
    @PostMapping("/{basketId}/apply")
    public ResponseEntity<Map<String, String>> applyFromBasket(
            @PathVariable Long basketId,
            @RequestHeader("Authorization") String authHeader,
            @RequestHeader("X-User-Id") Long jobSeekerId,
            @RequestParam(required = false) String coverLetter) {

        return ResponseEntity.ok(
                jobBasketService.applyFromBasket(
                        basketId, jobSeekerId, coverLetter, authHeader));
    }

    // ── Clear entire basket ───────────────────────────────────────────────────
    @DeleteMapping("/clear")
    public ResponseEntity<Map<String, String>> clearBasket(
            @RequestHeader("X-User-Id") Long jobSeekerId) {

        return ResponseEntity.ok(
                jobBasketService.clearBasket(jobSeekerId));
    }
}

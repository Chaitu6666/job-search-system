package com.jobsearch.message_service.controller;

import com.jobsearch.message_service.dto.MessageRequest;
import com.jobsearch.message_service.dto.MessageResponse;
import com.jobsearch.message_service.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    // ── Job Seeker sends message to Employer ──────────────────────────────────
    @PostMapping("/jobseeker/send")
    public ResponseEntity<MessageResponse> jobSeekerSends(
            @Valid @RequestBody MessageRequest request,
            @RequestHeader("Authorization") String authHeader,
            @RequestHeader("X-User-Id") Long jobSeekerId) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(messageService.jobSeekerSendsMessage(
                        request, jobSeekerId, authHeader));
    }

    // ── Employer sends message to Job Seeker ──────────────────────────────────
    @PostMapping("/employer/send")
    public ResponseEntity<MessageResponse> employerSends(
            @Valid @RequestBody MessageRequest request,
            @RequestHeader("Authorization") String authHeader,
            @RequestHeader("X-User-Id") Long employerId) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(messageService.employerSendsMessage(
                        request, employerId, authHeader));
    }

    // ── Get full conversation thread between a seeker and employer ────────────
    @GetMapping("/thread")
    public ResponseEntity<List<MessageResponse>> getThread(
            @RequestParam Long jobSeekerId,
            @RequestParam Long employerId,
            @RequestHeader("Authorization") String authHeader) {

        return ResponseEntity.ok(
                messageService.getConversationThread(
                        jobSeekerId, employerId, authHeader));
    }

    // ── Get conversation thread filtered by job ───────────────────────────────
    @GetMapping("/thread/job")
    public ResponseEntity<List<MessageResponse>> getThreadByJob(
            @RequestParam Long jobSeekerId,
            @RequestParam Long employerId,
            @RequestParam Long jobId,
            @RequestHeader("Authorization") String authHeader) {

        return ResponseEntity.ok(
                messageService.getConversationByJob(
                        jobSeekerId, employerId, jobId, authHeader));
    }

    // ── Job Seeker: View inbox (messages from employers) ──────────────────────
    @GetMapping("/jobseeker/inbox")
    public ResponseEntity<List<MessageResponse>> jobSeekerInbox(
            @RequestHeader("Authorization") String authHeader,
            @RequestHeader("X-User-Id") Long jobSeekerId) {

        return ResponseEntity.ok(
                messageService.getJobSeekerInbox(jobSeekerId, authHeader));
    }

    // ── Job Seeker: View sent messages ────────────────────────────────────────
    @GetMapping("/jobseeker/sent")
    public ResponseEntity<List<MessageResponse>> jobSeekerSent(
            @RequestHeader("Authorization") String authHeader,
            @RequestHeader("X-User-Id") Long jobSeekerId) {

        return ResponseEntity.ok(
                messageService.getJobSeekerSentMessages(
                        jobSeekerId, authHeader));
    }

    // ── Employer: View inbox (messages from job seekers) ─────────────────────
    @GetMapping("/employer/inbox")
    public ResponseEntity<List<MessageResponse>> employerInbox(
            @RequestHeader("Authorization") String authHeader,
            @RequestHeader("X-User-Id") Long employerId) {

        return ResponseEntity.ok(
                messageService.getEmployerInbox(employerId, authHeader));
    }

    // ── Employer: View sent messages ──────────────────────────────────────────
    @GetMapping("/employer/sent")
    public ResponseEntity<List<MessageResponse>> employerSent(
            @RequestHeader("Authorization") String authHeader,
            @RequestHeader("X-User-Id") Long employerId) {

        return ResponseEntity.ok(
                messageService.getEmployerSentMessages(employerId, authHeader));
    }

    // ── Employer: View all messages related to a job ──────────────────────────
    @GetMapping("/job/{jobId}")
    public ResponseEntity<List<MessageResponse>> messagesByJob(
            @PathVariable Long jobId,
            @RequestHeader("Authorization") String authHeader) {

        return ResponseEntity.ok(
                messageService.getMessagesByJobId(jobId, authHeader));
    }

    // ── Common: Get a single message by ID ───────────────────────────────────
    @GetMapping("/{messageId}")
    public ResponseEntity<MessageResponse> getById(
            @PathVariable Long messageId,
            @RequestHeader("Authorization") String authHeader) {

        return ResponseEntity.ok(
                messageService.getMessageById(messageId, authHeader));
    }

    // ── Mark entire thread as read ────────────────────────────────────────────
    @PutMapping("/thread/read")
    public ResponseEntity<Map<String, Object>> markThreadRead(
            @RequestParam Long jobSeekerId,
            @RequestParam Long employerId) {

        return ResponseEntity.ok(
                messageService.markThreadAsRead(jobSeekerId, employerId));
    }

    // ── Job Seeker: Get unread message count ──────────────────────────────────
    @GetMapping("/jobseeker/unread/count")
    public ResponseEntity<Map<String, Long>> jobSeekerUnreadCount(
            @RequestHeader("X-User-Id") Long jobSeekerId) {

        return ResponseEntity.ok(
                messageService.getUnreadCountForJobSeeker(jobSeekerId));
    }

    // ── Employer: Get unread message count ────────────────────────────────────
    @GetMapping("/employer/unread/count")
    public ResponseEntity<Map<String, Long>> employerUnreadCount(
            @RequestHeader("X-User-Id") Long employerId) {

        return ResponseEntity.ok(
                messageService.getUnreadCountForEmployer(employerId));
    }

    // ── Common: Delete a message ──────────────────────────────────────────────
    @DeleteMapping("/{messageId}")
    public ResponseEntity<Map<String, String>> deleteMessage(
            @PathVariable Long messageId,
            @RequestHeader("X-User-Id") Long userId) {

        return ResponseEntity.ok(
                messageService.deleteMessage(messageId, userId));
    }
}

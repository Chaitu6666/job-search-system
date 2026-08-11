package com.jobsearch.message_service.service;

import com.jobsearch.message_service.dto.JobResponse;
import com.jobsearch.message_service.dto.MessageRequest;
import com.jobsearch.message_service.dto.MessageResponse;
import com.jobsearch.message_service.dto.UserResponse;
import com.jobsearch.message_service.entity.Message;
import com.jobsearch.message_service.enums.MessageDirection;
import com.jobsearch.message_service.exception.MessageNotFoundException;
import com.jobsearch.message_service.feignClient.JobServiceClient;
import com.jobsearch.message_service.feignClient.UserServiceClient;
import com.jobsearch.message_service.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserServiceClient userServiceClient;
    private final JobServiceClient jobServiceClient;

    // ── JOB SEEKER SENDS MESSAGE TO EMPLOYER ─────────────────────────────────
    public MessageResponse jobSeekerSendsMessage(MessageRequest request,
                                                 Long jobSeekerId,
                                                 String authHeader) {
        log.info("JobSeeker sending message — jobSeekerId: {}, to employerId: {}",
                jobSeekerId, request.getEmployerId());
        if (request.getEmployerId() == null) {
            throw new IllegalArgumentException(
                    "employerId is required when job seeker sends a message");
        }

        // Verify both users exist
        UserResponse jobSeeker = userServiceClient.getUserById(
                jobSeekerId, authHeader);
        UserResponse employer = userServiceClient.getUserById(
                request.getEmployerId(), authHeader);

        if (!"JOB_SEEKER".equals(jobSeeker.getUserType())) {
            throw new IllegalArgumentException(
                    "Sender must be a JOB_SEEKER");
        }
        if (!"EMPLOYER".equals(employer.getUserType())) {
            throw new IllegalArgumentException(
                    "Recipient must be an EMPLOYER");
        }

        // Optionally verify job exists
        JobResponse job = null;
        if (request.getJobId() != null) {
            job = jobServiceClient.getJobById(request.getJobId(), authHeader);
        }

        Message message = Message.builder()
                .jobSeekerId(jobSeekerId)
                .employerId(request.getEmployerId())
                .jobId(request.getJobId())
                .description(request.getDescription())
                .direction(MessageDirection.JOBSEEKER_TO_EMPLOYER)
                .build();

        Message saved = messageRepository.save(message);
        log.info("Message sent — messageId: {}, direction: JOBSEEKER_TO_EMPLOYER",
                saved.getMessageId());
        return mapToResponse(saved, jobSeeker, employer, job);
    }

    // ── EMPLOYER SENDS MESSAGE TO JOB SEEKER ─────────────────────────────────
    public MessageResponse employerSendsMessage(MessageRequest request,
                                                Long employerId,
                                                String authHeader) {
        log.info("Employer sending message — employerId: {}, to jobSeekerId: {}",
                employerId, request.getJobSeekerId());
        if (request.getJobSeekerId() == null) {
            throw new IllegalArgumentException(
                    "jobSeekerId is required when employer sends a message");
        }

        // Verify both users exist
        UserResponse employer = userServiceClient.getUserById(
                employerId, authHeader);
        UserResponse jobSeeker = userServiceClient.getUserById(
                request.getJobSeekerId(), authHeader);

        if (!"EMPLOYER".equals(employer.getUserType())) {
            throw new IllegalArgumentException(
                    "Sender must be an EMPLOYER");
        }
        if (!"JOB_SEEKER".equals(jobSeeker.getUserType())) {
            throw new IllegalArgumentException(
                    "Recipient must be a JOB_SEEKER");
        }

        // Optionally verify job exists
        JobResponse job = null;
        if (request.getJobId() != null) {
            job = jobServiceClient.getJobById(request.getJobId(), authHeader);
        }

        Message message = Message.builder()
                .jobSeekerId(request.getJobSeekerId())
                .employerId(employerId)
                .jobId(request.getJobId())
                .description(request.getDescription())
                .direction(MessageDirection.EMPLOYER_TO_JOBSEEKER)
                .build();

        Message saved = messageRepository.save(message);
        log.info("Message sent — messageId: {}, direction: EMPLOYER_TO_JOBSEEKER",
                saved.getMessageId());

        return mapToResponse(saved, jobSeeker, employer, job);
    }

    // ── GET FULL CONVERSATION THREAD ─────────────────────────────────────────
    // All messages between a job seeker and employer (both directions)
    public List<MessageResponse> getConversationThread(Long jobSeekerId,
                                                       Long employerId,
                                                       String authHeader) {
        UserResponse jobSeeker = userServiceClient.getUserById(
                jobSeekerId, authHeader);
        UserResponse employer = userServiceClient.getUserById(
                employerId, authHeader);

        return messageRepository
                .findByJobSeekerIdAndEmployerIdOrderBySentAtAsc(
                        jobSeekerId, employerId)
                .stream()
                .map(msg -> mapToResponse(msg, jobSeeker, employer, null))
                .collect(Collectors.toList());
    }

    // ── GET CONVERSATION THREAD BY JOB ───────────────────────────────────────
    public List<MessageResponse> getConversationByJob(Long jobSeekerId,
                                                      Long employerId,
                                                      Long jobId,
                                                      String authHeader) {
        UserResponse jobSeeker = userServiceClient.getUserById(
                jobSeekerId, authHeader);
        UserResponse employer = userServiceClient.getUserById(
                employerId, authHeader);
        JobResponse job = jobServiceClient.getJobById(jobId, authHeader);

        return messageRepository
                .findByJobSeekerIdAndEmployerIdAndJobIdOrderBySentAtAsc(
                        jobSeekerId, employerId, jobId)
                .stream()
                .map(msg -> mapToResponse(msg, jobSeeker, employer, job))
                .collect(Collectors.toList());
    }

    // ── JOB SEEKER: VIEW INBOX (messages received from employers) ────────────
    public List<MessageResponse> getJobSeekerInbox(Long jobSeekerId,
                                                   String authHeader) {
        UserResponse jobSeeker = userServiceClient.getUserById(
                jobSeekerId, authHeader);

        return messageRepository
                .findByJobSeekerIdAndDirectionOrderBySentAtDesc(
                        jobSeekerId, MessageDirection.EMPLOYER_TO_JOBSEEKER)
                .stream()
                .map(msg -> {
                    UserResponse employer = userServiceClient.getUserById(
                            msg.getEmployerId(), authHeader);
                    JobResponse job = msg.getJobId() != null
                            ? jobServiceClient.getJobById(msg.getJobId(), authHeader)
                            : null;
                    return mapToResponse(msg, jobSeeker, employer, job);
                })
                .collect(Collectors.toList());
    }

    // ── JOB SEEKER: VIEW SENT MESSAGES ───────────────────────────────────────
    public List<MessageResponse> getJobSeekerSentMessages(Long jobSeekerId,
                                                          String authHeader) {
        UserResponse jobSeeker = userServiceClient.getUserById(
                jobSeekerId, authHeader);

        return messageRepository
                .findByJobSeekerIdAndDirection(
                        jobSeekerId, MessageDirection.JOBSEEKER_TO_EMPLOYER)
                .stream()
                .map(msg -> {
                    UserResponse employer = userServiceClient.getUserById(
                            msg.getEmployerId(), authHeader);
                    JobResponse job = msg.getJobId() != null
                            ? jobServiceClient.getJobById(msg.getJobId(), authHeader)
                            : null;
                    return mapToResponse(msg, jobSeeker, employer, job);
                })
                .collect(Collectors.toList());
    }

    // ── EMPLOYER: VIEW INBOX (messages received from job seekers) ────────────
    public List<MessageResponse> getEmployerInbox(Long employerId,
                                                  String authHeader) {
        UserResponse employer = userServiceClient.getUserById(
                employerId, authHeader);

        return messageRepository
                .findByEmployerIdAndDirectionOrderBySentAtDesc(
                        employerId, MessageDirection.JOBSEEKER_TO_EMPLOYER)
                .stream()
                .map(msg -> {
                    UserResponse jobSeeker = userServiceClient.getUserById(
                            msg.getJobSeekerId(), authHeader);
                    JobResponse job = msg.getJobId() != null
                            ? jobServiceClient.getJobById(msg.getJobId(), authHeader)
                            : null;
                    return mapToResponse(msg, jobSeeker, employer, job);
                })
                .collect(Collectors.toList());
    }

    // ── EMPLOYER: VIEW SENT MESSAGES ─────────────────────────────────────────
    public List<MessageResponse> getEmployerSentMessages(Long employerId,
                                                         String authHeader) {
        UserResponse employer = userServiceClient.getUserById(
                employerId, authHeader);

        return messageRepository
                .findByEmployerIdAndDirection(
                        employerId, MessageDirection.EMPLOYER_TO_JOBSEEKER)
                .stream()
                .map(msg -> {
                    UserResponse jobSeeker = userServiceClient.getUserById(
                            msg.getJobSeekerId(), authHeader);
                    JobResponse job = msg.getJobId() != null
                            ? jobServiceClient.getJobById(msg.getJobId(), authHeader)
                            : null;
                    return mapToResponse(msg, jobSeeker, employer, job);
                })
                .collect(Collectors.toList());
    }

    // ── EMPLOYER: VIEW MESSAGES BY JOB ID ────────────────────────────────────
    public List<MessageResponse> getMessagesByJobId(Long jobId,
                                                    String authHeader) {
        JobResponse job = jobServiceClient.getJobById(jobId, authHeader);

        return messageRepository.findByJobIdOrderBySentAtAsc(jobId)
                .stream()
                .map(msg -> {
                    UserResponse jobSeeker = userServiceClient.getUserById(
                            msg.getJobSeekerId(), authHeader);
                    UserResponse employer = userServiceClient.getUserById(
                            msg.getEmployerId(), authHeader);
                    return mapToResponse(msg, jobSeeker, employer, job);
                })
                .collect(Collectors.toList());
    }

    // ── GET SINGLE MESSAGE BY ID ──────────────────────────────────────────────
    public MessageResponse getMessageById(Long messageId, String authHeader) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new MessageNotFoundException(
                        "Message not found with ID: " + messageId));

        UserResponse jobSeeker = userServiceClient.getUserById(
                message.getJobSeekerId(), authHeader);
        UserResponse employer = userServiceClient.getUserById(
                message.getEmployerId(), authHeader);
        JobResponse job = message.getJobId() != null
                ? jobServiceClient.getJobById(message.getJobId(), authHeader)
                : null;

        return mapToResponse(message, jobSeeker, employer, job);
    }

    // ── MARK THREAD AS READ ───────────────────────────────────────────────────
    public Map<String, Object> markThreadAsRead(Long jobSeekerId,
                                                Long employerId) {
        log.info("Mark thread as read — jobSeekerId: {}, employerId: {}",
                jobSeekerId, employerId);
        int updated = messageRepository.markThreadAsRead(
                jobSeekerId, employerId);
        log.info("Marked {} messages as read", updated);
        return Map.of(
                "message", "Thread marked as read",
                "messagesUpdated", updated
        );
    }

    // ── GET UNREAD COUNT FOR JOB SEEKER ──────────────────────────────────────
    public Map<String, Long> getUnreadCountForJobSeeker(Long jobSeekerId) {
        long count = messageRepository
                .countByJobSeekerIdAndIsReadFalseAndDirection(
                        jobSeekerId, MessageDirection.EMPLOYER_TO_JOBSEEKER);
        return Map.of("unreadCount", count);
    }

    // ── GET UNREAD COUNT FOR EMPLOYER ─────────────────────────────────────────
    public Map<String, Long> getUnreadCountForEmployer(Long employerId) {
        long count = messageRepository
                .countByEmployerIdAndIsReadFalseAndDirection(
                        employerId, MessageDirection.JOBSEEKER_TO_EMPLOYER);
        return Map.of("unreadCount", count);
    }

    // ── DELETE MESSAGE ────────────────────────────────────────────────────────
    public Map<String, String> deleteMessage(Long messageId, Long userId) {
        log.info("Delete message — messageId: {}, requestedBy userId: {}",
                messageId, userId);
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new MessageNotFoundException(
                        "Message not found with ID: " + messageId));

        // Only sender can delete their own message
        boolean isSender = switch (message.getDirection()) {
            case JOBSEEKER_TO_EMPLOYER ->
                    message.getJobSeekerId().equals(userId);
            case EMPLOYER_TO_JOBSEEKER ->
                    message.getEmployerId().equals(userId);
        };

        if (!isSender) {
            throw new SecurityException(
                    "You are not authorized to delete this message");
        }

        messageRepository.delete(message);
        log.info("Message deleted — messageId: {}", messageId);
        return Map.of("message", "Message deleted successfully");
    }

    // ── MAPPER ────────────────────────────────────────────────────────────────
    private MessageResponse mapToResponse(Message msg,
                                          UserResponse jobSeeker,
                                          UserResponse employer,
                                          JobResponse job) {
        boolean sentByJobSeeker =
                msg.getDirection() == MessageDirection.JOBSEEKER_TO_EMPLOYER;

        Long senderId = sentByJobSeeker
                ? msg.getJobSeekerId() : msg.getEmployerId();
        String senderName = sentByJobSeeker
                ? (jobSeeker != null ? jobSeeker.getName() : "N/A")
                : (employer != null ? employer.getOrgName() : "N/A");

        Long receiverId = sentByJobSeeker
                ? msg.getEmployerId() : msg.getJobSeekerId();
        String receiverName = sentByJobSeeker
                ? (employer != null ? employer.getOrgName() : "N/A")
                : (jobSeeker != null ? jobSeeker.getName() : "N/A");

        return MessageResponse.builder()
                .messageId(msg.getMessageId())
                .senderId(senderId)
                .senderName(senderName)
                .receiverId(receiverId)
                .receiverName(receiverName)
                .jobId(msg.getJobId())
                .jobTitle(job != null ? job.getJobTitle() : null)
                .description(msg.getDescription())
                .direction(msg.getDirection())
                .isRead(msg.isRead())
                .sentAt(msg.getSentAt())
                .build();
    }
}

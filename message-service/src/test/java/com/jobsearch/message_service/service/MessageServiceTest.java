package com.jobsearch.message_service.service;

import com.jobsearch.message_service.dto.*;
import com.jobsearch.message_service.entity.Message;
import com.jobsearch.message_service.enums.MessageDirection;
import com.jobsearch.message_service.exception.MessageNotFoundException;

import com.jobsearch.message_service.feignClient.JobServiceClient;
import com.jobsearch.message_service.feignClient.UserServiceClient;
import com.jobsearch.message_service.repository.MessageRepository;
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
class MessageServiceTest {

    @Mock private MessageRepository messageRepository;
    @Mock private UserServiceClient userServiceClient;
    @Mock private JobServiceClient jobServiceClient;

    @InjectMocks private MessageService messageService;

    private static final String AUTH = "Bearer test.token";
    private MessageRequest seekerRequest;
    private MessageRequest employerRequest;
    private Message seekerMessage;
    private Message employerMessage;
    private UserResponse jobSeekerResponse;
    private UserResponse employerResponse;
    private JobResponse jobResponse;

    @BeforeEach
    void setUp() {
        seekerRequest = new MessageRequest();
        seekerRequest.setEmployerId(1L);
        seekerRequest.setJobId(1L);
        seekerRequest.setDescription("Hi, I am interested in the role");

        employerRequest = new MessageRequest();
        employerRequest.setJobSeekerId(2L);
        employerRequest.setJobId(1L);
        employerRequest.setDescription("Please attend interview");

        seekerMessage = Message.builder()
                .messageId(1L)
                .jobSeekerId(2L)
                .employerId(1L)
                .jobId(1L)
                .description("Hi, I am interested in the role")
                .direction(MessageDirection.JOBSEEKER_TO_EMPLOYER)
                .isRead(false)
                .sentAt(LocalDateTime.now())
                .build();

        employerMessage = Message.builder()
                .messageId(2L)
                .jobSeekerId(2L)
                .employerId(1L)
                .jobId(1L)
                .description("Please attend interview")
                .direction(MessageDirection.EMPLOYER_TO_JOBSEEKER)
                .isRead(false)
                .sentAt(LocalDateTime.now())
                .build();

        jobSeekerResponse = new UserResponse();
        jobSeekerResponse.setId(2L);
        jobSeekerResponse.setName("Ravi Kumar");
        jobSeekerResponse.setUserType("JOB_SEEKER");

        employerResponse = new UserResponse();
        employerResponse.setId(1L);
        employerResponse.setOrgName("Tech Corp");
        employerResponse.setUserType("EMPLOYER");

        jobResponse = new JobResponse();
        jobResponse.setJobId(1L);
        jobResponse.setJobTitle("Java Backend Developer");
    }

    // ── Send Message Tests ────────────────────────────────────────────────────

    @Test
    void jobSeekerSendsMessage_Success() {
        when(userServiceClient.getUserById(2L, AUTH))
                .thenReturn(jobSeekerResponse);
        when(userServiceClient.getUserById(1L, AUTH))
                .thenReturn(employerResponse);
        when(jobServiceClient.getJobById(1L, AUTH))
                .thenReturn(jobResponse);
        when(messageRepository.save(any(Message.class)))
                .thenReturn(seekerMessage);

        MessageResponse response =
                messageService.jobSeekerSendsMessage(seekerRequest, 2L, AUTH);

        assertNotNull(response);
        assertEquals(1L, response.getMessageId());
        assertEquals(MessageDirection.JOBSEEKER_TO_EMPLOYER,
                response.getDirection());
        assertEquals("Ravi Kumar", response.getSenderName());
        assertEquals("Tech Corp", response.getReceiverName());
        verify(messageRepository).save(any(Message.class));
    }

    @Test
    void jobSeekerSendsMessage_MissingEmployerId_ThrowsException() {
        seekerRequest.setEmployerId(null);

        assertThrows(IllegalArgumentException.class,
                () -> messageService.jobSeekerSendsMessage(
                        seekerRequest, 2L, AUTH));
        verify(messageRepository, never()).save(any());
    }

    @Test
    void jobSeekerSendsMessage_SenderNotJobSeeker_ThrowsException() {
        when(userServiceClient.getUserById(1L, AUTH))
                .thenReturn(employerResponse); // employer trying to use seeker endpoint

        assertThrows(IllegalArgumentException.class,
                () -> messageService.jobSeekerSendsMessage(
                        seekerRequest, 1L, AUTH));
    }

    @Test
    void employerSendsMessage_Success() {
        when(userServiceClient.getUserById(1L, AUTH))
                .thenReturn(employerResponse);
        when(userServiceClient.getUserById(2L, AUTH))
                .thenReturn(jobSeekerResponse);
        when(jobServiceClient.getJobById(1L, AUTH))
                .thenReturn(jobResponse);
        when(messageRepository.save(any(Message.class)))
                .thenReturn(employerMessage);

        MessageResponse response =
                messageService.employerSendsMessage(employerRequest, 1L, AUTH);

        assertNotNull(response);
        assertEquals(2L, response.getMessageId());
        assertEquals(MessageDirection.EMPLOYER_TO_JOBSEEKER,
                response.getDirection());
        assertEquals("Tech Corp", response.getSenderName());
        assertEquals("Ravi Kumar", response.getReceiverName());
    }

    @Test
    void employerSendsMessage_MissingJobSeekerId_ThrowsException() {
        employerRequest.setJobSeekerId(null);

        assertThrows(IllegalArgumentException.class,
                () -> messageService.employerSendsMessage(
                        employerRequest, 1L, AUTH));
    }

    // ── Get Thread Tests ──────────────────────────────────────────────────────

    @Test
    void getConversationThread_ReturnsMessages() {
        when(userServiceClient.getUserById(2L, AUTH))
                .thenReturn(jobSeekerResponse);
        when(userServiceClient.getUserById(1L, AUTH))
                .thenReturn(employerResponse);
        when(messageRepository
                .findByJobSeekerIdAndEmployerIdOrderBySentAtAsc(2L, 1L))
                .thenReturn(List.of(seekerMessage, employerMessage));

        List<MessageResponse> thread =
                messageService.getConversationThread(2L, 1L, AUTH);

        assertEquals(2, thread.size());
        assertEquals(MessageDirection.JOBSEEKER_TO_EMPLOYER,
                thread.get(0).getDirection());
        assertEquals(MessageDirection.EMPLOYER_TO_JOBSEEKER,
                thread.get(1).getDirection());
    }

    @Test
    void getConversationThread_NoMessages_ReturnsEmptyList() {
        when(userServiceClient.getUserById(2L, AUTH))
                .thenReturn(jobSeekerResponse);
        when(userServiceClient.getUserById(1L, AUTH))
                .thenReturn(employerResponse);
        when(messageRepository
                .findByJobSeekerIdAndEmployerIdOrderBySentAtAsc(2L, 1L))
                .thenReturn(List.of());

        List<MessageResponse> thread =
                messageService.getConversationThread(2L, 1L, AUTH);

        assertTrue(thread.isEmpty());
    }

    // ── Mark Read Tests ───────────────────────────────────────────────────────

    @Test
    void markThreadAsRead_ReturnsCount() {
        when(messageRepository.markThreadAsRead(2L, 1L)).thenReturn(3);

        Map<String, Object> result =
                messageService.markThreadAsRead(2L, 1L);

        assertEquals("Thread marked as read", result.get("message"));
        assertEquals(3, result.get("messagesUpdated"));
    }

    // ── Unread Count Tests ────────────────────────────────────────────────────

    @Test
    void getUnreadCountForJobSeeker_ReturnsCount() {
        when(messageRepository
                .countByJobSeekerIdAndIsReadFalseAndDirection(
                        2L, MessageDirection.EMPLOYER_TO_JOBSEEKER))
                .thenReturn(2L);

        Map<String, Long> result =
                messageService.getUnreadCountForJobSeeker(2L);

        assertEquals(2L, result.get("unreadCount"));
    }

    @Test
    void getUnreadCountForEmployer_ReturnsCount() {
        when(messageRepository
                .countByEmployerIdAndIsReadFalseAndDirection(
                        1L, MessageDirection.JOBSEEKER_TO_EMPLOYER))
                .thenReturn(5L);

        Map<String, Long> result =
                messageService.getUnreadCountForEmployer(1L);

        assertEquals(5L, result.get("unreadCount"));
    }

    // ── Delete Tests ──────────────────────────────────────────────────────────

    @Test
    void deleteMessage_BySender_JobSeeker_Success() {
        when(messageRepository.findById(1L))
                .thenReturn(Optional.of(seekerMessage));

        Map<String, String> result =
                messageService.deleteMessage(1L, 2L);

        assertEquals("Message deleted successfully", result.get("message"));
        verify(messageRepository).delete(seekerMessage);
    }

    @Test
    void deleteMessage_NotSender_ThrowsSecurityException() {
        when(messageRepository.findById(1L))
                .thenReturn(Optional.of(seekerMessage));

        // seekerMessage was sent by jobSeekerId=2, trying to delete as employerId=1
        assertThrows(SecurityException.class,
                () -> messageService.deleteMessage(1L, 1L));
        verify(messageRepository, never()).delete(any());
    }

    @Test
    void deleteMessage_NotFound_ThrowsException() {
        when(messageRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(MessageNotFoundException.class,
                () -> messageService.deleteMessage(99L, 2L));
    }
}
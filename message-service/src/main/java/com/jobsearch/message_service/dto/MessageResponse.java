package com.jobsearch.message_service.dto;

import com.jobsearch.message_service.enums.MessageDirection;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MessageResponse {

    private Long messageId;

    // Sender info
    private Long senderId;
    private String senderName;     // orgName or name depending on direction

    // Receiver info
    private Long receiverId;
    private String receiverName;

    // Job context
    private Long jobId;
    private String jobTitle;       // fetched from job-service via Feign

    // Message content
    private String description;
    private MessageDirection direction;
    private boolean isRead;
    private LocalDateTime sentAt;
}

package com.jobsearch.message_service.entity;

import com.jobsearch.message_service.enums.MessageDirection;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long messageId;

    // Both actors involved — logical FKs
    @Column(nullable = false)
    private Long jobSeekerId;

    @Column(nullable = false)
    private Long employerId;

    // The job this message is about (optional but useful)
    private Long jobId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    // Tells us who sent this message
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageDirection direction;

    // Track if receiver has read the message
    @Column(nullable = false)
    private boolean isRead = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime sentAt;

    @PrePersist
    public void prePersist() {
        this.sentAt = LocalDateTime.now();
        this.isRead = false;
    }
}

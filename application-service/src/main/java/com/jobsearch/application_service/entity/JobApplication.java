package com.jobsearch.application_service.entity;

import com.jobsearch.application_service.enums.ApplicationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "job_applications",
        // Prevent a job seeker from applying to the same job twice
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"job_id", "jobseeker_id"},
                name = "uq_job_jobseeker"
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long applicationId;

    // Logical FKs — no physical foreign keys across microservices
    @Column(nullable = false)
    private Long jobId;

    @Column(nullable = false)
    private Long jobSeekerId;

    @Column(nullable = false)
    private Long employerId;      // stored for quick employer queries

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationStatus status;

    private String coverLetter;   // optional message from job seeker

    @Column(nullable = false, updatable = false)
    private LocalDateTime appliedAt;

    @PrePersist
    public void prePersist() {
        this.appliedAt = LocalDateTime.now();
        this.status = ApplicationStatus.APPLIED;
    }

}

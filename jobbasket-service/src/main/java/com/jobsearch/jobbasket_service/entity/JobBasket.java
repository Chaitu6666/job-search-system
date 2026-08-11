package com.jobsearch.jobbasket_service.entity;

import com.jobsearch.jobbasket_service.enums.BasketStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "job_basket",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"job_id", "jobseeker_id"},
                name = "uq_basket_job_jobseeker"
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobBasket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long basketId;

    // Logical FK — no physical FK across microservices
    @Column(nullable = false)
    private Long jobSeekerId;

    @Column(nullable = false)
    private Long jobId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BasketStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime addedAt;

    @PrePersist
    public void prePersist() {
        this.addedAt = LocalDateTime.now();
        this.status = BasketStatus.SAVED;
    }
}

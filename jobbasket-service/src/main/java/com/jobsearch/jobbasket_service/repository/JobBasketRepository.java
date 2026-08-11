package com.jobsearch.jobbasket_service.repository;

import com.jobsearch.jobbasket_service.entity.JobBasket;
import com.jobsearch.jobbasket_service.enums.BasketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobBasketRepository extends JpaRepository<JobBasket, Long> {

    // All basket items for a job seeker
    List<JobBasket> findByJobSeekerId(Long jobSeekerId);

    // All basket items with a specific status for a job seeker
    List<JobBasket> findByJobSeekerIdAndStatus(
            Long jobSeekerId, BasketStatus status);

    // Check if job already in basket
    boolean existsByJobIdAndJobSeekerId(Long jobId, Long jobSeekerId);

    // Find a specific basket item by basketId and jobSeekerId
    // (ownership check — job seeker can only touch their own basket)
    Optional<JobBasket> findByBasketIdAndJobSeekerId(
            Long basketId, Long jobSeekerId);

    // Find basket item by jobId and jobSeekerId
    Optional<JobBasket> findByJobIdAndJobSeekerId(
            Long jobId, Long jobSeekerId);

    // Count total saved jobs for a job seeker
    long countByJobSeekerIdAndStatus(
            Long jobSeekerId, BasketStatus status);

    // Delete all basket items for a job seeker (clear basket)
    void deleteAllByJobSeekerId(Long jobSeekerId);
}

package com.jobsearch.application_service.repository;

import com.jobsearch.application_service.entity.JobApplication;
import com.jobsearch.application_service.enums.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<JobApplication, Long> {
    List<JobApplication> findByJobSeekerId(Long jobSeekerId);
    List<JobApplication> findByJobId(Long jobId);
    List<JobApplication> findByEmployerId(Long employerId);
    boolean existsByJobIdAndJobSeekerId(Long jobId, Long jobSeekerId);
    Optional<JobApplication> findByApplicationIdAndEmployerId(Long applicationId, Long employerId);
    Optional<JobApplication> findByApplicationIdAndJobSeekerId(Long applicationId, Long jobSeekerId);
    long countByJobId(Long jobId);
}

package com.jobserach.job_service.repository;

import com.jobserach.job_service.entity.Job;
import com.jobserach.job_service.enums.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {
    List<Job> findByEmployerId(Long employerId);
    List<Job> findByLocationContainingIgnoreCaseAndStatus(String location, JobStatus status);
    @Query("SELECT j FROM Job j WHERE j.status = 'ACTIVE' AND (" +
            "LOWER(j.requiredSkills) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(j.designation)    LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(j.companyName)    LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Job> searchByKeyword(@Param("keyword") String keyword);
}

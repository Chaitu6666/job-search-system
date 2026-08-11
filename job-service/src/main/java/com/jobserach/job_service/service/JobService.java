package com.jobserach.job_service.service;

import com.jobserach.job_service.dto.JobRequest;
import com.jobserach.job_service.dto.JobResponse;
import com.jobserach.job_service.dto.UserResponse;
import com.jobserach.job_service.entity.Job;
import com.jobserach.job_service.enums.JobStatus;
import com.jobserach.job_service.exception.JobNotFoundException;
import com.jobserach.job_service.feignClient.UserServiceClient;
import com.jobserach.job_service.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;
    private final UserServiceClient userServiceClient;

    public JobResponse postJob(JobRequest request, Long employerId, String authHeader) {
        log.info("Post job request — employerId: {}, title: {}",
                employerId, request.getJobTitle());
        UserResponse employer = userServiceClient.getUserById(employerId,authHeader);

        if(!"EMPLOYER".equals(employer.getUserType())) {
            log.warn("Post job denied — userId: {} is not an EMPLOYER",
                    employerId);
            throw new IllegalArgumentException("Only employers can post jobs");
        }

        Job job = Job.builder()
                .employerId(employerId)
                .jobTitle(request.getJobTitle())
                .location(request.getLocation())
                .description(request.getDescription())
                .experience(request.getExperience())
                .salary(request.getSalary())
                .noticePeriod(request.getNoticePeriod())
                .contactEmail(request.getContactEmail())
                .status(request.getStatus() != null ? request.getStatus() : JobStatus.ACTIVE)
                .requiredSkills(request.getRequiredSkills())
                .companyName(request.getCompanyName())
                .designation(request.getDesignation())
                .build();

        Job saved = jobRepository.save(job);
        log.info("Job posted successfully — jobId: {}, employerId: {}",
                saved.getJobId(), employerId);

        return mapToResponse(saved, employer);
    }

    public JobResponse editJob(Long jobId, JobRequest request, Long employerId, String authHeader) {
        log.info("Edit job request — jobId: {}, employerId: {}",
                jobId, employerId);
        Job job = jobRepository.findById(jobId).orElseThrow(()-> new JobNotFoundException("Job not found with ID: " + jobId));

        if(!job.getEmployerId().equals(employerId)) {
            log.warn("Edit job denied — employerId: {} does not own jobId: {}",
                    employerId, jobId);
            throw new SecurityException("You are not authorized to edit this job");
        }

        job.setJobTitle(request.getJobTitle());
        job.setLocation(request.getLocation());
        job.setDescription(request.getDescription());
        job.setExperience(request.getExperience());
        job.setSalary(request.getSalary());
        job.setNoticePeriod(request.getNoticePeriod());
        job.setContactEmail(request.getContactEmail());
        job.setRequiredSkills(request.getRequiredSkills());
        job.setCompanyName(request.getCompanyName());
        job.setDesignation(request.getDesignation());
        if (request.getStatus() != null) {
            job.setStatus(request.getStatus());
        }

        Job updated = jobRepository.save(job);
        log.info("Job updated successfully — jobId: {}", jobId);
        UserResponse employer = userServiceClient.getUserById(employerId, authHeader);
        return mapToResponse(updated,employer);
    }

    public Map<String, String> deleteJob(Long jobId, Long employerId) {
        log.info("Delete job request — jobId: {}, employerId: {}",
                jobId, employerId);
        Job job = jobRepository.findById(jobId).orElseThrow(() -> new JobNotFoundException("Job not found with ID: " + jobId));

        if(!job.getEmployerId().equals(employerId)) {
            log.warn("Delete job denied — employerId: {} does not own jobId: {}",
                    employerId, jobId);
            throw new SecurityException("You are not authorized to delete this job");
        }
        jobRepository.delete(job);
        log.info("Job deleted successfully — jobId: {}", jobId);
        return Map.of("message: ", "Job deleted successfully");
    }

    public JobResponse getJobById(Long jobId, String authHeader) {
        log.debug("Get job by id — jobId: {}", jobId);
        Job job = jobRepository.findById(jobId).orElseThrow(() -> new JobNotFoundException("Job not found with ID: " + jobId));
        UserResponse employer = userServiceClient.getUserById(job.getEmployerId(), authHeader);
        return mapToResponse(job, employer);
    }

    public List<JobResponse> getJobsByEmployer(Long employerId, String authHeader) {
        log.info("Get jobs by employer — employerId: {}", employerId);
        UserResponse employer = userServiceClient.getUserById(employerId, authHeader);
        return jobRepository.findByEmployerId(employerId).stream()
                .map(job -> mapToResponse(job,employer))
                .toList();
    }


    public List<JobResponse> searchByKeyword(String keyword, String authHeader) {
        log.info("Search jobs by keyword: {}", keyword);
        return jobRepository.searchByKeyword(keyword).stream()
                .map(job -> {
                    UserResponse employer = userServiceClient.getUserById(job.getEmployerId(), authHeader);
                    return mapToResponse(job, employer);
                })
                .toList();
    }

    public List<JobResponse> searchByLocation(String location, String authHeader) {
        log.info("Search jobs by location: {}", location);
        return jobRepository.findByLocationContainingIgnoreCaseAndStatus(location, JobStatus.ACTIVE)
                .stream()
                .map(job -> {
                    UserResponse employer = userServiceClient.getUserById(job.getEmployerId(), authHeader);
                    return mapToResponse(job, employer);
                })
                .toList();
    }

    public List<UserResponse> searchJobSeekersBySkill(String skill, String authHeader) {
        log.info("Search job seekers by skill: {}", skill);
        return userServiceClient.searchJobSeekersBySkill(skill,authHeader);
    }

    public List<UserResponse> searchJobSeekersByJobId(Long jobId, String authHeader) {
        log.info("Search job seekers by jobId: {}", jobId);
        Job job = jobRepository.findById(jobId).orElseThrow(() -> new JobNotFoundException("Job not found with ID: " + jobId));
        if(job.getRequiredSkills() == null || job.getRequiredSkills().isBlank()) {
            return List.of();
        }
        String primarySkill = job.getRequiredSkills().split(",")[0].trim();
        return userServiceClient.searchJobSeekersBySkill(primarySkill, authHeader);
    }

    private JobResponse mapToResponse(Job job, UserResponse employer) {
        return JobResponse.builder()
                .jobId(job.getJobId())
                .employerId(job.getEmployerId())
                .employerName(employer != null ? employer.getOrgName() : "N/A")
                .jobTitle(job.getJobTitle())
                .location(job.getLocation())
                .description(job.getDescription())
                .experience(job.getExperience())
                .salary(job.getSalary())
                .noticePeriod(job.getNoticePeriod())
                .contactEmail(job.getContactEmail())
                .status(job.getStatus())
                .requiredSkills(job.getRequiredSkills())
                .companyName(job.getCompanyName())
                .designation(job.getDesignation())
                .createdAt(job.getCreatedAt())
                .build();
    }
}

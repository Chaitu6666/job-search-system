package com.jobsearch.jobbasket_service.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BasketRequest {

    @NotNull(message = "Job ID is required")
    private Long jobId;

    // jobSeekerId always comes from X-User-Id header — never from body
}

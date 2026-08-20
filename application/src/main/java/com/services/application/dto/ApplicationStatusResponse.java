package com.services.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationStatusResponse {
    private Long applicationId;
    private String trackingId;
    private String status;
    private String jobTitle;
    private String appliedDate;
    private String updatedAt;
}
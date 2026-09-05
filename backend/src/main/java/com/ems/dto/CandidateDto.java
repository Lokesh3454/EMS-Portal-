package com.ems.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CandidateDto {
    private Long id;
    private Long jobPostingId;
    private String jobTitle;
    private String departmentName;
    private String fullName;
    private String email;
    private String phone;
    private String currentCompany;
    private Double experienceYears;
    private String stage;
    private LocalDate appliedDate;
    private String interviewFeedback;
    private Double rating;
    private Boolean convertedToEmployee;
}

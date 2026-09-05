package com.ems.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobPostingDto {
    private Long id;
    private String title;
    private Long departmentId;
    private String departmentName;
    private String location;
    private String employmentType;
    private String experienceRequired;
    private String salaryRange;
    private String description;
    private Integer vacancies;
    private String status;
    private LocalDate postedDate;
    private Long applicantCount;
}

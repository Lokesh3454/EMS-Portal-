package com.ems.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeGoalDto {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private String title;
    private String description;
    private String category;
    private LocalDate targetDate;
    private Integer progressPercentage;
    private String status;
}

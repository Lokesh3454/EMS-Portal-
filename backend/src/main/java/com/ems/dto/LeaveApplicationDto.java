package com.ems.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeaveApplicationDto {
    private String leaveType; // CASUAL, SICK, EARNED, WFH
    private LocalDate startDate;
    private LocalDate endDate;
    private String reason;
}

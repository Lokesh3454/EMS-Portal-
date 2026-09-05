package com.ems.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceSummaryDto {
    private long totalEmployees;
    private long presentToday;
    private long lateToday;
    private long onLeaveToday;
    private long absentToday;
    private double attendanceRate;
}

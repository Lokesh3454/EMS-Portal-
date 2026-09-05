package com.ems.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveBalanceDto {
    private Integer year;
    private Integer casualLeavesRemaining;
    private Integer sickLeavesRemaining;
    private Integer earnedLeavesRemaining;
    private Integer totalRemaining;
}

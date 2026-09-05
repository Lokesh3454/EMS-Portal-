package com.ems.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PerformanceReviewDto {
    private Long id;
    private Long cycleId;
    private String cycleName;
    private String period;

    private Long employeeId;
    private String empId;
    private String employeeName;
    private String departmentName;
    private String designation;
    private String email;

    private Long managerId;
    private String managerName;

    // Self review
    private Double selfScoreTechnical;
    private Double selfScoreDelivery;
    private Double selfScoreCollaboration;
    private Double selfScoreLeadership;
    private Double selfAverageScore;
    private String selfAchievements;
    private String selfImprovements;

    // Manager review
    private Double mgrScoreTechnical;
    private Double mgrScoreDelivery;
    private Double mgrScoreCollaboration;
    private Double mgrScoreLeadership;
    private Double mgrAverageScore;
    private Double finalRating;
    private String managerFeedback;
    private Double recommendedIncrement;
    private Boolean recommendedPromotion;
    private String recommendedDesignation;

    private String status;
    private LocalDateTime updatedAt;
}

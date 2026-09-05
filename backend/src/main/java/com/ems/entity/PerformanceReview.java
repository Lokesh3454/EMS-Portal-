package com.ems.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "performance_reviews")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PerformanceReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "cycle_id", nullable = false)
    private AppraisalCycle cycle;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "manager_id")
    private Employee manager;

    // Self Evaluation Scores (1.0 to 5.0)
    @Column(name = "self_score_technical")
    private Double selfScoreTechnical = 3.0;

    @Column(name = "self_score_delivery")
    private Double selfScoreDelivery = 3.0;

    @Column(name = "self_score_collaboration")
    private Double selfScoreCollaboration = 3.0;

    @Column(name = "self_score_leadership")
    private Double selfScoreLeadership = 3.0;

    @Column(name = "self_average_score")
    private Double selfAverageScore = 3.0;

    @Column(name = "self_achievements", columnDefinition = "TEXT")
    private String selfAchievements;

    @Column(name = "self_improvements", columnDefinition = "TEXT")
    private String selfImprovements;

    // Manager Evaluation Scores (1.0 to 5.0)
    @Column(name = "mgr_score_technical")
    private Double mgrScoreTechnical;

    @Column(name = "mgr_score_delivery")
    private Double mgrScoreDelivery;

    @Column(name = "mgr_score_collaboration")
    private Double mgrScoreCollaboration;

    @Column(name = "mgr_score_leadership")
    private Double mgrScoreLeadership;

    @Column(name = "mgr_average_score")
    private Double mgrAverageScore;

    @Column(name = "final_rating")
    private Double finalRating;

    @Column(name = "manager_feedback", columnDefinition = "TEXT")
    private String managerFeedback;

    @Column(name = "recommended_increment")
    private Double recommendedIncrement = 0.0; // Percentage, e.g. 10.0%

    @Column(name = "recommended_promotion")
    private Boolean recommendedPromotion = false;

    @Column(name = "recommended_designation")
    private String recommendedDesignation;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ReviewStatus status = ReviewStatus.DRAFT;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum ReviewStatus {
        DRAFT, SELF_SUBMITTED, MANAGER_REVIEWED, COMPLETED
    }
}

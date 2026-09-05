package com.ems.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "candidates")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Candidate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "job_posting_id", nullable = false)
    private JobPosting jobPosting;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "phone")
    private String phone;

    @Column(name = "current_company")
    private String currentCompany;

    @Column(name = "experience_years")
    private Double experienceYears = 0.0;

    @Enumerated(EnumType.STRING)
    @Column(name = "stage")
    private CandidateStage stage = CandidateStage.APPLIED;

    @Column(name = "applied_date")
    private LocalDate appliedDate;

    @Column(name = "interview_feedback", columnDefinition = "TEXT")
    private String interviewFeedback;

    @Column(name = "rating")
    private Double rating = 4.0; // 1 to 5

    @Column(name = "converted_to_employee")
    private Boolean convertedToEmployee = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum CandidateStage {
        APPLIED,
        SCREENING,
        INTERVIEW,
        OFFERED,
        HIRED,
        REJECTED
    }
}

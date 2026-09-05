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
@Table(name = "job_postings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobPosting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "department_id")
    private Department department;

    @Column(name = "location")
    private String location = "Bangalore, India (Hybrid)";

    @Enumerated(EnumType.STRING)
    @Column(name = "employment_type")
    private Employee.EmploymentType employmentType = Employee.EmploymentType.FULL_TIME;

    @Column(name = "experience_required")
    private String experienceRequired = "3-5 Years";

    @Column(name = "salary_range")
    private String salaryRange = "₹12 - ₹18 LPA";

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "vacancies")
    private Integer vacancies = 1;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private JobStatus status = JobStatus.ACTIVE;

    @Column(name = "posted_date")
    private LocalDate postedDate;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum JobStatus {
        ACTIVE, CLOSED, DRAFT
    }
}

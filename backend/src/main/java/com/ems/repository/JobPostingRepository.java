package com.ems.repository;

import com.ems.entity.JobPosting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobPostingRepository extends JpaRepository<JobPosting, Long> {
    List<JobPosting> findByStatusOrderByPostedDateDesc(JobPosting.JobStatus status);
    List<JobPosting> findByDepartmentId(Long departmentId);
}

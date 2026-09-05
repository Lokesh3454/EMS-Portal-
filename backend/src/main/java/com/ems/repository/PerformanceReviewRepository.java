package com.ems.repository;

import com.ems.entity.PerformanceReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PerformanceReviewRepository extends JpaRepository<PerformanceReview, Long> {
    List<PerformanceReview> findByEmployeeId(Long employeeId);
    Optional<PerformanceReview> findByCycleIdAndEmployeeId(Long cycleId, Long employeeId);
    List<PerformanceReview> findByCycleId(Long cycleId);
    List<PerformanceReview> findByCycleIdAndEmployeeDepartmentId(Long cycleId, Long departmentId);
    List<PerformanceReview> findByManagerId(Long managerId);
    boolean existsByCycleIdAndEmployeeId(Long cycleId, Long employeeId);
}

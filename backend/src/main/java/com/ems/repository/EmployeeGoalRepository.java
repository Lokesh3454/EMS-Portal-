package com.ems.repository;

import com.ems.entity.EmployeeGoal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeGoalRepository extends JpaRepository<EmployeeGoal, Long> {
    List<EmployeeGoal> findByEmployeeIdOrderByCreatedAtDesc(Long employeeId);
    List<EmployeeGoal> findByEmployeeDepartmentId(Long departmentId);
}

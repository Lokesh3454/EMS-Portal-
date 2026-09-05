package com.ems.repository;

import com.ems.entity.Employee;
import com.ems.entity.LeaveRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
    List<LeaveRequest> findByEmployeeOrderByAppliedAtDesc(Employee employee);
    List<LeaveRequest> findByStatusOrderByAppliedAtDesc(LeaveRequest.LeaveStatus status);
    List<LeaveRequest> findAllByOrderByAppliedAtDesc();
    long countByStatus(LeaveRequest.LeaveStatus status);
}

package com.ems.service;

import com.ems.dto.LeaveApplicationDto;
import com.ems.dto.LeaveBalanceDto;
import com.ems.dto.LeaveResponseDto;
import com.ems.dto.LeaveReviewDto;
import com.ems.entity.*;
import com.ems.exception.ResourceNotFoundException;
import com.ems.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LeaveService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;

    @Transactional
    public LeaveResponseDto applyLeave(String email, LeaveApplicationDto dto) {
        Employee employee = getOrCreateEmployeeForEmail(email);

        if (dto.getStartDate() == null || dto.getEndDate() == null) {
            throw new IllegalArgumentException("Start date and end date are required.");
        }
        if (dto.getEndDate().isBefore(dto.getStartDate())) {
            throw new IllegalArgumentException("End date cannot be earlier than start date.");
        }
        if (dto.getReason() == null || dto.getReason().trim().isEmpty()) {
            throw new IllegalArgumentException("Reason is required.");
        }

        int days = (int) ChronoUnit.DAYS.between(dto.getStartDate(), dto.getEndDate()) + 1;

        LeaveRequest.LeaveType type;
        try {
            type = LeaveRequest.LeaveType.valueOf(dto.getLeaveType().toUpperCase());
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid leave type. Supported: CASUAL, SICK, EARNED, WFH");
        }

        // Validate balance if not WFH
        if (type != LeaveRequest.LeaveType.WFH) {
            LeaveBalance balance = getOrCreateBalance(employee, dto.getStartDate().getYear());
            int remaining = switch (type) {
                case CASUAL -> balance.getCasualLeavesRemaining();
                case SICK -> balance.getSickLeavesRemaining();
                case EARNED -> balance.getEarnedLeavesRemaining();
                default -> 0;
            };

            if (remaining < days) {
                throw new IllegalArgumentException("Insufficient leave balance. You requested " + days + " days, but only " + remaining + " days remain.");
            }
        }

        LeaveRequest request = LeaveRequest.builder()
                .employee(employee)
                .leaveType(type)
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .totalDays(days)
                .reason(dto.getReason())
                .status(LeaveRequest.LeaveStatus.PENDING)
                .build();

        LeaveRequest saved = leaveRequestRepository.save(request);
        return toDto(saved);
    }

    public List<LeaveResponseDto> getMyLeaves(String email) {
        Employee employee = getOrCreateEmployeeForEmail(email);
        return leaveRequestRepository.findByEmployeeOrderByAppliedAtDesc(employee)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<LeaveResponseDto> getPendingLeaves() {
        return leaveRequestRepository.findByStatusOrderByAppliedAtDesc(LeaveRequest.LeaveStatus.PENDING)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<LeaveResponseDto> getAllLeaves() {
        return leaveRequestRepository.findAllByOrderByAppliedAtDesc()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public LeaveBalanceDto getLeaveBalance(String email, Integer year) {
        Employee employee = getOrCreateEmployeeForEmail(email);
        int targetYear = year != null ? year : LocalDate.now().getYear();
        LeaveBalance balance = getOrCreateBalance(employee, targetYear);

        int totalRemaining = balance.getCasualLeavesRemaining() 
                + balance.getSickLeavesRemaining() 
                + balance.getEarnedLeavesRemaining();

        return LeaveBalanceDto.builder()
                .year(balance.getYear())
                .casualLeavesRemaining(balance.getCasualLeavesRemaining())
                .sickLeavesRemaining(balance.getSickLeavesRemaining())
                .earnedLeavesRemaining(balance.getEarnedLeavesRemaining())
                .totalRemaining(totalRemaining)
                .build();
    }

    @Transactional
    public LeaveResponseDto reviewLeave(Long leaveId, String reviewerEmail, LeaveReviewDto reviewDto) {
        LeaveRequest request = leaveRequestRepository.findById(leaveId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found with id: " + leaveId));

        if (request.getStatus() != LeaveRequest.LeaveStatus.PENDING) {
            throw new IllegalArgumentException("Leave request has already been " + request.getStatus());
        }

        LeaveRequest.LeaveStatus newStatus;
        try {
            newStatus = LeaveRequest.LeaveStatus.valueOf(reviewDto.getStatus().toUpperCase());
        } catch (Exception e) {
            throw new IllegalArgumentException("Status must be APPROVED or REJECTED");
        }

        request.setStatus(newStatus);
        request.setReviewedBy(reviewerEmail);
        request.setReviewedAt(LocalDateTime.now());
        request.setReviewRemarks(reviewDto.getRemarks());

        // Deduct from balance on approval if not WFH
        if (newStatus == LeaveRequest.LeaveStatus.APPROVED && request.getLeaveType() != LeaveRequest.LeaveType.WFH) {
            LeaveBalance balance = getOrCreateBalance(request.getEmployee(), request.getStartDate().getYear());
            switch (request.getLeaveType()) {
                case CASUAL -> balance.setCasualLeavesRemaining(Math.max(0, balance.getCasualLeavesRemaining() - request.getTotalDays()));
                case SICK -> balance.setSickLeavesRemaining(Math.max(0, balance.getSickLeavesRemaining() - request.getTotalDays()));
                case EARNED -> balance.setEarnedLeavesRemaining(Math.max(0, balance.getEarnedLeavesRemaining() - request.getTotalDays()));
                default -> {}
            }
            leaveBalanceRepository.save(balance);
        }

        LeaveRequest saved = leaveRequestRepository.save(request);
        return toDto(saved);
    }

    public long getPendingCount() {
        return leaveRequestRepository.countByStatus(LeaveRequest.LeaveStatus.PENDING);
    }

    private LeaveBalance getOrCreateBalance(Employee employee, int year) {
        return leaveBalanceRepository.findByEmployeeAndYear(employee, year)
                .orElseGet(() -> {
                    LeaveBalance balance = LeaveBalance.builder()
                            .employee(employee)
                            .year(year)
                            .casualLeavesRemaining(12)
                            .sickLeavesRemaining(10)
                            .earnedLeavesRemaining(15)
                            .build();
                    return leaveBalanceRepository.save(balance);
                });
    }

    private Employee getOrCreateEmployeeForEmail(String email) {
        return employeeRepository.findByEmail(email).orElseGet(() -> {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found for email: " + email));

            Department adminDept = departmentRepository.findByName("Administration")
                    .or(() -> departmentRepository.findAll().stream().findFirst())
                    .orElse(null);

            Employee adminEmp = new Employee();
            adminEmp.setEmpId("ADM001");
            adminEmp.setFirstName("System");
            adminEmp.setLastName("Administrator");
            adminEmp.setEmail(user.getEmail());
            adminEmp.setPhone("+1 555-0100");
            adminEmp.setDesignation("System Administrator");
            adminEmp.setDepartment(adminDept);
            adminEmp.setDateOfJoining(LocalDate.of(2023, 1, 1));
            adminEmp.setStatus(Employee.EmployeeStatus.ACTIVE);
            adminEmp.setAddress("Headquarters, Executive Suite 100");
            adminEmp.setUser(user);
            return employeeRepository.save(adminEmp);
        });
    }

    private LeaveResponseDto toDto(LeaveRequest request) {
        Employee emp = request.getEmployee();
        return LeaveResponseDto.builder()
                .id(request.getId())
                .employeeId(emp.getId())
                .employeeName(emp.getFirstName() + " " + emp.getLastName())
                .employeeCode(emp.getEmpId())
                .departmentName(emp.getDepartment() != null ? emp.getDepartment().getName() : "Unassigned")
                .leaveType(request.getLeaveType().name())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .totalDays(request.getTotalDays())
                .reason(request.getReason())
                .status(request.getStatus().name())
                .appliedAt(request.getAppliedAt())
                .reviewedBy(request.getReviewedBy())
                .reviewedAt(request.getReviewedAt())
                .reviewRemarks(request.getReviewRemarks())
                .build();
    }
}

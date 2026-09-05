package com.ems.controller;

import com.ems.dto.*;
import com.ems.service.LeaveService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/leaves")
@RequiredArgsConstructor
public class LeaveController {

    private final LeaveService leaveService;

    @PostMapping("/apply")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<LeaveResponseDto>> applyLeave(
            @RequestBody LeaveApplicationDto dto,
            Authentication authentication) {
        String email = authentication.getName();
        LeaveResponseDto result = leaveService.applyLeave(email, dto);
        return ResponseEntity.ok(ApiResponse.success("Leave applied successfully", result));
    }

    @GetMapping("/my-requests")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<LeaveResponseDto>>> getMyLeaves(Authentication authentication) {
        String email = authentication.getName();
        List<LeaveResponseDto> list = leaveService.getMyLeaves(email);
        return ResponseEntity.ok(ApiResponse.success("Leave requests fetched", list));
    }

    @GetMapping("/balance")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<LeaveBalanceDto>> getLeaveBalance(
            @RequestParam(required = false) Integer year,
            Authentication authentication) {
        String email = authentication.getName();
        LeaveBalanceDto balance = leaveService.getLeaveBalance(email, year);
        return ResponseEntity.ok(ApiResponse.success("Leave balance fetched", balance));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('ADMIN','HR','MANAGER')")
    public ResponseEntity<ApiResponse<List<LeaveResponseDto>>> getPendingLeaves() {
        List<LeaveResponseDto> list = leaveService.getPendingLeaves();
        return ResponseEntity.ok(ApiResponse.success("Pending leave requests fetched", list));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMIN','HR','MANAGER')")
    public ResponseEntity<ApiResponse<List<LeaveResponseDto>>> getAllLeaves() {
        List<LeaveResponseDto> list = leaveService.getAllLeaves();
        return ResponseEntity.ok(ApiResponse.success("All leave requests fetched", list));
    }

    @PutMapping("/{id}/review")
    @PreAuthorize("hasAnyRole('ADMIN','HR','MANAGER')")
    public ResponseEntity<ApiResponse<LeaveResponseDto>> reviewLeave(
            @PathVariable Long id,
            @RequestBody LeaveReviewDto reviewDto,
            Authentication authentication) {
        String reviewerEmail = authentication.getName();
        LeaveResponseDto result = leaveService.reviewLeave(id, reviewerEmail, reviewDto);
        return ResponseEntity.ok(ApiResponse.success("Leave request " + reviewDto.getStatus().toLowerCase() + " successfully", result));
    }

    @GetMapping("/pending-count")
    @PreAuthorize("hasAnyRole('ADMIN','HR','MANAGER')")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getPendingCount() {
        long count = leaveService.getPendingCount();
        return ResponseEntity.ok(ApiResponse.success("Pending count fetched", Map.of("count", count)));
    }
}

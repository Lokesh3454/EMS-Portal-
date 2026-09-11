package com.ems.controller;

import com.ems.dto.ApiResponse;
import com.ems.dto.EmployeeGoalDto;
import com.ems.dto.PerformanceReviewDto;
import com.ems.entity.AppraisalCycle;
import com.ems.service.PerformanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/performance")
@RequiredArgsConstructor
public class PerformanceController {

    private final PerformanceService performanceService;

    @GetMapping("/cycle/active")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<AppraisalCycle>> getActiveCycle() {
        AppraisalCycle cycle = performanceService.getOrCreateActiveCycle();
        return ResponseEntity.ok(ApiResponse.success("Active appraisal cycle fetched", cycle));
    }

    @GetMapping("/my-review")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PerformanceReviewDto>> getMyReview(Authentication authentication) {
        String email = authentication.getName();
        PerformanceReviewDto review = performanceService.getMyReview(email);
        return ResponseEntity.ok(ApiResponse.success("Employee review fetched", review));
    }

    @PostMapping("/my-review/submit")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PerformanceReviewDto>> submitSelfReview(
            @RequestBody PerformanceReviewDto dto,
            Authentication authentication) {
        String email = authentication.getName();
        PerformanceReviewDto submitted = performanceService.submitSelfReview(email, dto);
        return ResponseEntity.ok(ApiResponse.success("Self-appraisal submitted successfully", submitted));
    }

    @GetMapping("/team-reviews")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
    public ResponseEntity<ApiResponse<List<PerformanceReviewDto>>> getTeamReviews(
            @RequestParam(required = false) Long deptId) {
        List<PerformanceReviewDto> list = performanceService.getTeamReviews(deptId);
        return ResponseEntity.ok(ApiResponse.success("Team reviews fetched", list));
    }

    @PostMapping("/manager-review/{reviewId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
    public ResponseEntity<ApiResponse<PerformanceReviewDto>> submitManagerReview(
            @PathVariable Long reviewId,
            @RequestBody PerformanceReviewDto dto,
            Authentication authentication) {
        String managerEmail = authentication.getName();
        PerformanceReviewDto reviewed = performanceService.submitManagerReview(reviewId, dto, managerEmail);
        return ResponseEntity.ok(ApiResponse.success("Manager appraisal review submitted", reviewed));
    }

    @GetMapping("/my-goals")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<EmployeeGoalDto>>> getMyGoals(Authentication authentication) {
        String email = authentication.getName();
        List<EmployeeGoalDto> list = performanceService.getMyGoals(email);
        return ResponseEntity.ok(ApiResponse.success("Employee OKRs & goals fetched", list));
    }

    @PostMapping("/goals")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<EmployeeGoalDto>> createGoal(
            @RequestBody EmployeeGoalDto dto,
            Authentication authentication) {
        String email = authentication.getName();
        EmployeeGoalDto created = performanceService.createGoal(email, dto);
        return ResponseEntity.ok(ApiResponse.success("Goal created successfully", created));
    }

    @PutMapping("/goals/{goalId}/progress")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<EmployeeGoalDto>> updateGoalProgress(
            @PathVariable Long goalId,
            @RequestBody Map<String, Object> body,
            Authentication authentication) {
        Integer progress = body.containsKey("progress") ? Integer.valueOf(body.get("progress").toString()) : null;
        String status = body.containsKey("status") ? body.get("status").toString() : null;

        boolean isManagerOrAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_HR") || a.getAuthority().equals("ROLE_MANAGER"));

        EmployeeGoalDto updated = performanceService.updateGoalProgress(goalId, progress, status, authentication.getName(), isManagerOrAdmin);
        return ResponseEntity.ok(ApiResponse.success("Goal progress updated", updated));
    }
}

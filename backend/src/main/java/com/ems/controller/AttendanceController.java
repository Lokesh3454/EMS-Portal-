package com.ems.controller;

import com.ems.dto.ApiResponse;
import com.ems.dto.AttendanceDto;
import com.ems.dto.AttendanceSummaryDto;
import com.ems.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping("/clock-in")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<AttendanceDto>> clockIn(
            @RequestBody(required = false) Map<String, String> body,
            Authentication authentication) {
        String email = authentication.getName();
        String notes = body != null ? body.get("notes") : null;
        AttendanceDto dto = attendanceService.clockIn(email, notes);
        return ResponseEntity.ok(ApiResponse.success("Clock-in recorded successfully", dto));
    }

    @PostMapping("/clock-out")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<AttendanceDto>> clockOut(
            @RequestBody(required = false) Map<String, String> body,
            Authentication authentication) {
        String email = authentication.getName();
        String notes = body != null ? body.get("notes") : null;
        AttendanceDto dto = attendanceService.clockOut(email, notes);
        return ResponseEntity.ok(ApiResponse.success("Clock-out recorded successfully", dto));
    }

    @GetMapping("/today")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<AttendanceDto>> getTodayStatus(Authentication authentication) {
        String email = authentication.getName();
        AttendanceDto dto = attendanceService.getTodayStatus(email);
        return ResponseEntity.ok(ApiResponse.success("Today status fetched", dto));
    }

    @GetMapping("/my-monthly")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<AttendanceDto>>> getMyMonthlyAttendance(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            Authentication authentication) {
        String email = authentication.getName();
        int y = year != null ? year : LocalDate.now().getYear();
        int m = month != null ? month : LocalDate.now().getMonthValue();
        List<AttendanceDto> list = attendanceService.getMyMonthlyAttendance(email, y, m);
        return ResponseEntity.ok(ApiResponse.success("Monthly attendance fetched", list));
    }

    @GetMapping("/company-daily")
    @PreAuthorize("hasAnyRole('ADMIN','HR','MANAGER')")
    public ResponseEntity<ApiResponse<List<AttendanceDto>>> getCompanyDailyRoster(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<AttendanceDto> list = attendanceService.getCompanyDailyRoster(date);
        return ResponseEntity.ok(ApiResponse.success("Company daily roster fetched", list));
    }

    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('ADMIN','HR','MANAGER')")
    public ResponseEntity<ApiResponse<AttendanceSummaryDto>> getAttendanceSummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        AttendanceSummaryDto summary = attendanceService.getAttendanceSummary(date);
        return ResponseEntity.ok(ApiResponse.success("Attendance summary fetched", summary));
    }
}

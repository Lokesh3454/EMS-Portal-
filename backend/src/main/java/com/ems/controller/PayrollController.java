package com.ems.controller;

import com.ems.dto.ApiResponse;
import com.ems.dto.PayrollRecordDto;
import com.ems.dto.SalaryStructureDto;
import com.ems.service.PayrollService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payroll")
@RequiredArgsConstructor
public class PayrollController {

    private final PayrollService payrollService;

    @GetMapping("/my-payslips")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<PayrollRecordDto>>> getMyPayslips(Authentication authentication) {
        String email = authentication.getName();
        List<PayrollRecordDto> list = payrollService.getMyPayslips(email);
        return ResponseEntity.ok(ApiResponse.success("Fetched employee payslips", list));
    }

    @GetMapping("/my-structure")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<SalaryStructureDto>> getMySalaryStructure(Authentication authentication) {
        String email = authentication.getName();
        SalaryStructureDto struct = payrollService.getMySalaryStructure(email);
        return ResponseEntity.ok(ApiResponse.success("Fetched salary structure", struct));
    }

    @GetMapping("/structure/{employeeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
    public ResponseEntity<ApiResponse<SalaryStructureDto>> getSalaryStructure(@PathVariable Long employeeId) {
        SalaryStructureDto struct = payrollService.getSalaryStructure(employeeId);
        return ResponseEntity.ok(ApiResponse.success("Fetched salary structure", struct));
    }

    @PostMapping("/structure")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<ApiResponse<SalaryStructureDto>> saveSalaryStructure(@RequestBody SalaryStructureDto dto) {
        SalaryStructureDto saved = payrollService.saveSalaryStructure(dto);
        return ResponseEntity.ok(ApiResponse.success("Salary structure saved successfully", saved));
    }

    @GetMapping("/period")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<ApiResponse<List<PayrollRecordDto>>> getPayslipsByPeriod(
            @RequestParam Integer month,
            @RequestParam Integer year) {
        List<PayrollRecordDto> list = payrollService.getPayslipsByPeriod(month, year);
        return ResponseEntity.ok(ApiResponse.success("Fetched period payroll records", list));
    }

    @GetMapping("/department/{deptId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
    public ResponseEntity<ApiResponse<List<PayrollRecordDto>>> getDepartmentPayslips(
            @PathVariable Long deptId,
            @RequestParam Integer month,
            @RequestParam Integer year) {
        List<PayrollRecordDto> list = payrollService.getDepartmentPayslips(deptId, month, year);
        return ResponseEntity.ok(ApiResponse.success("Fetched department payroll records", list));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PayrollRecordDto>> getPayslipById(
            @PathVariable Long id,
            Authentication authentication) {
        boolean isPrivileged = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_HR"));
        PayrollRecordDto record = payrollService.getPayslipById(id, authentication.getName(), isPrivileged);
        return ResponseEntity.ok(ApiResponse.success("Fetched payslip details", record));
    }

    @PostMapping("/generate")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<ApiResponse<List<PayrollRecordDto>>> generatePayroll(
            @RequestParam Integer month,
            @RequestParam Integer year) {
        List<PayrollRecordDto> records = payrollService.generateMonthlyPayroll(month, year);
        return ResponseEntity.ok(ApiResponse.success("Monthly payroll generated successfully", records));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<ApiResponse<PayrollRecordDto>> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String status = body.getOrDefault("status", "PAID");
        PayrollRecordDto updated = payrollService.updatePayrollStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("Payroll status updated to " + status, updated));
    }
}

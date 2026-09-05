package com.ems.controller;

import com.ems.dto.ApiResponse;
import com.ems.dto.CandidateDto;
import com.ems.dto.EmployeeDto;
import com.ems.dto.JobPostingDto;
import com.ems.service.RecruitmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/recruitment")
@RequiredArgsConstructor
public class RecruitmentController {

    private final RecruitmentService recruitmentService;

    @GetMapping("/jobs")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<JobPostingDto>>> getAllJobs() {
        List<JobPostingDto> jobs = recruitmentService.getAllJobs();
        return ResponseEntity.ok(ApiResponse.success("Job postings fetched", jobs));
    }

    @PostMapping("/jobs")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<ApiResponse<JobPostingDto>> createJob(@RequestBody JobPostingDto dto) {
        JobPostingDto created = recruitmentService.createJob(dto);
        return ResponseEntity.ok(ApiResponse.success("Job posting created successfully", created));
    }

    @GetMapping("/candidates")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
    public ResponseEntity<ApiResponse<List<CandidateDto>>> getCandidates(
            @RequestParam(required = false) Long jobId,
            @RequestParam(required = false) String stage) {
        List<CandidateDto> list = recruitmentService.getCandidates(jobId, stage);
        return ResponseEntity.ok(ApiResponse.success("Candidates fetched", list));
    }

    @PutMapping("/candidates/{candidateId}/stage")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
    public ResponseEntity<ApiResponse<CandidateDto>> updateCandidateStage(
            @PathVariable Long candidateId,
            @RequestBody Map<String, String> body) {
        String stage = body.getOrDefault("stage", "INTERVIEW");
        String feedback = body.get("feedback");
        CandidateDto updated = recruitmentService.updateCandidateStage(candidateId, stage, feedback);
        return ResponseEntity.ok(ApiResponse.success("Candidate stage updated to " + stage, updated));
    }

    @PostMapping("/candidates/{candidateId}/convert")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<ApiResponse<EmployeeDto>> convertCandidateToEmployee(@PathVariable Long candidateId) {
        EmployeeDto employee = recruitmentService.convertCandidateToEmployee(candidateId);
        return ResponseEntity.ok(ApiResponse.success("Candidate successfully onboarded as an Employee!", employee));
    }
}

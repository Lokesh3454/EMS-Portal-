package com.ems.controller;

import com.ems.dto.ApiResponse;
import com.ems.dto.EmployeeDocumentDto;
import com.ems.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @GetMapping("/my-documents")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<EmployeeDocumentDto>>> getMyDocuments(Authentication authentication) {
        String email = authentication.getName();
        List<EmployeeDocumentDto> list = documentService.getMyDocuments(email);
        return ResponseEntity.ok(ApiResponse.success("Employee documents fetched successfully", list));
    }

    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
    public ResponseEntity<ApiResponse<List<EmployeeDocumentDto>>> getEmployeeDocuments(@PathVariable Long employeeId) {
        List<EmployeeDocumentDto> list = documentService.getEmployeeDocuments(employeeId);
        return ResponseEntity.ok(ApiResponse.success("Employee documents fetched successfully", list));
    }

    @PostMapping("/{documentId}/sign")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<EmployeeDocumentDto>> signDocument(
            @PathVariable Long documentId,
            Authentication authentication) {
        String email = authentication.getName();
        EmployeeDocumentDto signed = documentService.signDocument(documentId, email);
        return ResponseEntity.ok(ApiResponse.success("Document electronically signed and verified", signed));
    }

    @PostMapping("/upload")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<EmployeeDocumentDto>> uploadDocument(
            @RequestBody EmployeeDocumentDto dto,
            Authentication authentication) {
        String email = authentication.getName();
        boolean isPrivileged = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_HR"));
        EmployeeDocumentDto created = documentService.createDocument(dto, email, isPrivileged);
        return ResponseEntity.ok(ApiResponse.success("Document uploaded successfully to vault", created));
    }
}

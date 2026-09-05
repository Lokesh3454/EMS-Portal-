package com.ems.service;

import com.ems.dto.EmployeeDocumentDto;
import com.ems.entity.Employee;
import com.ems.entity.EmployeeDocument;
import com.ems.exception.ResourceNotFoundException;
import com.ems.repository.EmployeeDocumentRepository;
import com.ems.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentService {

    private final EmployeeDocumentRepository documentRepository;
    private final EmployeeRepository employeeRepository;

    @Transactional
    public List<EmployeeDocumentDto> getMyDocuments(String email) {
        Employee emp = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found for email: " + email));

        List<EmployeeDocument> docs = documentRepository.findByEmployeeIdOrderByCreatedAtDesc(emp.getId());
        if (docs.isEmpty()) {
            createDefaultDocumentsForEmployee(emp);
            docs = documentRepository.findByEmployeeIdOrderByCreatedAtDesc(emp.getId());
        }

        return docs.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional
    public List<EmployeeDocumentDto> getEmployeeDocuments(Long employeeId) {
        Employee emp = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));

        List<EmployeeDocument> docs = documentRepository.findByEmployeeIdOrderByCreatedAtDesc(emp.getId());
        if (docs.isEmpty()) {
            createDefaultDocumentsForEmployee(emp);
            docs = documentRepository.findByEmployeeIdOrderByCreatedAtDesc(emp.getId());
        }

        return docs.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional
    public EmployeeDocumentDto signDocument(Long documentId, String email) {
        EmployeeDocument doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + documentId));

        doc.setIsSigned(true);
        doc.setSignedAt(LocalDateTime.now());
        EmployeeDocument saved = documentRepository.save(doc);
        return toDto(saved);
    }

    @Transactional
    public EmployeeDocumentDto createDocument(EmployeeDocumentDto dto) {
        Employee emp = employeeRepository.findById(dto.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + dto.getEmployeeId()));

        EmployeeDocument doc = new EmployeeDocument();
        doc.setEmployee(emp);
        doc.setTitle(dto.getTitle());
        doc.setDocumentType(EmployeeDocument.DocumentType.valueOf(dto.getDocumentType()));
        doc.setFileSize(dto.getFileSize() != null ? dto.getFileSize() : "1.4 MB");
        doc.setFileType("PDF");
        doc.setUploadDate(LocalDate.now());
        doc.setIsSigned(false);
        doc.setDocumentContent(dto.getDocumentContent());

        EmployeeDocument saved = documentRepository.save(doc);
        return toDto(saved);
    }

    public void createDefaultDocumentsForEmployee(Employee emp) {
        String empName = emp.getFirstName() + " " + emp.getLastName();

        EmployeeDocument d1 = new EmployeeDocument();
        d1.setEmployee(emp);
        d1.setTitle("Official Appointment & Employment Agreement");
        d1.setDocumentType(EmployeeDocument.DocumentType.OFFER_LETTER);
        d1.setFileSize("1.8 MB");
        d1.setFileType("PDF");
        d1.setUploadDate(emp.getDateOfJoining() != null ? emp.getDateOfJoining() : LocalDate.of(2025, 1, 1));
        d1.setIsSigned(true);
        d1.setSignedAt(LocalDateTime.of(2025, 1, 2, 10, 30));
        d1.setDocumentContent("This Employment Agreement confirms the appointment of " + empName + " as " + emp.getDesignation() +
                " in the " + (emp.getDepartment() != null ? emp.getDepartment().getName() : "Enterprise Division") +
                ". The employee agrees to enterprise standards, intellectual property assignments, and core compliance policies.");

        EmployeeDocument d2 = new EmployeeDocument();
        d2.setEmployee(emp);
        d2.setTitle("Corporate Security & Non-Disclosure Agreement (NDA)");
        d2.setDocumentType(EmployeeDocument.DocumentType.POLICY_AGREEMENT);
        d2.setFileSize("840 KB");
        d2.setFileType("PDF");
        d2.setUploadDate(LocalDate.of(2026, 1, 5));
        d2.setIsSigned(true);
        d2.setSignedAt(LocalDateTime.of(2026, 1, 6, 14, 15));
        d2.setDocumentContent("Enterprise Cyber Security & Data Privacy Policy (FY2026). Employees agree to safeguard source code, client records, and access tokens using mandatory two-factor authentication.");

        EmployeeDocument d3 = new EmployeeDocument();
        d3.setEmployee(emp);
        d3.setTitle("Annual Income Tax & Investment Declaration (Form 12BB)");
        d3.setDocumentType(EmployeeDocument.DocumentType.TAX_DECLARATION);
        d3.setFileSize("520 KB");
        d3.setFileType("PDF");
        d3.setUploadDate(LocalDate.of(2026, 7, 10));
        d3.setIsSigned(false);
        d3.setDocumentContent("Annual statutory declaration of investments under Section 80C, 80D, and HRA exemptions for financial year 2026-2027. Requires electronic employee acknowledgment.");

        EmployeeDocument d4 = new EmployeeDocument();
        d4.setEmployee(emp);
        d4.setTitle("Enterprise Code of Conduct & Remote Work Policy");
        d4.setDocumentType(EmployeeDocument.DocumentType.POLICY_AGREEMENT);
        d4.setFileSize("1.1 MB");
        d4.setFileType("PDF");
        d4.setUploadDate(LocalDate.of(2026, 8, 1));
        d4.setIsSigned(false);
        d4.setDocumentContent("Workplace conduct, anti-harassment regulations, and remote shift expectations. Please review and provide your electronic signature to complete mandatory compliance.");

        documentRepository.saveAll(List.of(d1, d2, d3, d4));
    }

    private EmployeeDocumentDto toDto(EmployeeDocument d) {
        Employee emp = d.getEmployee();
        return new EmployeeDocumentDto(
                d.getId(),
                emp.getId(),
                emp.getFirstName() + " " + emp.getLastName(),
                d.getTitle(),
                d.getDocumentType().name(),
                d.getFileSize(),
                d.getFileType(),
                d.getUploadDate(),
                d.getIsSigned(),
                d.getSignedAt(),
                d.getDocumentContent()
        );
    }
}

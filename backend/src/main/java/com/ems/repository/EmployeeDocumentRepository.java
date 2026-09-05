package com.ems.repository;

import com.ems.entity.EmployeeDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeDocumentRepository extends JpaRepository<EmployeeDocument, Long> {
    List<EmployeeDocument> findByEmployeeIdOrderByCreatedAtDesc(Long employeeId);
    List<EmployeeDocument> findByEmployeeIdAndDocumentType(Long employeeId, EmployeeDocument.DocumentType type);
    long countByEmployeeIdAndIsSignedFalse(Long employeeId);
}

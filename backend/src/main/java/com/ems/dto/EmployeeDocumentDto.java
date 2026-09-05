package com.ems.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDocumentDto {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private String title;
    private String documentType;
    private String fileSize;
    private String fileType;
    private LocalDate uploadDate;
    private Boolean isSigned;
    private LocalDateTime signedAt;
    private String documentContent;
}

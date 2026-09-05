package com.ems.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalaryStructureDto {
    private Long id;
    private Long employeeId;
    private String empId;
    private String employeeName;
    private String departmentName;
    private String designation;

    private Double annualCtc;
    private Double monthlyGross;
    private Double basicPay;
    private Double hra;
    private Double specialAllowance;
    private Double conveyanceAllowance;
    private Double medicalAllowance;

    private Double pfEmployee;
    private Double professionalTax;
    private Double incomeTaxTds;

    private String bankName;
    private String accountNumber;
    private String ifscCode;
    private String panNumber;
    private LocalDate effectiveDate;
}

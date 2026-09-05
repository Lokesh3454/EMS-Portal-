package com.ems.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PayrollRecordDto {
    private Long id;
    private Long employeeId;
    private String empId;
    private String employeeName;
    private String departmentName;
    private String designation;
    private String email;

    private Integer payMonth;
    private Integer payYear;
    private String monthName; // e.g. "September 2026"
    private Integer totalDaysInMonth;
    private Double daysWorked;
    private Double paidLeaves;
    private Double lossOfPayDays;

    private Double basicPay;
    private Double hra;
    private Double specialAllowance;
    private Double otherAllowances;
    private Double grossPay;

    private Double pfDeduction;
    private Double professionalTax;
    private Double tdsDeduction;
    private Double lossOfPayDeduction;
    private Double totalDeductions;
    private Double netPay;

    private String status;
    private LocalDate disbursalDate;
    private String paymentMode;
    private String transactionReference;

    private String bankName;
    private String accountNumber;
    private String panNumber;
    private LocalDateTime generatedAt;
}

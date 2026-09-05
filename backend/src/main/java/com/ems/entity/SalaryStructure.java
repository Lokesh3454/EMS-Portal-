package com.ems.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "salary_structures")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalaryStructure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "employee_id", nullable = false, unique = true)
    private Employee employee;

    @Column(name = "annual_ctc", nullable = false)
    private Double annualCtc;

    @Column(name = "monthly_gross", nullable = false)
    private Double monthlyGross;

    @Column(name = "basic_pay", nullable = false)
    private Double basicPay;

    @Column(name = "hra", nullable = false)
    private Double hra;

    @Column(name = "special_allowance", nullable = false)
    private Double specialAllowance;

    @Column(name = "conveyance_allowance")
    private Double conveyanceAllowance = 0.0;

    @Column(name = "medical_allowance")
    private Double medicalAllowance = 0.0;

    // Deductions
    @Column(name = "pf_employee")
    private Double pfEmployee = 0.0;

    @Column(name = "professional_tax")
    private Double professionalTax = 200.0;

    @Column(name = "income_tax_tds")
    private Double incomeTaxTds = 0.0;

    // Banking Details
    @Column(name = "bank_name")
    private String bankName;

    @Column(name = "account_number")
    private String accountNumber;

    @Column(name = "ifsc_code")
    private String ifscCode;

    @Column(name = "pan_number")
    private String panNumber;

    @Column(name = "effective_date")
    private LocalDate effectiveDate;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

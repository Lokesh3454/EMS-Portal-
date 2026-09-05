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
@Table(name = "payroll_records")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PayrollRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "pay_month", nullable = false)
    private Integer payMonth; // 1-12

    @Column(name = "pay_year", nullable = false)
    private Integer payYear; // e.g. 2026

    @Column(name = "total_days_in_month")
    private Integer totalDaysInMonth;

    @Column(name = "days_worked")
    private Double daysWorked;

    @Column(name = "paid_leaves")
    private Double paidLeaves;

    @Column(name = "loss_of_pay_days")
    private Double lossOfPayDays = 0.0;

    // Financial calculations
    @Column(name = "basic_pay")
    private Double basicPay;

    @Column(name = "hra")
    private Double hra;

    @Column(name = "special_allowance")
    private Double specialAllowance;

    @Column(name = "other_allowances")
    private Double otherAllowances = 0.0;

    @Column(name = "gross_pay", nullable = false)
    private Double grossPay;

    @Column(name = "pf_deduction")
    private Double pfDeduction = 0.0;

    @Column(name = "professional_tax")
    private Double professionalTax = 0.0;

    @Column(name = "tds_deduction")
    private Double tdsDeduction = 0.0;

    @Column(name = "loss_of_pay_deduction")
    private Double lossOfPayDeduction = 0.0;

    @Column(name = "total_deductions", nullable = false)
    private Double totalDeductions;

    @Column(name = "net_pay", nullable = false)
    private Double netPay;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private PayrollStatus status = PayrollStatus.PROCESSED;

    @Column(name = "disbursal_date")
    private LocalDate disbursalDate;

    @Column(name = "payment_mode")
    private String paymentMode = "DIRECT_DEPOSIT";

    @Column(name = "transaction_reference")
    private String transactionReference;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum PayrollStatus {
        DRAFT, PROCESSED, PAID, ON_HOLD
    }
}

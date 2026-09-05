package com.ems.service;

import com.ems.dto.PayrollRecordDto;
import com.ems.dto.SalaryStructureDto;
import com.ems.entity.Employee;
import com.ems.entity.PayrollRecord;
import com.ems.entity.SalaryStructure;
import com.ems.exception.ResourceNotFoundException;
import com.ems.repository.EmployeeRepository;
import com.ems.repository.PayrollRecordRepository;
import com.ems.repository.SalaryStructureRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PayrollService {

    private final SalaryStructureRepository salaryStructureRepository;
    private final PayrollRecordRepository payrollRecordRepository;
    private final EmployeeRepository employeeRepository;

    @Transactional(readOnly = true)
    public SalaryStructureDto getSalaryStructure(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));

        SalaryStructure structure = salaryStructureRepository.findByEmployeeId(employeeId)
                .orElseGet(() -> createDefaultSalaryStructure(employee));

        return toStructureDto(structure);
    }

    @Transactional(readOnly = true)
    public SalaryStructureDto getMySalaryStructure(String email) {
        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with email: " + email));

        SalaryStructure structure = salaryStructureRepository.findByEmployeeId(employee.getId())
                .orElseGet(() -> createDefaultSalaryStructure(employee));

        return toStructureDto(structure);
    }

    @Transactional
    public SalaryStructureDto saveSalaryStructure(SalaryStructureDto dto) {
        Employee employee = employeeRepository.findById(dto.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + dto.getEmployeeId()));

        SalaryStructure structure = salaryStructureRepository.findByEmployeeId(employee.getId())
                .orElse(new SalaryStructure());

        structure.setEmployee(employee);
        structure.setAnnualCtc(dto.getAnnualCtc());
        structure.setMonthlyGross(dto.getMonthlyGross());
        structure.setBasicPay(dto.getBasicPay());
        structure.setHra(dto.getHra());
        structure.setSpecialAllowance(dto.getSpecialAllowance());
        structure.setConveyanceAllowance(dto.getConveyanceAllowance() != null ? dto.getConveyanceAllowance() : 0.0);
        structure.setMedicalAllowance(dto.getMedicalAllowance() != null ? dto.getMedicalAllowance() : 0.0);

        structure.setPfEmployee(dto.getPfEmployee() != null ? dto.getPfEmployee() : 0.0);
        structure.setProfessionalTax(dto.getProfessionalTax() != null ? dto.getProfessionalTax() : 200.0);
        structure.setIncomeTaxTds(dto.getIncomeTaxTds() != null ? dto.getIncomeTaxTds() : 0.0);

        structure.setBankName(dto.getBankName());
        structure.setAccountNumber(dto.getAccountNumber());
        structure.setIfscCode(dto.getIfscCode());
        structure.setPanNumber(dto.getPanNumber());
        structure.setEffectiveDate(dto.getEffectiveDate() != null ? dto.getEffectiveDate() : LocalDate.now());

        SalaryStructure saved = salaryStructureRepository.save(structure);
        return toStructureDto(saved);
    }

    @Transactional(readOnly = true)
    public List<PayrollRecordDto> getMyPayslips(String email) {
        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with email: " + email));

        return payrollRecordRepository.findByEmployeeIdOrderByPayYearDescPayMonthDesc(employee.getId())
                .stream()
                .map(this::toRecordDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PayrollRecordDto> getPayslipsByPeriod(Integer month, Integer year) {
        return payrollRecordRepository.findByPayMonthAndPayYear(month, year)
                .stream()
                .map(this::toRecordDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PayrollRecordDto> getDepartmentPayslips(Long deptId, Integer month, Integer year) {
        return payrollRecordRepository.findByEmployeeDepartmentIdAndPayMonthAndPayYear(deptId, month, year)
                .stream()
                .map(this::toRecordDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PayrollRecordDto getPayslipById(Long id) {
        PayrollRecord record = payrollRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payroll record not found with id: " + id));
        return toRecordDto(record);
    }

    @Transactional
    public List<PayrollRecordDto> generateMonthlyPayroll(Integer month, Integer year) {
        YearMonth ym = YearMonth.of(year, month);
        int totalDays = ym.lengthOfMonth();

        List<Employee> activeEmployees = employeeRepository.findAll().stream()
                .filter(e -> e.getStatus() == Employee.EmployeeStatus.ACTIVE)
                .collect(Collectors.toList());

        for (Employee emp : activeEmployees) {
            SalaryStructure structure = salaryStructureRepository.findByEmployeeId(emp.getId())
                    .orElseGet(() -> createDefaultSalaryStructure(emp));

            double gross = structure.getMonthlyGross();
            double dailyRate = gross / totalDays;

            // Standard worked days with reasonable default
            double workedDays = totalDays - 2.0;
            double paidLeaves = 2.0;
            double lopDays = 0.0;

            double lopDeduction = lopDays * dailyRate;
            double pf = structure.getPfEmployee();
            double pt = structure.getProfessionalTax();
            double tds = structure.getIncomeTaxTds();
            double totalDeductions = pf + pt + tds + lopDeduction;
            double netPay = Math.max(0.0, gross - totalDeductions);

            PayrollRecord record = payrollRecordRepository.findByEmployeeIdAndPayMonthAndPayYear(emp.getId(), month, year)
                    .orElse(new PayrollRecord());

            record.setEmployee(emp);
            record.setPayMonth(month);
            record.setPayYear(year);
            record.setTotalDaysInMonth(totalDays);
            record.setDaysWorked(workedDays);
            record.setPaidLeaves(paidLeaves);
            record.setLossOfPayDays(lopDays);

            record.setBasicPay(structure.getBasicPay());
            record.setHra(structure.getHra());
            record.setSpecialAllowance(structure.getSpecialAllowance());
            record.setOtherAllowances(structure.getConveyanceAllowance() + structure.getMedicalAllowance());
            record.setGrossPay(gross);

            record.setPfDeduction(pf);
            record.setProfessionalTax(pt);
            record.setTdsDeduction(tds);
            record.setLossOfPayDeduction(lopDeduction);
            record.setTotalDeductions(totalDeductions);
            record.setNetPay(netPay);

            record.setStatus(PayrollRecord.PayrollStatus.PAID);
            record.setDisbursalDate(LocalDate.of(year, month, Math.min(28, totalDays)));
            record.setPaymentMode("DIRECT_DEPOSIT");
            if (record.getTransactionReference() == null) {
                record.setTransactionReference("TXN-" + year + month + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            }

            payrollRecordRepository.save(record);
        }

        return getPayslipsByPeriod(month, year);
    }

    @Transactional
    public PayrollRecordDto updatePayrollStatus(Long id, String status) {
        PayrollRecord record = payrollRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payroll record not found with id: " + id));

        record.setStatus(PayrollRecord.PayrollStatus.valueOf(status.toUpperCase()));
        if (record.getStatus() == PayrollRecord.PayrollStatus.PAID && record.getDisbursalDate() == null) {
            record.setDisbursalDate(LocalDate.now());
        }
        return toRecordDto(payrollRecordRepository.save(record));
    }

    public SalaryStructure createDefaultSalaryStructure(Employee emp) {
        SalaryStructure s = new SalaryStructure();
        s.setEmployee(emp);

        double annualCtc = 960000.0;
        String desig = emp.getDesignation() != null ? emp.getDesignation().toLowerCase() : "";
        if (desig.contains("lead") || desig.contains("architect") || desig.contains("manager")) {
            annualCtc = 1500000.0;
        } else if (desig.contains("senior")) {
            annualCtc = 1200000.0;
        } else if (desig.contains("intern") || desig.contains("trainee")) {
            annualCtc = 480000.0;
        }

        double monthlyGross = annualCtc / 12.0;
        double basic = Math.round(monthlyGross * 0.50);
        double hra = Math.round(monthlyGross * 0.20);
        double special = Math.round(monthlyGross * 0.20);
        double conveyance = Math.round(monthlyGross * 0.05);
        double medical = monthlyGross - (basic + hra + special + conveyance);

        double pf = Math.round(basic * 0.12);
        double pt = 200.0;
        double tds = monthlyGross > 80000 ? 3500.0 : (monthlyGross > 50000 ? 1800.0 : 500.0);

        s.setAnnualCtc(annualCtc);
        s.setMonthlyGross(monthlyGross);
        s.setBasicPay(basic);
        s.setHra(hra);
        s.setSpecialAllowance(special);
        s.setConveyanceAllowance(conveyance);
        s.setMedicalAllowance(medical);

        s.setPfEmployee(pf);
        s.setProfessionalTax(pt);
        s.setIncomeTaxTds(tds);

        s.setBankName("HDFC Bank Corp");
        s.setAccountNumber("50100" + (1000000 + (emp.getId() != null ? emp.getId() * 3719 : 4567)));
        s.setIfscCode("HDFC0001244");
        s.setPanNumber("ABCDE" + (1000 + (emp.getId() != null ? emp.getId() : 1)) + "Z");
        s.setEffectiveDate(LocalDate.of(2026, 1, 1));

        return salaryStructureRepository.save(s);
    }

    private SalaryStructureDto toStructureDto(SalaryStructure s) {
        Employee emp = s.getEmployee();
        return new SalaryStructureDto(
                s.getId(),
                emp.getId(),
                emp.getEmpId(),
                emp.getFirstName() + " " + emp.getLastName(),
                emp.getDepartment() != null ? emp.getDepartment().getName() : "Unassigned",
                emp.getDesignation(),
                s.getAnnualCtc(),
                s.getMonthlyGross(),
                s.getBasicPay(),
                s.getHra(),
                s.getSpecialAllowance(),
                s.getConveyanceAllowance(),
                s.getMedicalAllowance(),
                s.getPfEmployee(),
                s.getProfessionalTax(),
                s.getIncomeTaxTds(),
                s.getBankName(),
                s.getAccountNumber(),
                s.getIfscCode(),
                s.getPanNumber(),
                s.getEffectiveDate()
        );
    }

    private PayrollRecordDto toRecordDto(PayrollRecord r) {
        Employee emp = r.getEmployee();
        String monthName = Month.of(r.getPayMonth()).getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + r.getPayYear();

        SalaryStructure struct = salaryStructureRepository.findByEmployeeId(emp.getId()).orElse(null);
        String bank = struct != null ? struct.getBankName() : "HDFC Bank";
        String account = struct != null ? struct.getAccountNumber() : "******";
        String pan = struct != null ? struct.getPanNumber() : "******";

        return new PayrollRecordDto(
                r.getId(),
                emp.getId(),
                emp.getEmpId(),
                emp.getFirstName() + " " + emp.getLastName(),
                emp.getDepartment() != null ? emp.getDepartment().getName() : "General",
                emp.getDesignation(),
                emp.getEmail(),
                r.getPayMonth(),
                r.getPayYear(),
                monthName,
                r.getTotalDaysInMonth(),
                r.getDaysWorked(),
                r.getPaidLeaves(),
                r.getLossOfPayDays(),
                r.getBasicPay(),
                r.getHra(),
                r.getSpecialAllowance(),
                r.getOtherAllowances(),
                r.getGrossPay(),
                r.getPfDeduction(),
                r.getProfessionalTax(),
                r.getTdsDeduction(),
                r.getLossOfPayDeduction(),
                r.getTotalDeductions(),
                r.getNetPay(),
                r.getStatus().name(),
                r.getDisbursalDate(),
                r.getPaymentMode(),
                r.getTransactionReference(),
                bank,
                account,
                pan,
                r.getCreatedAt()
        );
    }
}

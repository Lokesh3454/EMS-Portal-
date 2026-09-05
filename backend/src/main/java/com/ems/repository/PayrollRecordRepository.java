package com.ems.repository;

import com.ems.entity.PayrollRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PayrollRecordRepository extends JpaRepository<PayrollRecord, Long> {
    List<PayrollRecord> findByEmployeeIdOrderByPayYearDescPayMonthDesc(Long employeeId);
    List<PayrollRecord> findByPayMonthAndPayYear(Integer payMonth, Integer payYear);
    Optional<PayrollRecord> findByEmployeeIdAndPayMonthAndPayYear(Long employeeId, Integer payMonth, Integer payYear);
    List<PayrollRecord> findByEmployeeDepartmentIdAndPayMonthAndPayYear(Long departmentId, Integer payMonth, Integer payYear);
    boolean existsByEmployeeIdAndPayMonthAndPayYear(Long employeeId, Integer payMonth, Integer payYear);
}

package com.ems.repository;

import com.ems.entity.SalaryStructure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SalaryStructureRepository extends JpaRepository<SalaryStructure, Long> {
    Optional<SalaryStructure> findByEmployeeId(Long employeeId);
    Optional<SalaryStructure> findByEmployeeEmpId(String empId);
    boolean existsByEmployeeId(Long employeeId);
}

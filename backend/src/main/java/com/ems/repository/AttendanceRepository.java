package com.ems.repository;

import com.ems.entity.Attendance;
import com.ems.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    Optional<Attendance> findByEmployeeAndDate(Employee employee, LocalDate date);
    List<Attendance> findByEmployeeAndDateBetweenOrderByDateDesc(Employee employee, LocalDate startDate, LocalDate endDate);
    List<Attendance> findByDateOrderByCreatedAtDesc(LocalDate date);
    long countByDateAndStatus(LocalDate date, Attendance.AttendanceStatus status);
}

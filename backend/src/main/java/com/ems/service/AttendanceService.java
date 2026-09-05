package com.ems.service;

import com.ems.dto.AttendanceDto;
import com.ems.dto.AttendanceSummaryDto;
import com.ems.entity.Attendance;
import com.ems.entity.Department;
import com.ems.entity.Employee;
import com.ems.entity.User;
import com.ems.exception.ResourceNotFoundException;
import com.ems.repository.AttendanceRepository;
import com.ems.repository.DepartmentRepository;
import com.ems.repository.EmployeeRepository;
import com.ems.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;

    private static final LocalTime STANDARD_START_TIME = LocalTime.of(9, 30); // 9:30 AM is standard threshold

    @Transactional
    public AttendanceDto clockIn(String email, String notes) {
        Employee employee = getOrCreateEmployeeForEmail(email);
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        Optional<Attendance> existing = attendanceRepository.findByEmployeeAndDate(employee, today);
        if (existing.isPresent()) {
            throw new IllegalArgumentException("You have already clocked in today at " + existing.get().getClockInTime());
        }

        Attendance.AttendanceStatus status = now.isAfter(STANDARD_START_TIME) 
                ? Attendance.AttendanceStatus.LATE 
                : Attendance.AttendanceStatus.PRESENT;

        Attendance attendance = Attendance.builder()
                .employee(employee)
                .date(today)
                .clockInTime(now)
                .status(status)
                .notes(notes)
                .build();

        Attendance saved = attendanceRepository.save(attendance);
        return toDto(saved);
    }

    @Transactional
    public AttendanceDto clockOut(String email, String notes) {
        Employee employee = getOrCreateEmployeeForEmail(email);
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        Attendance attendance = attendanceRepository.findByEmployeeAndDate(employee, today)
                .orElseThrow(() -> new IllegalArgumentException("No clock-in record found for today. Please clock in first."));

        if (attendance.getClockOutTime() != null) {
            throw new IllegalArgumentException("You have already clocked out today at " + attendance.getClockOutTime());
        }

        attendance.setClockOutTime(now);

        Duration duration = Duration.between(attendance.getClockInTime(), now);
        double hours = Math.round((duration.toMinutes() / 60.0) * 100.0) / 100.0;
        attendance.setTotalHours(hours);

        if (hours < 4.5 && attendance.getStatus() == Attendance.AttendanceStatus.PRESENT) {
            attendance.setStatus(Attendance.AttendanceStatus.HALF_DAY);
        }

        if (notes != null && !notes.isBlank()) {
            attendance.setNotes(attendance.getNotes() != null ? attendance.getNotes() + " | " + notes : notes);
        }

        Attendance saved = attendanceRepository.save(attendance);
        return toDto(saved);
    }

    public AttendanceDto getTodayStatus(String email) {
        Employee employee = getOrCreateEmployeeForEmail(email);
        LocalDate today = LocalDate.now();
        return attendanceRepository.findByEmployeeAndDate(employee, today)
                .map(this::toDto)
                .orElse(null);
    }

    public List<AttendanceDto> getMyMonthlyAttendance(String email, int year, int month) {
        Employee employee = getOrCreateEmployeeForEmail(email);
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.plusMonths(1).minusDays(1);

        return attendanceRepository.findByEmployeeAndDateBetweenOrderByDateDesc(employee, start, end)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<AttendanceDto> getCompanyDailyRoster(LocalDate date) {
        LocalDate target = date != null ? date : LocalDate.now();
        return attendanceRepository.findByDateOrderByCreatedAtDesc(target)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public AttendanceSummaryDto getAttendanceSummary(LocalDate date) {
        LocalDate target = date != null ? date : LocalDate.now();
        long totalEmployees = employeeRepository.count();
        long present = attendanceRepository.countByDateAndStatus(target, Attendance.AttendanceStatus.PRESENT);
        long late = attendanceRepository.countByDateAndStatus(target, Attendance.AttendanceStatus.LATE);
        long halfDay = attendanceRepository.countByDateAndStatus(target, Attendance.AttendanceStatus.HALF_DAY);
        long onLeave = attendanceRepository.countByDateAndStatus(target, Attendance.AttendanceStatus.ON_LEAVE);

        long totalActiveToday = present + late + halfDay;
        long absent = Math.max(0, totalEmployees - (totalActiveToday + onLeave));
        double rate = totalEmployees > 0 ? Math.round(((double) totalActiveToday / totalEmployees) * 1000.0) / 10.0 : 0.0;

        return AttendanceSummaryDto.builder()
                .totalEmployees(totalEmployees)
                .presentToday(present)
                .lateToday(late)
                .onLeaveToday(onLeave)
                .absentToday(absent)
                .attendanceRate(rate)
                .build();
    }

    private Employee getOrCreateEmployeeForEmail(String email) {
        return employeeRepository.findByEmail(email).orElseGet(() -> {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found for email: " + email));

            Department adminDept = departmentRepository.findByName("Administration")
                    .or(() -> departmentRepository.findAll().stream().findFirst())
                    .orElse(null);

            Employee adminEmp = new Employee();
            adminEmp.setEmpId("ADM001");
            adminEmp.setFirstName("System");
            adminEmp.setLastName("Administrator");
            adminEmp.setEmail(user.getEmail());
            adminEmp.setPhone("+1 555-0100");
            adminEmp.setDesignation("System Administrator");
            adminEmp.setDepartment(adminDept);
            adminEmp.setDateOfJoining(LocalDate.of(2023, 1, 1));
            adminEmp.setStatus(Employee.EmployeeStatus.ACTIVE);
            adminEmp.setAddress("Headquarters, Executive Suite 100");
            adminEmp.setUser(user);
            return employeeRepository.save(adminEmp);
        });
    }

    private AttendanceDto toDto(Attendance attendance) {
        Employee emp = attendance.getEmployee();
        return AttendanceDto.builder()
                .id(attendance.getId())
                .employeeId(emp.getId())
                .employeeName(emp.getFirstName() + " " + emp.getLastName())
                .employeeCode(emp.getEmpId())
                .departmentName(emp.getDepartment() != null ? emp.getDepartment().getName() : "Unassigned")
                .date(attendance.getDate())
                .clockInTime(attendance.getClockInTime())
                .clockOutTime(attendance.getClockOutTime())
                .totalHours(attendance.getTotalHours())
                .status(attendance.getStatus().name())
                .notes(attendance.getNotes())
                .build();
    }
}

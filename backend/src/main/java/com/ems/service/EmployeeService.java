package com.ems.service;

import com.ems.dto.EmployeeDto;
import com.ems.entity.Department;
import com.ems.entity.Employee;
import com.ems.entity.Role;
import com.ems.entity.User;
import com.ems.exception.ResourceNotFoundException;
import com.ems.repository.DepartmentRepository;
import com.ems.repository.EmployeeRepository;
import com.ems.repository.RoleRepository;
import com.ems.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public Page<EmployeeDto> getAllEmployees(String search, Long departmentId,
                                             String status, int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Employee.EmployeeStatus empStatus = null;
        if (status != null && !status.isEmpty()) {
            try { empStatus = Employee.EmployeeStatus.valueOf(status); } catch (Exception ignored) {}
        }

        Page<Employee> employees = employeeRepository.searchEmployees(
                (search != null && !search.isEmpty()) ? search : null,
                departmentId, empStatus, pageable);

        return employees.map(EmployeeDto::fromEntity);
    }

    public EmployeeDto getEmployeeById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));
        return EmployeeDto.fromEntity(employee);
    }

    public EmployeeDto getMyProfile(String email) {
        Optional<Employee> empOpt = employeeRepository.findByEmail(email);
        if (empOpt.isPresent()) {
            return EmployeeDto.fromEntity(empOpt.get());
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found for email: " + email));

        Department adminDept = departmentRepository.findByName("Administration")
                .or(() -> departmentRepository.findAll().stream().findFirst())
                .orElse(null);

        Employee adminEmp = new Employee();
        String adminCode = "ADM001";
        if (employeeRepository.existsByEmpId(adminCode)) {
            adminCode = generateEmpId();
        }
        adminEmp.setEmpId(adminCode);
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
        Employee saved = employeeRepository.save(adminEmp);
        return EmployeeDto.fromEntity(saved);
    }

    @Transactional
    public EmployeeDto updateMyProfile(String email, EmployeeDto dto) {
        Employee employee = employeeRepository.findByEmail(email).orElseGet(() -> {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
            Employee e = new Employee();
            String code = "ADM001";
            if (employeeRepository.existsByEmpId(code)) {
                code = generateEmpId();
            }
            e.setEmpId(code);
            e.setFirstName("System");
            e.setLastName("Administrator");
            e.setEmail(email);
            e.setUser(user);
            return employeeRepository.save(e);
        });

        if (dto.getFirstName() != null && !dto.getFirstName().isEmpty()) employee.setFirstName(dto.getFirstName());
        if (dto.getLastName() != null && !dto.getLastName().isEmpty()) employee.setLastName(dto.getLastName());
        if (dto.getPhone() != null) employee.setPhone(dto.getPhone());
        if (dto.getAddress() != null) employee.setAddress(dto.getAddress());
        if (dto.getDateOfBirth() != null) employee.setDateOfBirth(dto.getDateOfBirth());
        if (dto.getGender() != null) {
            try { employee.setGender(Employee.Gender.valueOf(dto.getGender())); } catch (Exception ignored) {}
        }
        Employee saved = employeeRepository.save(employee);
        return EmployeeDto.fromEntity(saved);
    }

    @Transactional
    public EmployeeDto createEmployee(EmployeeDto dto) {
        if (employeeRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("An employee with this email already exists");
        }

        Employee employee = new Employee();
        employee.setEmpId(generateEmpId());
        mapDtoToEntity(dto, employee);
        employee.setStatus(Employee.EmployeeStatus.ACTIVE);

        // Create user account
        Role employeeRole = roleRepository.findByName(Role.RoleName.ROLE_EMPLOYEE)
                .orElseThrow(() -> new RuntimeException("Employee role not found"));

        if (!userRepository.existsByEmail(dto.getEmail())) {
            User user = new User();
            user.setEmail(dto.getEmail());
            user.setPassword(passwordEncoder.encode("Welcome@123"));
            user.setRole(employeeRole);
            user.setIsActive(true);
            user.setFirstLogin(true);
            User savedUser = userRepository.save(user);
            employee.setUser(savedUser);
        }

        Employee saved = employeeRepository.save(employee);
        return EmployeeDto.fromEntity(saved);
    }

    @Transactional
    public EmployeeDto updateEmployee(Long id, EmployeeDto dto) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));

        if (!employee.getEmail().equals(dto.getEmail()) && employeeRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("An employee with this email already exists");
        }

        mapDtoToEntity(dto, employee);
        Employee updated = employeeRepository.save(employee);
        return EmployeeDto.fromEntity(updated);
    }

    @Transactional
    public void deleteEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));
        // Soft delete — mark as INACTIVE
        employee.setStatus(Employee.EmployeeStatus.INACTIVE);
        employeeRepository.save(employee);
    }

    @Transactional
    public void permanentDeleteEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));
        employeeRepository.delete(employee);
    }

    public Map<String, Long> getDashboardStats() {
        long total = employeeRepository.count();
        long active = employeeRepository.countByStatus(Employee.EmployeeStatus.ACTIVE);
        long inactive = employeeRepository.countByStatus(Employee.EmployeeStatus.INACTIVE);
        return Map.of("total", total, "active", active, "inactive", inactive);
    }

    private void mapDtoToEntity(EmployeeDto dto, Employee employee) {
        employee.setFirstName(dto.getFirstName());
        employee.setLastName(dto.getLastName());
        employee.setEmail(dto.getEmail());
        employee.setPhone(dto.getPhone());
        employee.setAddress(dto.getAddress());
        employee.setDesignation(dto.getDesignation());
        employee.setDateOfBirth(dto.getDateOfBirth());
        employee.setDateOfJoining(dto.getDateOfJoining());

        if (dto.getGender() != null) {
            try { employee.setGender(Employee.Gender.valueOf(dto.getGender())); } catch (Exception ignored) {}
        }
        if (dto.getEmploymentType() != null) {
            try { employee.setEmploymentType(Employee.EmploymentType.valueOf(dto.getEmploymentType())); } catch (Exception ignored) {}
        }
        if (dto.getStatus() != null) {
            try { employee.setStatus(Employee.EmployeeStatus.valueOf(dto.getStatus())); } catch (Exception ignored) {}
        }
        if (dto.getDepartmentId() != null) {
            Department dept = departmentRepository.findById(dto.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found"));
            employee.setDepartment(dept);
        }
    }

    public String generateEmpId() {
        long nextNum = employeeRepository.count() + 1;
        String candidate = String.format("EMP%03d", nextNum);
        while (employeeRepository.existsByEmpId(candidate)) {
            nextNum++;
            candidate = String.format("EMP%03d", nextNum);
        }
        return candidate;
    }
}

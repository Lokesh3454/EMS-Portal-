package com.ems.config;

import com.ems.entity.Department;
import com.ems.entity.Role;
import com.ems.entity.User;
import com.ems.entity.Employee;
import com.ems.entity.Attendance;
import com.ems.entity.LeaveBalance;
import com.ems.repository.DepartmentRepository;
import com.ems.repository.RoleRepository;
import com.ems.repository.UserRepository;
import com.ems.repository.EmployeeRepository;
import com.ems.repository.AttendanceRepository;
import com.ems.repository.LeaveBalanceRepository;
import com.ems.service.PayrollService;
import com.ems.service.PerformanceService;
import com.ems.service.DocumentService;
import com.ems.service.RecruitmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final AttendanceRepository attendanceRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final PayrollService payrollService;
    private final PerformanceService performanceService;
    private final DocumentService documentService;
    private final RecruitmentService recruitmentService;

    @Override
    public void run(String... args) {
        seedRoles();
        seedDepartments();
        seedAdminUser();
        seedDemoUsersAndEmployees();
        seedTenAdditionalEmployees();
        seedPayrollAndCompensation();
        seedPerformanceAndAppraisals();
        seedPhaseFourData();
    }

    private void seedRoles() {
        for (Role.RoleName roleName : Role.RoleName.values()) {
            if (roleRepository.findByName(roleName).isEmpty()) {
                roleRepository.save(new Role(null, roleName));
                log.info("Created role: {}", roleName);
            }
        }
    }

    private void seedDepartments() {
        List<String[]> departments = List.of(
            new String[]{"Information Technology", "IT Department - Software and Infrastructure"},
            new String[]{"Human Resources", "HR Department - People and Culture"},
            new String[]{"Finance", "Finance and Accounting Department"},
            new String[]{"Marketing", "Marketing and Brand Management"},
            new String[]{"Sales", "Sales and Business Development"},
            new String[]{"Operations", "Operations and Process Management"},
            new String[]{"Administration", "Administrative and Support Services"}
        );

        for (String[] dept : departments) {
            if (!departmentRepository.existsByName(dept[0])) {
                departmentRepository.save(new Department(null, dept[0], dept[1], null));
                log.info("Created department: {}", dept[0]);
            }
        }
    }

    private void seedAdminUser() {
        String adminEmail = "admin@ems.com";
        if (userRepository.findByEmail(adminEmail).isEmpty()) {
            Role adminRole = roleRepository.findByName(Role.RoleName.ROLE_ADMIN)
                    .orElseThrow(() -> new RuntimeException("Admin role not found"));

            User admin = new User();
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode("Admin@123"));
            admin.setRole(adminRole);
            admin.setIsActive(true);
            admin.setFirstLogin(false);
            userRepository.save(admin);

            log.info("===========================================");
            log.info("  DEFAULT ADMIN CREDENTIALS CREATED");
            log.info("  Email   : admin@ems.com");
            log.info("  Password: Admin@123");
            log.info("===========================================");
        }
    }

    private void seedDemoUsersAndEmployees() {
        Role employeeRole = roleRepository.findByName(Role.RoleName.ROLE_EMPLOYEE)
                .orElseThrow(() -> new RuntimeException("Employee role not found"));
        Role hrRole = roleRepository.findByName(Role.RoleName.ROLE_HR)
                .orElseThrow(() -> new RuntimeException("HR role not found"));

        Department itDept = departmentRepository.findAll().stream().findFirst().orElse(null);

        // 1. Employee: Lokesh Mahesh
        String empEmail = "lokesh.mahesh@ems.com";
        User empUser = userRepository.findByEmail(empEmail).orElseGet(() -> {
            User u = new User();
            u.setEmail(empEmail);
            u.setPassword(passwordEncoder.encode("Employee@123"));
            u.setRole(employeeRole);
            u.setIsActive(true);
            u.setFirstLogin(false);
            return userRepository.save(u);
        });

        if (employeeRepository.findByEmail(empEmail).isEmpty()) {
            Employee emp = new Employee();
            emp.setEmpId("EMP001");
            emp.setFirstName("Lokesh");
            emp.setLastName("Mahesh");
            emp.setEmail(empEmail);
            emp.setPhone("+1 555-0199");
            emp.setDesignation("Lead Software Architect");
            emp.setDepartment(itDept);
            emp.setDateOfJoining(LocalDate.of(2024, 1, 10));
            emp.setStatus(Employee.EmployeeStatus.ACTIVE);
            emp.setAddress("124 Innovation Way, Tech Park");
            emp.setUser(empUser);
            employeeRepository.save(emp);
            log.info("Seeded employee: {}", empEmail);
        } else {
            Employee emp = employeeRepository.findByEmail(empEmail).get();
            if (emp.getUser() == null) {
                emp.setUser(empUser);
                employeeRepository.save(emp);
            }
        }

        // 2. HR User: Sarah Connor
        String hrEmail = "sarah.connor@ems.com";
        User hrUser = userRepository.findByEmail(hrEmail).orElseGet(() -> {
            User u = new User();
            u.setEmail(hrEmail);
            u.setPassword(passwordEncoder.encode("Hr@123"));
            u.setRole(hrRole);
            u.setIsActive(true);
            u.setFirstLogin(false);
            return userRepository.save(u);
        });

        if (employeeRepository.findByEmail(hrEmail).isEmpty()) {
            Employee emp = new Employee();
            emp.setEmpId("EMP002");
            emp.setFirstName("Sarah");
            emp.setLastName("Connor");
            emp.setEmail(hrEmail);
            emp.setPhone("+1 555-0144");
            emp.setDesignation("HR Director");
            emp.setDepartment(itDept);
            emp.setDateOfJoining(LocalDate.of(2023, 6, 15));
            emp.setStatus(Employee.EmployeeStatus.ACTIVE);
            emp.setAddress("89 Sunset Blvd, Suite 400");
            emp.setUser(hrUser);
            employeeRepository.save(emp);
            log.info("Seeded HR: {}", hrEmail);
        } else {
            Employee emp = employeeRepository.findByEmail(hrEmail).get();
            if (emp.getUser() == null) {
                emp.setUser(hrUser);
                employeeRepository.save(emp);
            }
        }

        // 3. Manager: Vikramaditya Rao (Engineering Manager)
        Role managerRole = roleRepository.findByName(Role.RoleName.ROLE_MANAGER)
                .orElseThrow(() -> new RuntimeException("Manager role not found"));
        String mgrEmail = "manager@ems.com";
        User mgrUser = userRepository.findByEmail(mgrEmail).orElseGet(() -> {
            User u = new User();
            u.setEmail(mgrEmail);
            u.setPassword(passwordEncoder.encode("Manager@123"));
            u.setRole(managerRole);
            u.setIsActive(true);
            u.setFirstLogin(false);
            return userRepository.save(u);
        });

        if (employeeRepository.findByEmail(mgrEmail).isEmpty()) {
            Employee emp = new Employee();
            emp.setEmpId("MGR001");
            emp.setFirstName("Vikramaditya");
            emp.setLastName("Rao");
            emp.setEmail(mgrEmail);
            emp.setPhone("+91 98200 11002");
            emp.setDesignation("Engineering Manager");
            emp.setDepartment(itDept);
            emp.setDateOfJoining(LocalDate.of(2022, 3, 1));
            emp.setStatus(Employee.EmployeeStatus.ACTIVE);
            emp.setAddress("42 Silicon Hills, Bangalore");
            emp.setUser(mgrUser);
            employeeRepository.save(emp);
            log.info("Seeded Manager: {}", mgrEmail);
        }

        // 4. Team Member: Neha Singhania
        String nehaEmail = "neha.singhania@ems.com";
        User nehaUser = userRepository.findByEmail(nehaEmail).orElseGet(() -> {
            User u = new User();
            u.setEmail(nehaEmail);
            u.setPassword(passwordEncoder.encode("Employee@123"));
            u.setRole(employeeRole);
            u.setIsActive(true);
            u.setFirstLogin(false);
            return userRepository.save(u);
        });

        if (employeeRepository.findByEmail(nehaEmail).isEmpty()) {
            Employee emp = new Employee();
            emp.setEmpId("EMP005");
            emp.setFirstName("Neha");
            emp.setLastName("Singhania");
            emp.setEmail(nehaEmail);
            emp.setPhone("+91 98200 11008");
            emp.setDesignation("Senior Cloud Engineer");
            emp.setDepartment(itDept);
            emp.setDateOfJoining(LocalDate.of(2024, 4, 15));
            emp.setStatus(Employee.EmployeeStatus.ACTIVE);
            emp.setAddress("77 Cloud Towers, Pune");
            emp.setUser(nehaUser);
            employeeRepository.save(emp);
            log.info("Seeded team member: {}", nehaEmail);
        }

        // 5. Team Member: Arun Kumar
        String arunEmail = "arun.kumar@ems.com";
        User arunUser = userRepository.findByEmail(arunEmail).orElseGet(() -> {
            User u = new User();
            u.setEmail(arunEmail);
            u.setPassword(passwordEncoder.encode("Employee@123"));
            u.setRole(employeeRole);
            u.setIsActive(true);
            u.setFirstLogin(false);
            return userRepository.save(u);
        });

        if (employeeRepository.findByEmail(arunEmail).isEmpty()) {
            Employee emp = new Employee();
            emp.setEmpId("EMP006");
            emp.setFirstName("Arun");
            emp.setLastName("Kumar");
            emp.setEmail(arunEmail);
            emp.setPhone("+91 98200 11005");
            emp.setDesignation("Fullstack Developer");
            emp.setDepartment(itDept);
            emp.setDateOfJoining(LocalDate.of(2024, 6, 1));
            emp.setStatus(Employee.EmployeeStatus.ACTIVE);
            emp.setAddress("18 Residency Road, Hyderabad");
            emp.setUser(arunUser);
            employeeRepository.save(emp);
            log.info("Seeded team member: {}", arunEmail);
        }
    }

    private void seedTenAdditionalEmployees() {
        Role employeeRole = roleRepository.findByName(Role.RoleName.ROLE_EMPLOYEE)
                .orElseThrow(() -> new RuntimeException("Employee role not found"));

        List<Department> allDepts = departmentRepository.findAll();

        Object[][] additionalData = new Object[][]{
            {"EMP007", "Kavita", "Menon", "kavita.menon@ems.com", "+91 98200 11009", "QA Automation Lead", "Information Technology", Employee.Gender.FEMALE, 9, 5, false},
            {"EMP008", "Rohan", "Deshmukh", "rohan.deshmukh@ems.com", "+91 98200 11012", "DevOps Architect", "Information Technology", Employee.Gender.MALE, 9, 20, false},
            {"EMP009", "Priya", "Nair", "priya.nair@ems.com", "+91 98200 11004", "Senior HR Business Partner", "Human Resources", Employee.Gender.FEMALE, 8, 55, false},
            {"EMP010", "Suresh", "Raina", "suresh.raina@ems.com", "+91 98200 11011", "Talent Acquisition Specialist", "Human Resources", Employee.Gender.MALE, 9, 10, false},
            {"EMP011", "Arjun", "Mehta", "arjun.mehta@ems.com", "+91 98200 11006", "Financial Controller", "Finance", Employee.Gender.MALE, 8, 50, false},
            {"EMP012", "Deepika", "Sen", "deepika.sen@ems.com", "+91 98200 11013", "Senior Corporate Accountant", "Finance", Employee.Gender.FEMALE, 9, 15, false},
            {"EMP013", "Ananya", "Verma", "ananya.verma@ems.com", "+91 98200 11003", "Brand & Growth Lead", "Marketing", Employee.Gender.FEMALE, 9, 40, true},
            {"EMP014", "Karan", "Kapoor", "karan.kapoor@ems.com", "+91 98200 11014", "Digital Content Strategist", "Marketing", Employee.Gender.MALE, 9, 12, false},
            {"EMP015", "Rajesh", "Khanna", "rajesh.khanna@ems.com", "+91 98200 11015", "Enterprise Sales Lead", "Sales", Employee.Gender.MALE, 8, 45, false},
            {"EMP016", "Meera", "Pillai", "meera.pillai@ems.com", "+91 98200 11016", "Operations & Supply Coordinator", "Operations", Employee.Gender.FEMALE, 9, 0, false}
        };

        LocalDate today = LocalDate.now();
        int currentYear = today.getYear();

        for (Object[] row : additionalData) {
            String empId = (String) row[0];
            String firstName = (String) row[1];
            String lastName = (String) row[2];
            String email = (String) row[3];
            String phone = (String) row[4];
            String designation = (String) row[5];
            String deptName = (String) row[6];
            Employee.Gender gender = (Employee.Gender) row[7];
            int hour = (int) row[8];
            int minute = (int) row[9];
            boolean isLate = (boolean) row[10];

            // 1. Create or Find User
            User user = userRepository.findByEmail(email).orElseGet(() -> {
                User u = new User();
                u.setEmail(email);
                u.setPassword(passwordEncoder.encode("Employee@123"));
                u.setRole(employeeRole);
                u.setIsActive(true);
                u.setFirstLogin(false);
                return userRepository.save(u);
            });

            // 2. Find matching Department
            Department dept = allDepts.stream()
                    .filter(d -> d.getName().equalsIgnoreCase(deptName))
                    .findFirst()
                    .orElse(allDepts.isEmpty() ? null : allDepts.get(0));

            // 3. Create Employee if absent
            Employee employee = employeeRepository.findByEmail(email).orElseGet(() -> {
                Employee e = new Employee();
                e.setEmpId(empId);
                e.setFirstName(firstName);
                e.setLastName(lastName);
                e.setEmail(email);
                e.setPhone(phone);
                e.setDesignation(designation);
                e.setDepartment(dept);
                e.setGender(gender);
                e.setDateOfJoining(LocalDate.of(2024, 2, 1));
                e.setStatus(Employee.EmployeeStatus.ACTIVE);
                e.setEmploymentType(Employee.EmploymentType.FULL_TIME);
                e.setAddress("Cyber City Phase 2, Business Bay");
                e.setUser(user);
                Employee saved = employeeRepository.save(e);
                log.info("Seeded employee: {} ({})", email, empId);
                return saved;
            });

            // 4. Create Leave Balance if absent
            if (leaveBalanceRepository.findByEmployeeAndYear(employee, currentYear).isEmpty()) {
                LeaveBalance lb = LeaveBalance.builder()
                        .employee(employee)
                        .year(currentYear)
                        .casualLeavesRemaining(12)
                        .sickLeavesRemaining(10)
                        .earnedLeavesRemaining(15)
                        .build();
                leaveBalanceRepository.save(lb);
            }

            // 5. Create Today Attendance if absent
            if (attendanceRepository.findByEmployeeAndDate(employee, today).isEmpty()) {
                LocalTime clockIn = LocalTime.of(hour, minute);
                Attendance att = Attendance.builder()
                        .employee(employee)
                        .date(today)
                        .clockInTime(clockIn)
                        .totalHours(null)
                        .status(isLate ? Attendance.AttendanceStatus.LATE : Attendance.AttendanceStatus.PRESENT)
                        .notes(isLate ? "Delayed entry recorded" : "Regular morning shift clock-in")
                        .build();
                attendanceRepository.save(att);
            }
        }
    }

    private void seedPayrollAndCompensation() {
        log.info("Seeding Payroll & Compensation records for FY2026...");
        try {
            payrollService.generateMonthlyPayroll(8, 2026);
            payrollService.generateMonthlyPayroll(9, 2026);
            log.info("Successfully generated monthly payroll for August and September 2026");
        } catch (Exception e) {
            log.error("Error seeding payroll: {}", e.getMessage());
        }
    }

    private void seedPerformanceAndAppraisals() {
        log.info("Seeding Performance Reviews and OKRs for Q3 2026...");
        try {
            performanceService.getOrCreateActiveCycle();
            List<Employee> allEmployees = employeeRepository.findAll();
            for (Employee emp : allEmployees) {
                performanceService.getMyReview(emp.getEmail());
                performanceService.getMyGoals(emp.getEmail());
            }
            log.info("Successfully seeded performance reviews and OKRs for {} employees", allEmployees.size());
        } catch (Exception e) {
            log.error("Error seeding performance: {}", e.getMessage());
        }
    }

    private void seedPhaseFourData() {
        log.info("Seeding Phase 4 Documents and Recruitment records...");
        try {
            List<Employee> allEmployees = employeeRepository.findAll();
            for (Employee emp : allEmployees) {
                documentService.createDefaultDocumentsForEmployee(emp);
            }
            recruitmentService.seedDefaultJobsAndCandidates();
            log.info("Successfully seeded Phase 4 enterprise documents and ATS recruitment pipeline");
        } catch (Exception e) {
            log.error("Error seeding Phase 4 data: {}", e.getMessage());
        }
    }
}

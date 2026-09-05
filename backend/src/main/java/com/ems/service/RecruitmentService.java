package com.ems.service;

import com.ems.dto.CandidateDto;
import com.ems.dto.EmployeeDto;
import com.ems.dto.JobPostingDto;
import com.ems.entity.*;
import com.ems.exception.ResourceNotFoundException;
import com.ems.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecruitmentService {

    private final JobPostingRepository jobPostingRepository;
    private final CandidateRepository candidateRepository;
    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final PasswordEncoder passwordEncoder;
    private final PayrollService payrollService;
    private final DocumentService documentService;

    @Transactional
    public List<JobPostingDto> getAllJobs() {
        List<JobPosting> jobs = jobPostingRepository.findAll();
        if (jobs.isEmpty()) {
            seedDefaultJobsAndCandidates();
            jobs = jobPostingRepository.findAll();
        }

        return jobs.stream().map(this::toJobDto).collect(Collectors.toList());
    }

    @Transactional
    public JobPostingDto createJob(JobPostingDto dto) {
        Department dept = null;
        if (dto.getDepartmentId() != null) {
            dept = departmentRepository.findById(dto.getDepartmentId()).orElse(null);
        }

        JobPosting job = new JobPosting();
        job.setTitle(dto.getTitle());
        job.setDepartment(dept);
        job.setLocation(dto.getLocation() != null ? dto.getLocation() : "Bangalore, India (Hybrid)");
        job.setEmploymentType(Employee.EmploymentType.valueOf(dto.getEmploymentType() != null ? dto.getEmploymentType() : "FULL_TIME"));
        job.setExperienceRequired(dto.getExperienceRequired() != null ? dto.getExperienceRequired() : "3-5 Years");
        job.setSalaryRange(dto.getSalaryRange() != null ? dto.getSalaryRange() : "₹14 - ₹20 LPA");
        job.setDescription(dto.getDescription());
        job.setVacancies(dto.getVacancies() != null ? dto.getVacancies() : 1);
        job.setStatus(JobPosting.JobStatus.ACTIVE);
        job.setPostedDate(LocalDate.now());

        return toJobDto(jobPostingRepository.save(job));
    }

    @Transactional
    public List<CandidateDto> getCandidates(Long jobId, String stage) {
        List<Candidate> candidates;
        if (jobId != null) {
            candidates = candidateRepository.findByJobPostingId(jobId);
        } else if (stage != null && !stage.equalsIgnoreCase("ALL")) {
            candidates = candidateRepository.findByStage(Candidate.CandidateStage.valueOf(stage.toUpperCase()));
        } else {
            candidates = candidateRepository.findAllByOrderByAppliedDateDesc();
        }

        if (candidates.isEmpty() && jobPostingRepository.count() == 0) {
            seedDefaultJobsAndCandidates();
            candidates = candidateRepository.findAllByOrderByAppliedDateDesc();
        }

        return candidates.stream().map(this::toCandidateDto).collect(Collectors.toList());
    }

    @Transactional
    public CandidateDto updateCandidateStage(Long candidateId, String stage, String feedback) {
        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate not found with id: " + candidateId));

        candidate.setStage(Candidate.CandidateStage.valueOf(stage.toUpperCase()));
        if (feedback != null) {
            candidate.setInterviewFeedback(feedback);
        }

        return toCandidateDto(candidateRepository.save(candidate));
    }

    @Transactional
    public EmployeeDto convertCandidateToEmployee(Long candidateId) {
        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate not found with id: " + candidateId));

        if (Boolean.TRUE.equals(candidate.getConvertedToEmployee())) {
            throw new IllegalStateException("Candidate has already been onboarded as an employee.");
        }

        // 1. Create or Find User
        String email = candidate.getEmail();
        User user = userRepository.findByEmail(email).orElseGet(() -> {
            Role empRole = roleRepository.findByName(Role.RoleName.ROLE_EMPLOYEE)
                    .orElseThrow(() -> new RuntimeException("Role ROLE_EMPLOYEE not found"));

            User u = new User();
            u.setEmail(email);
            u.setPassword(passwordEncoder.encode("Employee@123"));
            u.setRole(empRole);
            return userRepository.save(u);
        });

        // 2. Parse First and Last Name
        String[] names = candidate.getFullName().trim().split("\\s+", 2);
        String firstName = names[0];
        String lastName = names.length > 1 ? names[1] : "Staff";

        // 3. Generate Next Emp ID
        long totalEmp = employeeRepository.count();
        String empCode = String.format("EMP%03d", totalEmp + 1);

        // 4. Create Employee Record
        Employee employee = new Employee();
        employee.setEmpId(empCode);
        employee.setFirstName(firstName);
        employee.setLastName(lastName);
        employee.setEmail(email);
        employee.setPhone(candidate.getPhone() != null ? candidate.getPhone() : "+91 98765 00000");
        employee.setGender(Employee.Gender.MALE);
        employee.setDepartment(candidate.getJobPosting().getDepartment());
        employee.setDesignation(candidate.getJobPosting().getTitle());
        employee.setDateOfJoining(LocalDate.now());
        employee.setEmploymentType(candidate.getJobPosting().getEmploymentType());
        employee.setStatus(Employee.EmployeeStatus.ACTIVE);
        employee.setUser(user);
        employee.setAddress("Bangalore Tech Hub, Residency Rd");

        Employee savedEmployee = employeeRepository.save(employee);

        // 5. Initialize Leave Balances
        int year = LocalDate.now().getYear();
        if (leaveBalanceRepository.findByEmployeeAndYear(savedEmployee, year).isEmpty()) {
            LeaveBalance lb = LeaveBalance.builder()
                    .employee(savedEmployee)
                    .year(year)
                    .casualLeavesRemaining(12)
                    .sickLeavesRemaining(10)
                    .earnedLeavesRemaining(15)
                    .build();
            leaveBalanceRepository.save(lb);
        }

        // 6. Initialize Salary Structure & Documents
        payrollService.createDefaultSalaryStructure(savedEmployee);
        documentService.createDefaultDocumentsForEmployee(savedEmployee);

        // 7. Update Candidate
        candidate.setConvertedToEmployee(true);
        candidate.setStage(Candidate.CandidateStage.HIRED);
        candidateRepository.save(candidate);

        log.info("Successfully converted candidate {} to employee {} ({})",
                candidate.getFullName(), savedEmployee.getEmpId(), savedEmployee.getEmail());

        return EmployeeDto.fromEntity(savedEmployee);
    }

    public void seedDefaultJobsAndCandidates() {
        Department itDept = departmentRepository.findByName("Information Technology").orElse(null);
        Department hrDept = departmentRepository.findByName("Human Resources").orElse(null);
        Department finDept = departmentRepository.findByName("Finance").orElse(null);

        JobPosting j1 = new JobPosting(null, "Senior Cloud Solutions Architect", itDept,
                "Bangalore (Hybrid)", Employee.EmploymentType.FULL_TIME, "5-8 Years", "₹22 - ₹30 LPA",
                "Lead cloud infrastructure migration on AWS/GCP and optimize multi-region Kubernetes clusters.",
                2, JobPosting.JobStatus.ACTIVE, LocalDate.of(2026, 8, 15), null, null);

        JobPosting j2 = new JobPosting(null, "Staff Fullstack Engineer (Angular & Spring Boot)", itDept,
                "Bangalore (Hybrid)", Employee.EmploymentType.FULL_TIME, "4-6 Years", "₹18 - ₹24 LPA",
                "Build modern responsive enterprise applications and architect resilient REST APIs.",
                3, JobPosting.JobStatus.ACTIVE, LocalDate.of(2026, 8, 20), null, null);

        JobPosting j3 = new JobPosting(null, "Corporate Talent Acquisition Lead", hrDept,
                "Mumbai (Onsite)", Employee.EmploymentType.FULL_TIME, "4-7 Years", "₹14 - ₹18 LPA",
                "Drive senior technical hiring, campus recruitment, and manage enterprise recruitment pipelines.",
                1, JobPosting.JobStatus.ACTIVE, LocalDate.of(2026, 9, 1), null, null);

        List<JobPosting> savedJobs = jobPostingRepository.saveAll(List.of(j1, j2, j3));

        // Seed Candidates in various stages
        Candidate c1 = new Candidate(null, savedJobs.get(0), "Amitabh Mukherjee", "amitabh.m@gmail.com",
                "+91 98450 11223", "Cognizant Enterprise", 7.0, Candidate.CandidateStage.OFFERED,
                LocalDate.of(2026, 8, 22), "Outstanding architectural depth. Cleared all 3 technical rounds.", 4.8, false, null, null);

        Candidate c2 = new Candidate(null, savedJobs.get(1), "Sneha Kulkarni", "sneha.kulkarni@outlook.com",
                "+91 97312 44556", "Infosys Digital", 4.5, Candidate.CandidateStage.INTERVIEW,
                LocalDate.of(2026, 8, 28), "Strong Spring Boot & Angular knowledge. Final manager round scheduled.", 4.2, false, null, null);

        Candidate c3 = new Candidate(null, savedJobs.get(1), "Vikas Sharma", "vikas.sharma@yahoo.com",
                "+91 91234 88776", "Wipro Cloud", 5.0, Candidate.CandidateStage.HIRED,
                LocalDate.of(2026, 8, 18), "Accepted formal offer letter. Ready for 1-click system onboarding.", 4.9, false, null, null);

        Candidate c4 = new Candidate(null, savedJobs.get(2), "Ritika Sen", "ritika.sen@gmail.com",
                "+91 99887 66554", "TCS HR Services", 5.5, Candidate.CandidateStage.SCREENING,
                LocalDate.of(2026, 9, 2), "Resume shortlisted for HR round.", 4.0, false, null, null);

        candidateRepository.saveAll(List.of(c1, c2, c3, c4));
    }

    private JobPostingDto toJobDto(JobPosting j) {
        long count = candidateRepository.findByJobPostingId(j.getId()).size();
        return new JobPostingDto(
                j.getId(),
                j.getTitle(),
                j.getDepartment() != null ? j.getDepartment().getId() : null,
                j.getDepartment() != null ? j.getDepartment().getName() : "General",
                j.getLocation(),
                j.getEmploymentType() != null ? j.getEmploymentType().name() : "FULL_TIME",
                j.getExperienceRequired(),
                j.getSalaryRange(),
                j.getDescription(),
                j.getVacancies(),
                j.getStatus().name(),
                j.getPostedDate(),
                count
        );
    }

    private CandidateDto toCandidateDto(Candidate c) {
        return new CandidateDto(
                c.getId(),
                c.getJobPosting().getId(),
                c.getJobPosting().getTitle(),
                c.getJobPosting().getDepartment() != null ? c.getJobPosting().getDepartment().getName() : "General",
                c.getFullName(),
                c.getEmail(),
                c.getPhone(),
                c.getCurrentCompany(),
                c.getExperienceYears(),
                c.getStage().name(),
                c.getAppliedDate(),
                c.getInterviewFeedback(),
                c.getRating(),
                c.getConvertedToEmployee()
        );
    }
}

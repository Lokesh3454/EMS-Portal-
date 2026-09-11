package com.ems.service;

import com.ems.dto.AiQueryDto;
import com.ems.dto.AiResponseDto;
import com.ems.dto.LeaveBalanceDto;
import com.ems.dto.PayrollRecordDto;
import com.ems.entity.Employee;
import com.ems.entity.Role;
import com.ems.repository.EmployeeRepository;
import com.ems.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiAssistantService {

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final LeaveService leaveService;
    private final PayrollService payrollService;

    public AiResponseDto processQuery(String userEmail, AiQueryDto queryDto) {
        String q = (queryDto != null && queryDto.getQuery() != null) ? queryDto.getQuery().toLowerCase().trim() : "";
        Employee emp = (userEmail != null) ? employeeRepository.findByEmail(userEmail).orElse(null) : null;

        boolean isPrivilegedUser = false;
        if (userEmail != null) {
            isPrivilegedUser = userRepository.findByEmail(userEmail)
                    .map(u -> u.getRole() != null && (
                            u.getRole().getName() == Role.RoleName.ROLE_ADMIN ||
                            u.getRole().getName() == Role.RoleName.ROLE_HR ||
                            u.getRole().getName() == Role.RoleName.ROLE_MANAGER
                    ))
                    .orElse(false);
        }

        String answer;
        String intent = "GENERAL_HR";
        List<String> actions = new ArrayList<>();
        List<String> suggestions = new ArrayList<>();

        if (q.contains("leave") || q.contains("balance") || q.contains("sick") || q.contains("casual") || q.contains("vacation")) {
            intent = "LEAVE_INQUIRY";
            actions.add("/leaves");
            suggestions.add("How to apply for Casual Leave?");
            suggestions.add("What is the Loss-of-Pay policy?");
            suggestions.add("Show my recent payslip");

            if (emp != null) {
                LeaveBalanceDto bal = leaveService.getLeaveBalance(userEmail, LocalDate.now().getYear());
                answer = String.format("Hello %s! Here is your live 2026 Leave Quota:\n" +
                                "• Casual Leaves (CL): %d remaining\n" +
                                "• Sick Leaves (SL): %d remaining\n" +
                                "• Earned Leaves (EL): %d remaining\n" +
                                "Total available paid days: %d days.\n" +
                                "You can click 'Leave Management' to submit a new leave application.",
                        emp.getFirstName(), bal.getCasualLeavesRemaining(), bal.getSickLeavesRemaining(),
                        bal.getEarnedLeavesRemaining(), bal.getTotalRemaining());
            } else {
                answer = "Employees are entitled to 12 Casual Leaves (CL), 10 Sick Leaves (SL), and 15 Earned Leaves (EL) annually for FY2026. Approved leaves protect 100% of your take-home pay.";
            }

        } else if (q.contains("salary") || q.contains("payroll") || q.contains("payslip") || q.contains("net pay") || q.contains("deduction") || q.contains("pf") || q.contains("tax")) {
            intent = "PAYROLL_INQUIRY";
            actions.add("/payroll");
            suggestions.add("Download my PDF payslip");
            suggestions.add("Explain PF and Professional Tax");
            suggestions.add("What is my current CTC?");

            if (emp != null) {
                List<PayrollRecordDto> slips = payrollService.getMyPayslips(userEmail);
                if (!slips.isEmpty()) {
                    PayrollRecordDto latest = slips.get(0);
                    answer = String.format("Here is your latest pay statement for %s:\n" +
                                    "• Gross Salary: ₹%,.0f\n" +
                                    "• Total Deductions (PF, PT, TDS): ₹%,.0f\n" +
                                    "• Net Take-Home Pay: ₹%,.0f (Disbursed via Direct Bank Deposit)\n" +
                                    "You can view and print your official corporate PDF statement in the Payroll tab.",
                            latest.getMonthName(), latest.getGrossPay(), latest.getTotalDeductions(), latest.getNetPay());
                } else {
                    answer = "Monthly payroll is calculated automatically based on your CTC structure and Attendance shift records. Net salary is disbursed on the 28th of every month.";
                }
            } else {
                answer = "Enterprise salary is structured as: 50% Basic, 20% HRA, 20% Special Allowance, and 10% Conveyance/Medical. Standard deductions include 12% PF, ₹200 Professional Tax, and TDS.";
            }

        } else if (q.contains("performance") || q.contains("appraisal") || q.contains("scorecard") || q.contains("okr") || q.contains("goal") || q.contains("rating")) {
            intent = "PERFORMANCE_INQUIRY";
            actions.add("/performance");
            suggestions.add("How are the 4 competencies weighted?");
            suggestions.add("When does manager review start?");
            suggestions.add("Check my quarterly OKRs");

            answer = "The EMS Performance Management System evaluates 4 core pillars on a 1.0 to 5.0 scale:\n" +
                    "1. Technical Competency & Quality (30% weight)\n" +
                    "2. Delivery & Execution Reliability (30% weight)\n" +
                    "3. Team Collaboration & Communication (20% weight)\n" +
                    "4. Leadership & Proactive Ownership (20% weight)\n" +
                    "Employees complete self-appraisal first, followed by Department Manager reviews and merit revision recommendations (+10% typical).";

        } else if (q.contains("attendance") || q.contains("clock") || q.contains("shift") || q.contains("late") || q.contains("absent") || q.contains("timing")) {
            intent = "ATTENDANCE_INQUIRY";
            actions.add("/attendance");
            suggestions.add("What is the late entry threshold?");
            suggestions.add("How to log clock-out?");
            suggestions.add("Show my attendance history");

            answer = "Standard company shifts are Monday through Friday, 09:00 AM to 06:00 PM (9 hours inclusive of 1 hr lunch). Clock-ins after 09:30 AM are flagged as 'LATE'. Unexcused absences trigger Loss of Pay (LOP) calculations during payroll processing.";

        } else if (q.contains("document") || q.contains("contract") || q.contains("nda") || q.contains("tax form") || q.contains("sign") || q.contains("vault")) {
            intent = "DOCUMENT_INQUIRY";
            actions.add("/documents");
            suggestions.add("Where do I sign Form 12BB?");
            suggestions.add("View my employment agreement");
            suggestions.add("Check company remote work policy");

            answer = "All official corporate records (Appointment Letter, NDA, Form 12BB, Code of Conduct) are securely stored in your Digital Document Vault. You can review and provide legally compliant electronic signatures in 1 click.";

        } else if (q.contains("hiring") || q.contains("recruit") || q.contains("job") || q.contains("candidate") || q.contains("interview")) {
            intent = "RECRUITMENT_INQUIRY";
            if (isPrivilegedUser) {
                actions.add("/recruitment");
                suggestions.add("View open engineering vacancies");
                suggestions.add("Check candidate interview pipeline");
                answer = "Our Recruitment & ATS suite tracks open requisitions across IT, HR, and Finance. Candidates progress through Screening, Technical Interview, Offered, and 1-Click Onboarding directly into the Employee Directory.";
            } else {
                actions.add("/documents");
                suggestions.add("View employee handbook in Document Vault");
                suggestions.add("What is the company probation policy?");
                suggestions.add("How many leaves do I have remaining?");
                answer = "Company job openings and hiring requisitions are managed by Department Managers and the Talent Acquisition HR team.\n" +
                        "• Internal job transfer and vacancy notices are published on the corporate portal.\n" +
                        "• For employee referral programs or role transfer guidelines, please check your Document Vault or contact HR at hr@ems.com.";
            }

        } else if (q.contains("probation") || q.contains("notice") || q.contains("resignation") || q.contains("separation") || q.contains("exit")) {
            intent = "POLICY_INQUIRY";
            actions.add("/documents");
            suggestions.add("What is the Work from Home policy?");
            suggestions.add("Check my remaining leave quota");
            suggestions.add("View official employment agreement");

            answer = "Here is the EMS Enterprise Employment Policy regarding Probation & Notice Period:\n" +
                    "• Probation Period: Standard duration is 3 months from joining date, subject to quarterly performance sign-off.\n" +
                    "• Confirmation: Upon completion, HR issues an official Letter of Confirmation into your Document Vault.\n" +
                    "• Notice Period: 60 days formal notice for permanent employees (30 days during probation).\n" +
                    "• Separation & Handover: Exit formalities include IT asset clearance, leaves encashment, and final settlement within 45 days.\n" +
                    "Your signed Appointment Agreement in the Document Vault contains your specific terms.";

        } else if (q.contains("wfh") || q.contains("remote") || q.contains("work from home") || q.contains("hybrid")) {
            intent = "POLICY_INQUIRY";
            actions.add("/documents");
            suggestions.add("What are standard shift timings?");
            suggestions.add("How to apply for Casual Leave?");
            suggestions.add("View Remote Work Agreement in Vault");

            answer = "Here is the official Enterprise Remote & Hybrid Work Policy:\n" +
                    "• Hybrid Working Model: 3 days in-office and up to 2 days remote per week with manager coordination.\n" +
                    "• Core Working Hours: Available for team syncs between 10:00 AM and 05:00 PM IST.\n" +
                    "• Ergonomic & Connectivity Allowance: Up to ₹1,500/month reimbursement for high-speed home internet.\n" +
                    "• Security Compliance: VPN and two-factor authentication (2FA) are mandatory when working remotely.\n" +
                    "Please ensure you have e-signed the 'Remote Work Policy & NDA' in your Document Vault.";

        } else if (q.contains("insurance") || q.contains("mediclaim") || q.contains("health") || q.contains("benefit") || q.contains("medical")) {
            intent = "BENEFITS_INQUIRY";
            actions.add("/payroll");
            suggestions.add("Show tax deductions on payslip");
            suggestions.add("Check company leave policy");

            answer = "Here are your Enterprise Health & Wellness Benefits:\n" +
                    "• Group Medical Insurance (GMC): ₹5,00,000 family floater coverage for employee, spouse, and up to 2 children.\n" +
                    "• Group Personal Accident (GPA): 3x annual CTC coverage.\n" +
                    "• Cashless Hospitalization: Available across 6,500+ network hospitals via corporate TPA e-card.\n" +
                    "• Maternity & Paternity Leave: 26 weeks paid maternity leave / 2 weeks paid paternity leave.";

        } else if (q.contains("holiday") || q.contains("calendar") || q.contains("festival")) {
            intent = "HOLIDAY_INQUIRY";
            actions.add("/leaves");
            suggestions.add("How many leaves do I have remaining?");
            suggestions.add("Apply for time off");

            answer = "The EMS Enterprise 2026 Holiday Schedule includes:\n" +
                    "• 10 Mandatory Gazetted Holidays (Republic Day, Independence Day, Gandhi Jayanti, Diwali, Christmas, etc.)\n" +
                    "• 2 Optional / Floating Festival Holidays per calendar year.\n" +
                    "• All official holidays are paid days and do not deduct from your Casual or Sick leave balances.";

        } else if (q.contains("policy") || q.contains("guideline") || q.contains("handbook") || q.contains("rule")) {
            intent = "POLICY_INQUIRY";
            actions.add("/documents");
            suggestions.add("What is the probation and notice period?");
            suggestions.add("Check Work from Home policy");
            suggestions.add("View all agreements in Document Vault");

            answer = "Company policies are maintained in your Digital Document Vault:\n" +
                    "• Code of Business Conduct & Ethics\n" +
                    "• Information Security & Non-Disclosure (NDA)\n" +
                    "• Hybrid & Remote Work Guidelines\n" +
                    "• Annual Investment & Tax Declaration (Form 12BB)\n" +
                    "Click 'Document Vault' to review and digitally sign your compliance agreements.";

        } else {
            answer = "Hello! I am your EMS Enterprise Virtual HR Assistant. I can assist you with:\n" +
                    "• Checking your real-time leave balances and applying for time off\n" +
                    "• Explaining your net payslip breakdown and tax deductions\n" +
                    "• Reviewing performance appraisal criteria & tracking quarterly OKRs\n" +
                    "• Accessing your digital document vault & policy e-signatures\n" +
                    "• Attendance shift timings & company operating guidelines.\n\n" +
                    "What would you like to explore today?";
            suggestions.add("What is my current leave balance?");
            suggestions.add("Show my latest monthly payslip");
            suggestions.add("Explain appraisal review competencies");
            suggestions.add("Check company shift timings");
        }

        return new AiResponseDto(answer, intent, actions, suggestions, null);
    }
}

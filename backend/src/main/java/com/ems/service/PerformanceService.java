package com.ems.service;

import com.ems.dto.EmployeeGoalDto;
import com.ems.dto.PerformanceReviewDto;
import com.ems.entity.AppraisalCycle;
import com.ems.entity.Employee;
import com.ems.entity.EmployeeGoal;
import com.ems.entity.PerformanceReview;
import com.ems.exception.ResourceNotFoundException;
import com.ems.repository.AppraisalCycleRepository;
import com.ems.repository.EmployeeGoalRepository;
import com.ems.repository.EmployeeRepository;
import com.ems.repository.PerformanceReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PerformanceService {

    private final AppraisalCycleRepository cycleRepository;
    private final PerformanceReviewRepository reviewRepository;
    private final EmployeeGoalRepository goalRepository;
    private final EmployeeRepository employeeRepository;

    @Transactional
    public AppraisalCycle getOrCreateActiveCycle() {
        return cycleRepository.findFirstByStatus(AppraisalCycle.CycleStatus.ACTIVE)
                .orElseGet(() -> {
                    AppraisalCycle cycle = new AppraisalCycle();
                    cycle.setCycleName("FY2026 Q3 Mid-Year Review");
                    cycle.setPeriod("Q3 2026");
                    cycle.setYear(2026);
                    cycle.setStatus(AppraisalCycle.CycleStatus.ACTIVE);
                    cycle.setStartDate(LocalDate.of(2026, 7, 1));
                    cycle.setEndDate(LocalDate.of(2026, 9, 30));
                    return cycleRepository.save(cycle);
                });
    }

    @Transactional
    public PerformanceReviewDto getMyReview(String email) {
        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with email: " + email));

        AppraisalCycle cycle = getOrCreateActiveCycle();

        PerformanceReview review = reviewRepository.findByCycleIdAndEmployeeId(cycle.getId(), employee.getId())
                .orElseGet(() -> {
                    PerformanceReview r = new PerformanceReview();
                    r.setCycle(cycle);
                    r.setEmployee(employee);
                    r.setSelfScoreTechnical(3.5);
                    r.setSelfScoreDelivery(4.0);
                    r.setSelfScoreCollaboration(4.0);
                    r.setSelfScoreLeadership(3.0);
                    r.setSelfAverageScore(3.6);
                    r.setSelfAchievements("Successfully delivered key features on schedule and ensured enterprise quality standards.");
                    r.setSelfImprovements("Continuing to deepen domain knowledge and lead cross-department architecture initiatives.");
                    r.setStatus(PerformanceReview.ReviewStatus.DRAFT);
                    return reviewRepository.save(r);
                });

        return toReviewDto(review);
    }

    @Transactional
    public PerformanceReviewDto submitSelfReview(String email, PerformanceReviewDto dto) {
        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with email: " + email));

        AppraisalCycle cycle = getOrCreateActiveCycle();

        PerformanceReview review = reviewRepository.findByCycleIdAndEmployeeId(cycle.getId(), employee.getId())
                .orElse(new PerformanceReview());

        review.setCycle(cycle);
        review.setEmployee(employee);

        double tech = dto.getSelfScoreTechnical() != null ? dto.getSelfScoreTechnical() : 3.0;
        double del = dto.getSelfScoreDelivery() != null ? dto.getSelfScoreDelivery() : 3.0;
        double col = dto.getSelfScoreCollaboration() != null ? dto.getSelfScoreCollaboration() : 3.0;
        double ldr = dto.getSelfScoreLeadership() != null ? dto.getSelfScoreLeadership() : 3.0;
        double avg = Math.round(((tech * 0.3) + (del * 0.3) + (col * 0.2) + (ldr * 0.2)) * 10.0) / 10.0;

        review.setSelfScoreTechnical(tech);
        review.setSelfScoreDelivery(del);
        review.setSelfScoreCollaboration(col);
        review.setSelfScoreLeadership(ldr);
        review.setSelfAverageScore(avg);
        review.setSelfAchievements(dto.getSelfAchievements());
        review.setSelfImprovements(dto.getSelfImprovements());
        review.setStatus(PerformanceReview.ReviewStatus.SELF_SUBMITTED);

        return toReviewDto(reviewRepository.save(review));
    }

    @Transactional
    public List<PerformanceReviewDto> getTeamReviews(Long deptId) {
        AppraisalCycle cycle = getOrCreateActiveCycle();
        if (deptId != null) {
            return reviewRepository.findByCycleIdAndEmployeeDepartmentId(cycle.getId(), deptId)
                    .stream()
                    .map(this::toReviewDto)
                    .collect(Collectors.toList());
        }
        return reviewRepository.findByCycleId(cycle.getId())
                .stream()
                .map(this::toReviewDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public PerformanceReviewDto submitManagerReview(Long reviewId, PerformanceReviewDto dto, String managerEmail) {
        PerformanceReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + reviewId));

        Employee manager = employeeRepository.findByEmail(managerEmail).orElse(null);
        if (manager != null) {
            review.setManager(manager);
        }

        double tech = dto.getMgrScoreTechnical() != null ? dto.getMgrScoreTechnical() : 3.0;
        double del = dto.getMgrScoreDelivery() != null ? dto.getMgrScoreDelivery() : 3.0;
        double col = dto.getMgrScoreCollaboration() != null ? dto.getMgrScoreCollaboration() : 3.0;
        double ldr = dto.getMgrScoreLeadership() != null ? dto.getMgrScoreLeadership() : 3.0;
        double mgrAvg = Math.round(((tech * 0.3) + (del * 0.3) + (col * 0.2) + (ldr * 0.2)) * 10.0) / 10.0;

        review.setMgrScoreTechnical(tech);
        review.setMgrScoreDelivery(del);
        review.setMgrScoreCollaboration(col);
        review.setMgrScoreLeadership(ldr);
        review.setMgrAverageScore(mgrAvg);

        double selfAvg = review.getSelfAverageScore() != null ? review.getSelfAverageScore() : mgrAvg;
        double finalRating = Math.round(((mgrAvg * 0.7) + (selfAvg * 0.3)) * 10.0) / 10.0;
        review.setFinalRating(finalRating);

        review.setManagerFeedback(dto.getManagerFeedback());
        review.setRecommendedIncrement(dto.getRecommendedIncrement() != null ? dto.getRecommendedIncrement() : 0.0);
        review.setRecommendedPromotion(dto.getRecommendedPromotion() != null ? dto.getRecommendedPromotion() : false);
        review.setRecommendedDesignation(dto.getRecommendedDesignation());
        review.setStatus(PerformanceReview.ReviewStatus.COMPLETED);

        return toReviewDto(reviewRepository.save(review));
    }

    // Goals (OKRs)
    @Transactional
    public List<EmployeeGoalDto> getMyGoals(String email) {
        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with email: " + email));

        List<EmployeeGoal> goals = goalRepository.findByEmployeeIdOrderByCreatedAtDesc(employee.getId());
        if (goals.isEmpty()) {
            createDefaultGoals(employee);
            goals = goalRepository.findByEmployeeIdOrderByCreatedAtDesc(employee.getId());
        }

        return goals.stream().map(this::toGoalDto).collect(Collectors.toList());
    }

    @Transactional
    public EmployeeGoalDto createGoal(String email, EmployeeGoalDto dto) {
        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with email: " + email));

        EmployeeGoal goal = new EmployeeGoal();
        goal.setEmployee(employee);
        goal.setTitle(dto.getTitle());
        goal.setDescription(dto.getDescription());
        goal.setCategory(dto.getCategory() != null ? dto.getCategory() : "DELIVERY");
        goal.setTargetDate(dto.getTargetDate() != null ? dto.getTargetDate() : LocalDate.of(2026, 9, 30));
        goal.setProgressPercentage(dto.getProgressPercentage() != null ? dto.getProgressPercentage() : 0);
        goal.setStatus(EmployeeGoal.GoalStatus.IN_PROGRESS);

        return toGoalDto(goalRepository.save(goal));
    }

    @Transactional
    public EmployeeGoalDto updateGoalProgress(Long goalId, Integer progress, String status) {
        EmployeeGoal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found with id: " + goalId));

        if (progress != null) {
            goal.setProgressPercentage(Math.max(0, Math.min(100, progress)));
            if (goal.getProgressPercentage() == 100) {
                goal.setStatus(EmployeeGoal.GoalStatus.COMPLETED);
            }
        }
        if (status != null) {
            goal.setStatus(EmployeeGoal.GoalStatus.valueOf(status.toUpperCase()));
        }

        return toGoalDto(goalRepository.save(goal));
    }

    public void createDefaultGoals(Employee emp) {
        EmployeeGoal g1 = new EmployeeGoal(null, emp, "Enterprise Architecture Scalability",
                "Refactor and optimize core modules for sub-second responses.", "TECHNICAL",
                LocalDate.of(2026, 9, 30), 75, EmployeeGoal.GoalStatus.IN_PROGRESS, null, null);

        EmployeeGoal g2 = new EmployeeGoal(null, emp, "Process Adherence & Quality Audits",
                "Ensure 100% test coverage and compliance across sprint deliverables.", "DELIVERY",
                LocalDate.of(2026, 10, 15), 60, EmployeeGoal.GoalStatus.IN_PROGRESS, null, null);

        EmployeeGoal g3 = new EmployeeGoal(null, emp, "Peer Mentorship & Knowledge Sharing",
                "Conduct two internal workshops on modern microservices design.", "LEADERSHIP",
                LocalDate.of(2026, 9, 25), 100, EmployeeGoal.GoalStatus.COMPLETED, null, null);

        goalRepository.saveAll(List.of(g1, g2, g3));
    }

    private PerformanceReviewDto toReviewDto(PerformanceReview r) {
        Employee emp = r.getEmployee();
        Employee mgr = r.getManager();

        return new PerformanceReviewDto(
                r.getId(),
                r.getCycle().getId(),
                r.getCycle().getCycleName(),
                r.getCycle().getPeriod(),
                emp.getId(),
                emp.getEmpId(),
                emp.getFirstName() + " " + emp.getLastName(),
                emp.getDepartment() != null ? emp.getDepartment().getName() : "General",
                emp.getDesignation(),
                emp.getEmail(),
                mgr != null ? mgr.getId() : null,
                mgr != null ? (mgr.getFirstName() + " " + mgr.getLastName()) : "Assigned Manager",
                r.getSelfScoreTechnical(),
                r.getSelfScoreDelivery(),
                r.getSelfScoreCollaboration(),
                r.getSelfScoreLeadership(),
                r.getSelfAverageScore(),
                r.getSelfAchievements(),
                r.getSelfImprovements(),
                r.getMgrScoreTechnical(),
                r.getMgrScoreDelivery(),
                r.getMgrScoreCollaboration(),
                r.getMgrScoreLeadership(),
                r.getMgrAverageScore(),
                r.getFinalRating(),
                r.getManagerFeedback(),
                r.getRecommendedIncrement(),
                r.getRecommendedPromotion(),
                r.getRecommendedDesignation(),
                r.getStatus().name(),
                r.getUpdatedAt()
        );
    }

    private EmployeeGoalDto toGoalDto(EmployeeGoal g) {
        Employee emp = g.getEmployee();
        return new EmployeeGoalDto(
                g.getId(),
                emp.getId(),
                emp.getFirstName() + " " + emp.getLastName(),
                g.getTitle(),
                g.getDescription(),
                g.getCategory(),
                g.getTargetDate(),
                g.getProgressPercentage(),
                g.getStatus().name()
        );
    }
}

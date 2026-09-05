import { Component, OnInit } from '@angular/core';
import { AuthService } from '../../core/services/auth.service';
import { PerformanceService } from '../../core/services/performance.service';
import { AppraisalCycle, PerformanceReview, EmployeeGoal } from '../../core/models/performance.model';

@Component({
  selector: 'app-performance',
  templateUrl: './performance.component.html',
  styleUrls: ['./performance.component.css']
})
export class PerformanceComponent implements OnInit {
  currentRole = '';
  isAdminOrHr = false;
  isManager = false;
  loading = true;

  // Active Tab: 'my-appraisal' | 'radar-matrix' | 'okr-goals'
  activeTab: 'my-appraisal' | 'radar-matrix' | 'okr-goals' = 'my-appraisal';

  // Cycle & Review
  activeCycle: AppraisalCycle | null = null;
  myReview: PerformanceReview | null = null;

  // Form Editing Scores
  editTech = 3.5;
  editDel = 4.0;
  editCol = 4.0;
  editLdr = 3.5;
  achievements = '';
  improvements = '';

  submitting = false;
  successMessage = '';

  // Goals (OKRs)
  goals: EmployeeGoal[] = [];
  showGoalModal = false;
  newGoalTitle = '';
  newGoalDesc = '';
  newGoalCategory: 'TECHNICAL' | 'DELIVERY' | 'LEADERSHIP' | 'INNOVATION' = 'TECHNICAL';
  newGoalDate = '2026-09-30';

  constructor(
    public authService: AuthService,
    private performanceService: PerformanceService
  ) {}

  ngOnInit(): void {
    const role = this.authService.getRole();
    this.currentRole = role;
    this.isAdminOrHr = role === 'ROLE_ADMIN' || role === 'ROLE_HR';
    this.isManager = role === 'ROLE_MANAGER';

    this.loadActiveCycle();
    this.loadMyReview();
    this.loadMyGoals();
  }

  loadActiveCycle(): void {
    this.performanceService.getActiveCycle().subscribe({
      next: (res) => {
        if (res.success && res.data) {
          this.activeCycle = res.data;
        }
      }
    });
  }

  loadMyReview(): void {
    this.performanceService.getMyReview().subscribe({
      next: (res) => {
        if (res.success && res.data) {
          this.myReview = res.data;
          if (this.myReview) {
            this.editTech = this.myReview.selfScoreTechnical || 3.5;
            this.editDel = this.myReview.selfScoreDelivery || 4.0;
            this.editCol = this.myReview.selfScoreCollaboration || 4.0;
            this.editLdr = this.myReview.selfScoreLeadership || 3.0;
            this.achievements = this.myReview.selfAchievements || '';
            this.improvements = this.myReview.selfImprovements || '';
          }
        }
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  loadMyGoals(): void {
    this.performanceService.getMyGoals().subscribe({
      next: (res) => {
        if (res.success && res.data) {
          this.goals = res.data;
        }
      }
    });
  }

  get computedSelfAverage(): number {
    const weighted = (this.editTech * 0.3) + (this.editDel * 0.3) + (this.editCol * 0.2) + (this.editLdr * 0.2);
    return Math.round(weighted * 10) / 10;
  }

  submitSelfAppraisal(): void {
    this.submitting = true;
    const payload: Partial<PerformanceReview> = {
      selfScoreTechnical: this.editTech,
      selfScoreDelivery: this.editDel,
      selfScoreCollaboration: this.editCol,
      selfScoreLeadership: this.editLdr,
      selfAchievements: this.achievements,
      selfImprovements: this.improvements
    };

    this.performanceService.submitSelfReview(payload).subscribe({
      next: (res) => {
        this.submitting = false;
        if (res.success && res.data) {
          this.myReview = res.data;
          this.successMessage = 'Self-Appraisal submitted successfully to your Manager for review!';
          setTimeout(() => this.successMessage = '', 6000);
        }
      },
      error: () => {
        this.submitting = false;
      }
    });
  }

  updateGoalProgress(goal: EmployeeGoal, newProgress: number): void {
    goal.progressPercentage = newProgress;
    this.performanceService.updateGoalProgress(goal.id, newProgress).subscribe({
      next: (res) => {
        if (res.success && res.data) {
          goal.status = res.data.status;
        }
      }
    });
  }

  openGoalModal(): void {
    this.newGoalTitle = '';
    this.newGoalDesc = '';
    this.newGoalCategory = 'TECHNICAL';
    this.showGoalModal = true;
  }

  closeGoalModal(): void {
    this.showGoalModal = false;
  }

  saveNewGoal(): void {
    if (!this.newGoalTitle.trim()) return;

    const payload: Partial<EmployeeGoal> = {
      title: this.newGoalTitle,
      description: this.newGoalDesc,
      category: this.newGoalCategory,
      targetDate: this.newGoalDate,
      progressPercentage: 0
    };

    this.performanceService.createGoal(payload).subscribe({
      next: (res) => {
        if (res.success && res.data) {
          this.goals.unshift(res.data);
          this.closeGoalModal();
        }
      }
    });
  }

  get completedGoalsCount(): number {
    return this.goals.filter(g => g.progressPercentage === 100 || g.status === 'COMPLETED').length;
  }

  get overallGoalsProgress(): number {
    if (this.goals.length === 0) return 0;
    const sum = this.goals.reduce((acc, g) => acc + (g.progressPercentage || 0), 0);
    return Math.round(sum / this.goals.length);
  }
}

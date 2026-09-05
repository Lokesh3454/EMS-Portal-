import { Component, OnInit } from '@angular/core';
import { RecruitmentService } from '../../core/services/recruitment.service';
import { AuthService } from '../../core/services/auth.service';
import { DepartmentService } from '../../core/services/department.service';
import { JobPosting, Candidate } from '../../core/models/recruitment.model';
import { Department } from '../../core/models/employee.model';

@Component({
  selector: 'app-recruitment',
  templateUrl: './recruitment.component.html',
  styleUrls: ['./recruitment.component.css']
})
export class RecruitmentComponent implements OnInit {
  activeView: 'pipeline' | 'jobs' = 'pipeline';
  loading = true;
  actionMessage = '';
  errorMessage = '';

  jobs: JobPosting[] = [];
  candidates: Candidate[] = [];
  departments: Department[] = [];

  // Filters
  selectedJobId: number | null = null;
  selectedStage: string = 'ALL';
  searchQuery = '';

  // Role permissions
  canManage = false;

  // New Job Modal
  showNewJobModal = false;
  newJob: Partial<JobPosting> = {
    title: '',
    departmentId: 1,
    location: 'Bangalore, India (Hybrid)',
    employmentType: 'FULL_TIME',
    experienceRequired: '3-5 years',
    salaryRange: '₹14,00,000 - ₹20,00,000',
    description: '',
    vacancies: 1,
    status: 'ACTIVE'
  };

  // Stage Update / Feedback Modal
  selectedCandidateForAction: Candidate | null = null;
  targetStage = '';
  stageFeedback = '';
  showStageModal = false;

  // Onboarding Loading
  convertingCandidateId: number | null = null;

  constructor(
    private recruitmentService: RecruitmentService,
    private departmentService: DepartmentService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    const role = this.authService.getRole();
    this.canManage = role === 'ROLE_ADMIN' || role === 'ROLE_HR' || role === 'ROLE_MANAGER';
    this.loadData();
    this.loadDepartments();
  }

  loadData(): void {
    this.loading = true;
    this.recruitmentService.getAllJobs().subscribe({
      next: (res) => {
        if (res.success && res.data) {
          this.jobs = res.data;
        }
        this.loadCandidates();
      },
      error: (err) => {
        this.loading = false;
        this.errorMessage = 'Failed to load job postings: ' + (err.error?.message || err.message);
      }
    });
  }

  loadCandidates(): void {
    this.recruitmentService.getCandidates(
      this.selectedJobId || undefined,
      this.selectedStage === 'ALL' ? undefined : this.selectedStage
    ).subscribe({
      next: (res) => {
        this.loading = false;
        if (res.success && res.data) {
          this.candidates = res.data;
        }
      },
      error: (err) => {
        this.loading = false;
        this.errorMessage = 'Failed to load candidates: ' + (err.error?.message || err.message);
      }
    });
  }

  loadDepartments(): void {
    this.departmentService.getAll().subscribe({
      next: (res: any) => {
        if (res.success && res.data) {
          this.departments = res.data;
          if (this.departments.length > 0 && !this.newJob.departmentId) {
            this.newJob.departmentId = this.departments[0].id;
          }
        }
      }
    });
  }

  // Filtered Candidates
  get filteredCandidates(): Candidate[] {
    return this.candidates.filter(c => {
      const matchSearch = !this.searchQuery ||
        c.fullName.toLowerCase().includes(this.searchQuery.toLowerCase()) ||
        c.email.toLowerCase().includes(this.searchQuery.toLowerCase()) ||
        c.jobTitle.toLowerCase().includes(this.searchQuery.toLowerCase()) ||
        c.currentCompany?.toLowerCase().includes(this.searchQuery.toLowerCase());

      const matchJob = !this.selectedJobId || c.jobPostingId === this.selectedJobId;
      const matchStage = this.selectedStage === 'ALL' || c.stage === this.selectedStage;

      return matchSearch && matchJob && matchStage;
    });
  }

  // Stats
  get totalCandidates(): number {
    return this.candidates.length;
  }

  get activeJobsCount(): number {
    return this.jobs.filter(j => j.status === 'ACTIVE').length;
  }

  get interviewCount(): number {
    return this.candidates.filter(c => c.stage === 'INTERVIEW').length;
  }

  get hiredCount(): number {
    return this.candidates.filter(c => c.stage === 'HIRED').length;
  }

  activeKpiFilter: 'ALL' | 'JOBS' | 'INTERVIEW' | 'HIRED' = 'ALL';

  filterByKpi(kpi: 'ALL' | 'JOBS' | 'INTERVIEW' | 'HIRED'): void {
    this.activeKpiFilter = kpi;
    if (kpi === 'JOBS') {
      this.activeView = 'jobs';
    } else {
      this.activeView = 'pipeline';
      if (kpi === 'ALL') {
        this.selectedStage = 'ALL';
      } else if (kpi === 'INTERVIEW') {
        this.selectedStage = 'INTERVIEW';
      } else if (kpi === 'HIRED') {
        this.selectedStage = 'HIRED';
      }
      this.loadCandidates();
    }
  }

  clearFilters(): void {
    this.activeKpiFilter = 'ALL';
    this.selectedStage = 'ALL';
    this.selectedJobId = null;
    this.searchQuery = '';
    this.activeView = 'pipeline';
    this.loadCandidates();
  }

  getCandidateAvatar(stage?: string): string {
    switch (stage) {
      case 'HIRED':
        return 'assets/images/ats-hired.jpg';
      case 'INTERVIEW':
        return 'assets/images/ats-interview.jpg';
      case 'OFFERED':
        return 'assets/images/vault-digitally-signed.jpg';
      default:
        return 'assets/images/ats-candidates.jpg';
    }
  }

  filterByJob(jobId: number | null): void {
    this.selectedJobId = jobId;
    this.loadCandidates();
  }

  filterByStage(stage: string): void {
    this.selectedStage = stage;
    if (stage === 'INTERVIEW') {
      this.activeKpiFilter = 'INTERVIEW';
    } else if (stage === 'HIRED') {
      this.activeKpiFilter = 'HIRED';
    } else if (stage === 'ALL') {
      this.activeKpiFilter = 'ALL';
    }
    this.loadCandidates();
  }

  // Stage Advancement
  promptStageChange(candidate: Candidate, newStage: string): void {
    this.selectedCandidateForAction = candidate;
    this.targetStage = newStage;
    this.stageFeedback = candidate.interviewFeedback || '';
    this.showStageModal = true;
  }

  confirmStageChange(): void {
    if (!this.selectedCandidateForAction) return;

    const candidateId = this.selectedCandidateForAction.id;
    this.recruitmentService.updateCandidateStage(candidateId, this.targetStage, this.stageFeedback).subscribe({
      next: (res) => {
        this.showStageModal = false;
        this.actionMessage = `Candidate ${this.selectedCandidateForAction?.fullName} successfully moved to ${this.targetStage}!`;
        if (res.data) {
          const idx = this.candidates.findIndex(c => c.id === candidateId);
          if (idx !== -1) {
            this.candidates[idx] = res.data;
          }
        }
        setTimeout(() => this.actionMessage = '', 5000);
      },
      error: (err) => {
        this.errorMessage = 'Failed to update candidate stage: ' + (err.error?.message || err.message);
        setTimeout(() => this.errorMessage = '', 5000);
      }
    });
  }

  // 1-Click Convert Candidate to Employee
  convertCandidate(candidate: Candidate): void {
    if (candidate.convertedToEmployee) {
      this.actionMessage = `${candidate.fullName} is already an onboarded employee!`;
      return;
    }

    if (!confirm(`Onboard ${candidate.fullName} directly as an active Employee in the Directory? An official employee profile and corporate credentials will be provisioned automatically.`)) {
      return;
    }

    this.convertingCandidateId = candidate.id;
    this.recruitmentService.convertCandidateToEmployee(candidate.id).subscribe({
      next: (res) => {
        this.convertingCandidateId = null;
        candidate.convertedToEmployee = true;
        this.actionMessage = `🎉 Success! ${candidate.fullName} has been officially onboarded as an Employee! Profile created with ID: ${res.data?.empId || res.data?.id}`;
        setTimeout(() => this.actionMessage = '', 8000);
      },
      error: (err) => {
        this.convertingCandidateId = null;
        this.errorMessage = 'Onboarding failed: ' + (err.error?.message || err.message);
        setTimeout(() => this.errorMessage = '', 6000);
      }
    });
  }

  // New Job Creation
  openNewJob(): void {
    this.newJob = {
      title: '',
      departmentId: this.departments[0]?.id || 1,
      location: 'Bangalore, India (Hybrid)',
      employmentType: 'FULL_TIME',
      experienceRequired: '3-5 years',
      salaryRange: '₹14,00,000 - ₹20,00,000',
      description: '',
      vacancies: 1,
      status: 'ACTIVE'
    };
    this.showNewJobModal = true;
  }

  closeNewJob(): void {
    this.showNewJobModal = false;
  }

  submitNewJob(): void {
    if (!this.newJob.title?.trim()) {
      this.errorMessage = 'Please provide a job title';
      return;
    }

    this.recruitmentService.createJob(this.newJob).subscribe({
      next: (res) => {
        this.showNewJobModal = false;
        this.actionMessage = `Job posting "${res.data?.title}" successfully published!`;
        if (res.data) {
          this.jobs.unshift(res.data);
        }
        setTimeout(() => this.actionMessage = '', 5000);
      },
      error: (err) => {
        this.errorMessage = 'Failed to create job: ' + (err.error?.message || err.message);
        setTimeout(() => this.errorMessage = '', 5000);
      }
    });
  }
}

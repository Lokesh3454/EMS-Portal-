import { Component, OnInit } from '@angular/core';
import { LeaveService, LeaveRequestRecord, LeaveBalance } from '../../core/services/leave.service';
import { AuthService } from '../../core/services/auth.service';
import { ExportService } from '../../core/services/export.service';

@Component({
  selector: 'app-leaves',
  templateUrl: './leaves.component.html',
  styleUrls: ['./leaves.component.css']
})
export class LeavesComponent implements OnInit {
  isAdmin = false;
  activeTab: 'my-leaves' | 'pending-approvals' | 'all-leaves' = 'my-leaves';

  // Interactive Card Selection & Table Filter ('ALL' | 'CASUAL' | 'SICK' | 'EARNED')
  selectedCardCategory: 'ALL' | 'CASUAL' | 'SICK' | 'EARNED' = 'ALL';

  // Leave Balances
  balance: LeaveBalance | null = null;
  loadingBalance = false;

  // Requests
  myRequests: LeaveRequestRecord[] = [];
  pendingRequests: LeaveRequestRecord[] = [];
  allRequests: LeaveRequestRecord[] = [];
  loadingRequests = false;

  // Apply Modal
  showApplyModal = false;
  applyForm = {
    leaveType: 'CASUAL',
    startDate: '',
    endDate: '',
    reason: ''
  };
  calculatedDays = 1;
  submittingLeave = false;
  applyError = '';
  applySuccess = '';

  // Review Action
  reviewRemarks = '';
  selectedRequestForReview: LeaveRequestRecord | null = null;
  showReviewModal = false;
  reviewActionType: 'APPROVED' | 'REJECTED' = 'APPROVED';
  submittingReview = false;

  constructor(
    public authService: AuthService,
    private leaveService: LeaveService,
    private exportService: ExportService
  ) {}

  ngOnInit(): void {
    const role = this.authService.getRole();
    this.isAdmin = role === 'ROLE_ADMIN' || role === 'ROLE_HR' || role === 'ROLE_MANAGER';

    if (this.isAdmin) {
      this.activeTab = 'pending-approvals';
    }

    this.loadBalances();
    this.loadMyLeaves();

    if (this.isAdmin) {
      this.loadPendingLeaves();
      this.loadAllLeaves();
    }
  }

  loadBalances(): void {
    this.loadingBalance = true;
    this.leaveService.getLeaveBalance().subscribe({
      next: (res) => {
        this.loadingBalance = false;
        if (res.success) {
          this.balance = res.data;
        }
      },
      error: () => { this.loadingBalance = false; }
    });
  }

  loadMyLeaves(): void {
    this.loadingRequests = true;
    this.leaveService.getMyLeaves().subscribe({
      next: (res) => {
        this.loadingRequests = false;
        if (res.success) {
          this.myRequests = res.data || [];
        }
      },
      error: () => { this.loadingRequests = false; }
    });
  }

  loadPendingLeaves(): void {
    this.leaveService.getPendingLeaves().subscribe({
      next: (res) => {
        if (res.success) {
          this.pendingRequests = res.data || [];
        }
      }
    });
  }

  loadAllLeaves(): void {
    this.leaveService.getAllLeaves().subscribe({
      next: (res) => {
        if (res.success) {
          this.allRequests = res.data || [];
        }
      }
    });
  }

  // Card Click Interaction
  selectCategory(category: 'ALL' | 'CASUAL' | 'SICK' | 'EARNED'): void {
    if (this.selectedCardCategory === category && category !== 'ALL') {
      this.selectedCardCategory = 'ALL';
    } else {
      this.selectedCardCategory = category;
    }
  }

  // Filtered Lists for Tables
  get filteredMyRequests(): LeaveRequestRecord[] {
    if (this.selectedCardCategory === 'ALL') return this.myRequests;
    return this.myRequests.filter(r => r.leaveType === this.selectedCardCategory);
  }

  get filteredPendingRequests(): LeaveRequestRecord[] {
    if (this.selectedCardCategory === 'ALL') return this.pendingRequests;
    return this.pendingRequests.filter(r => r.leaveType === this.selectedCardCategory);
  }

  get filteredAllRequests(): LeaveRequestRecord[] {
    if (this.selectedCardCategory === 'ALL') return this.allRequests;
    return this.allRequests.filter(r => r.leaveType === this.selectedCardCategory);
  }

  // Quota & Breakdown Helpers for Selected Category
  getCategoryTitle(cat: 'ALL' | 'CASUAL' | 'SICK' | 'EARNED'): string {
    switch (cat) {
      case 'CASUAL': return 'Casual Leave (CL)';
      case 'SICK': return 'Sick Leave (SL)';
      case 'EARNED': return 'Earned Leave (EL)';
      case 'ALL': return 'Total Leave Entitlement';
    }
  }

  getCategoryTag(cat: 'ALL' | 'CASUAL' | 'SICK' | 'EARNED'): string {
    switch (cat) {
      case 'CASUAL': return 'Short-term & Personal';
      case 'SICK': return 'Medical & Wellness';
      case 'EARNED': return 'Vacation & Annual';
      case 'ALL': return 'Annual Quota Overview';
    }
  }

  getCategoryDescription(cat: 'ALL' | 'CASUAL' | 'SICK' | 'EARNED'): string {
    switch (cat) {
      case 'CASUAL': return 'Allocated for unforeseen personal commitments, urgent family tasks, or short personal rest days.';
      case 'SICK': return 'Dedicated time-off for illness, recovery, doctor appointments, or medical treatments.';
      case 'EARNED': return 'Accrued paid time-off for scheduled vacations, holidays, and extended personal time.';
      case 'ALL': return 'Consolidated leave balance summary across all paid categories for the current calendar year.';
    }
  }

  getCategoryPolicy(cat: 'ALL' | 'CASUAL' | 'SICK' | 'EARNED'): string {
    switch (cat) {
      case 'CASUAL': return 'Up to 3 consecutive days. Can be requested on short notice.';
      case 'SICK': return 'Medical certificate mandatory for leaves exceeding 2 consecutive days.';
      case 'EARNED': return 'Recommended to submit at least 7 working days in advance for operational planning.';
      case 'ALL': return 'Standard company leave policy applies. Unused earned leaves may be subject to rollover.';
    }
  }

  getCategoryIcon(cat: 'ALL' | 'CASUAL' | 'SICK' | 'EARNED'): string {
    switch (cat) {
      case 'CASUAL': return 'bi-umbrella-fill text-primary';
      case 'SICK': return 'bi-heart-pulse-fill text-success';
      case 'EARNED': return 'bi-award-fill text-amber';
      case 'ALL': return 'bi-pie-chart-fill text-purple';
    }
  }

  getCategoryAllocated(cat: 'ALL' | 'CASUAL' | 'SICK' | 'EARNED'): number {
    switch (cat) {
      case 'CASUAL': return 12;
      case 'SICK': return 10;
      case 'EARNED': return 15;
      case 'ALL': return 37;
    }
  }

  getCategoryUsed(cat: 'ALL' | 'CASUAL' | 'SICK' | 'EARNED'): number {
    if (!this.balance) return 0;
    switch (cat) {
      case 'CASUAL': return Math.max(0, 12 - this.balance.casualLeavesRemaining);
      case 'SICK': return Math.max(0, 10 - this.balance.sickLeavesRemaining);
      case 'EARNED': return Math.max(0, 15 - this.balance.earnedLeavesRemaining);
      case 'ALL': return Math.max(0, 37 - this.balance.totalRemaining);
    }
  }

  getCategoryRemaining(cat: 'ALL' | 'CASUAL' | 'SICK' | 'EARNED'): number {
    if (!this.balance) return 0;
    switch (cat) {
      case 'CASUAL': return this.balance.casualLeavesRemaining;
      case 'SICK': return this.balance.sickLeavesRemaining;
      case 'EARNED': return this.balance.earnedLeavesRemaining;
      case 'ALL': return this.balance.totalRemaining;
    }
  }

  // Quick Action
  applyForCategory(type: string): void {
    const valid = type === 'ALL' ? 'CASUAL' : type;
    this.openApplyModal(valid);
  }

  openApplyModal(preselectedType?: string): void {
    const today = new Date().toISOString().substring(0, 10);
    this.applyForm = {
      leaveType: preselectedType || 'CASUAL',
      startDate: today,
      endDate: today,
      reason: ''
    };
    this.calculatedDays = 1;
    this.applyError = '';
    this.applySuccess = '';
    this.showApplyModal = true;
  }

  closeApplyModal(): void {
    this.showApplyModal = false;
  }

  onDatesChange(): void {
    if (this.applyForm.startDate && this.applyForm.endDate) {
      const start = new Date(this.applyForm.startDate);
      const end = new Date(this.applyForm.endDate);
      if (end >= start) {
        const diffTime = Math.abs(end.getTime() - start.getTime());
        this.calculatedDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24)) + 1;
      } else {
        this.calculatedDays = 0;
      }
    }
  }

  submitApplication(): void {
    if (!this.applyForm.startDate || !this.applyForm.endDate) {
      this.applyError = 'Please select both start and end dates.';
      return;
    }
    if (this.calculatedDays <= 0) {
      this.applyError = 'End date cannot be earlier than start date.';
      return;
    }
    if (!this.applyForm.reason.trim()) {
      this.applyError = 'Please provide a reason for the leave request.';
      return;
    }

    this.submittingLeave = true;
    this.applyError = '';

    this.leaveService.applyLeave(this.applyForm).subscribe({
      next: (res) => {
        this.submittingLeave = false;
        this.applySuccess = 'Leave request submitted successfully for approval!';
        this.loadBalances();
        this.loadMyLeaves();
        if (this.isAdmin) {
          this.loadPendingLeaves();
          this.loadAllLeaves();
        }
        setTimeout(() => this.closeApplyModal(), 1500);
      },
      error: (err) => {
        this.submittingLeave = false;
        this.applyError = err?.error?.message || 'Failed to submit leave request. Please check your balance.';
      }
    });
  }

  openReviewModal(request: LeaveRequestRecord, action: 'APPROVED' | 'REJECTED'): void {
    this.selectedRequestForReview = request;
    this.reviewActionType = action;
    this.reviewRemarks = action === 'APPROVED' ? 'Approved. Please coordinate handovers.' : '';
    this.showReviewModal = true;
  }

  closeReviewModal(): void {
    this.showReviewModal = false;
    this.selectedRequestForReview = null;
  }

  submitReview(): void {
    if (!this.selectedRequestForReview) return;

    this.submittingReview = true;
    this.leaveService.reviewLeave(this.selectedRequestForReview.id, this.reviewActionType, this.reviewRemarks).subscribe({
      next: () => {
        this.submittingReview = false;
        this.closeReviewModal();
        this.loadPendingLeaves();
        this.loadAllLeaves();
        this.loadMyLeaves();
        this.loadBalances();
      },
      error: () => { this.submittingReview = false; }
    });
  }

  getStatusClass(status: string): string {
    switch (status) {
      case 'APPROVED': return 'status-approved';
      case 'REJECTED': return 'status-rejected';
      case 'PENDING': return 'status-pending';
      default: return 'status-default';
    }
  }

  getLeaveTypeIcon(type: string): string {
    switch (type) {
      case 'CASUAL': return 'bi-umbrella-fill';
      case 'SICK': return 'bi-heart-pulse-fill';
      case 'EARNED': return 'bi-award-fill';
      case 'WFH': return 'bi-house-door-fill';
      default: return 'bi-calendar3';
    }
  }

  exportLeavesCsv(): void {
    let list: LeaveRequestRecord[] = [];
    if (this.isAdmin) {
      if (this.activeTab === 'pending-approvals') list = this.filteredPendingRequests;
      else if (this.activeTab === 'all-leaves') list = this.filteredAllRequests;
      else list = this.filteredMyRequests;
    } else {
      list = this.filteredMyRequests;
    }

    if (!list || list.length === 0) {
      alert('No leave records found in current view to export.');
      return;
    }

    const rows = list.map((r: LeaveRequestRecord) => ({
      'ID': r.id,
      'Employee': r.employeeName || 'Self',
      'Leave Type': r.leaveType,
      'Start Date': r.startDate,
      'End Date': r.endDate,
      'Days': r.totalDays,
      'Status': r.status,
      'Reason': r.reason || '',
      'Approver Remarks': r.reviewRemarks || '',
      'Applied On': r.appliedAt ? new Date(r.appliedAt).toLocaleDateString() : ''
    }));

    const filePrefix = this.isAdmin ? this.activeTab : 'my_leaves';
    this.exportService.exportToCsv(`leaves_${filePrefix}_${new Date().toISOString().slice(0, 10)}.csv`, rows);
  }
}

import { Component, OnInit } from '@angular/core';
import { AuthService } from '../../../core/services/auth.service';
import { EmployeeService } from '../../../core/services/employee.service';
import { DepartmentService } from '../../../core/services/department.service';
import { AttendanceService, AttendanceRecord } from '../../../core/services/attendance.service';
import { LeaveService, LeaveRequestRecord } from '../../../core/services/leave.service';
import { ExportService } from '../../../core/services/export.service';
import { PerformanceService } from '../../../core/services/performance.service';
import { PerformanceReview } from '../../../core/models/performance.model';
import { Employee, Department } from '../../../core/models/employee.model';
import { Router } from '@angular/router';

export interface GroupRoleSlice {
  role: string;
  count: number;
  pct: number;
  color: string;
  dashArray: string;
  dashOffset: number;
}

@Component({
  selector: 'app-manager-portal',
  templateUrl: './manager-portal.component.html',
  styleUrls: ['./manager-portal.component.css']
})
export class ManagerPortalComponent implements OnInit {
  currentRole = '';
  isManager = false;
  isAdminOrHr = false;
  currentManagerName = '';
  loading = true;

  // Department / Group Management
  allDepartments: Department[] = [];
  managedDepartment: Department | null = null;
  selectedDeptId: number = 1;

  // Active Tab
  activeTab: 'team-members' | 'team-attendance' | 'team-leaves' | 'team-appraisals' | 'team-analytics' = 'team-members';

  // Group Members Data
  teamMembers: Employee[] = [];
  searchMember = '';
  statusFilter = 'ALL';

  // Attendance Roster
  teamAttendance: AttendanceRecord[] = [];
  attendanceDate: string = '';

  // Leave Approvals
  teamLeaves: LeaveRequestRecord[] = [];
  leaveFilter: 'ALL' | 'PENDING' | 'APPROVED' | 'REJECTED' = 'ALL';

  // Group KPIs
  totalTeamMembers = 0;
  presentToday = 0;
  onLeaveToday = 0;
  pendingLeavesCount = 0;
  teamAttendanceRate = 0;

  // Modals
  showEditModal = false;
  savingMember = false;
  editEmp: any = {
    id: 0,
    firstName: '',
    lastName: '',
    email: '',
    designation: '',
    phone: '',
    address: '',
    employmentType: 'FULL_TIME',
    status: 'ACTIVE'
  };

  showAddModal = false;
  addingMember = false;
  newEmp: any = {
    firstName: '',
    lastName: '',
    email: '',
    phone: '',
    designation: '',
    employmentType: 'FULL_TIME',
    status: 'ACTIVE',
    address: '',
    dateOfJoining: ''
  };

  showReviewModal = false;
  reviewingLeave = false;
  selectedLeave: LeaveRequestRecord | null = null;
  reviewAction: 'APPROVED' | 'REJECTED' = 'APPROVED';
  reviewRemarks = '';

  showSnapshotModal = false;
  selectedMember: Employee | null = null;

  // SVG Donut metrics
  roleBreakdown: GroupRoleSlice[] = [];
  donutCirc = 2 * Math.PI * 45; // ~282.74

  // Team Appraisals & Performance
  teamReviews: PerformanceReview[] = [];
  selectedAppraisal: PerformanceReview | null = null;
  showAppraisalModal = false;
  mgrTech = 4.0;
  mgrDel = 4.0;
  mgrCol = 4.0;
  mgrLdr = 3.5;
  mgrFeedback = '';
  recommendedIncrement = 10.0;
  recommendedPromotion = false;
  recommendedDesignation = '';
  submittingReview = false;
  reviewSuccessMessage = '';

  constructor(
    public authService: AuthService,
    private employeeService: EmployeeService,
    private departmentService: DepartmentService,
    private attendanceService: AttendanceService,
    private leaveService: LeaveService,
    private performanceService: PerformanceService,
    private exportService: ExportService,
    private router: Router
  ) {}

  ngOnInit(): void {
    const user = this.authService.getCurrentUser();
    this.currentRole = this.authService.getRole();
    this.isManager = this.authService.isManager();
    this.isAdminOrHr = this.authService.isAdmin() || this.authService.isHR();
    this.currentManagerName = user?.fullName || 'Department Manager';
    this.attendanceDate = new Date().toISOString().slice(0, 10);
    this.newEmp.dateOfJoining = this.attendanceDate;

    this.loadInitialContext();
  }

  loadInitialContext(): void {
    this.loading = true;
    this.departmentService.getAll().subscribe({
      next: (res) => {
        this.allDepartments = res.success && res.data ? res.data : [];
        // If Manager, check manager's profile department
        if (this.isManager) {
          this.employeeService.getMyProfile().subscribe({
            next: (profRes) => {
              if (profRes.success && profRes.data && profRes.data.departmentId) {
                this.selectedDeptId = profRes.data.departmentId;
              } else if (this.allDepartments.length > 0) {
                this.selectedDeptId = this.allDepartments[0].id || 1;
              }
              this.updateCurrentDepartment();
              this.loadGroupData();
            },
            error: () => {
              if (this.allDepartments.length > 0) this.selectedDeptId = this.allDepartments[0].id || 1;
              this.updateCurrentDepartment();
              this.loadGroupData();
            }
          });
        } else {
          // Admin / HR default to first department
          if (this.allDepartments.length > 0) {
            this.selectedDeptId = this.allDepartments[0].id || 1;
          }
          this.updateCurrentDepartment();
          this.loadGroupData();
        }
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  updateCurrentDepartment(): void {
    const found = this.allDepartments.find(d => d.id == this.selectedDeptId);
    this.managedDepartment = found || { id: this.selectedDeptId, name: 'IT / Engineering Department' };
  }

  onDepartmentSwitch(): void {
    this.updateCurrentDepartment();
    this.loadGroupData();
  }

  loadGroupData(): void {
    this.loading = true;
    // 1. Fetch Employees for this group
    this.employeeService.getAll({ departmentId: this.selectedDeptId, size: 100 }).subscribe({
      next: (empRes) => {
        const list = (empRes.success && empRes.data && empRes.data.content) ? empRes.data.content : [];
        this.teamMembers = list;
        this.totalTeamMembers = list.length;
        this.calculateRoleAnalytics(list);
        this.loadTeamReviews();

        // 2. Fetch Group Attendance
        this.loadGroupAttendance();

        // 3. Fetch Group Leaves
        this.loadGroupLeaves();

        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  loadGroupAttendance(): void {
    this.attendanceService.getCompanyDailyRoster(this.attendanceDate).subscribe({
      next: (attRes) => {
        const roster: AttendanceRecord[] = attRes.success && attRes.data ? attRes.data : [];
        // Filter attendance records by group members' IDs or department name
        const deptName = this.managedDepartment?.name?.toLowerCase() || '';
        this.teamAttendance = roster.filter(r => {
          if (r.departmentName && deptName && r.departmentName.toLowerCase().includes(deptName.slice(0, 4))) return true;
          return this.teamMembers.some(m => m.id === r.employeeId || m.empId === r.employeeCode);
        });

        // Compute today's group attendance summary
        this.presentToday = this.teamAttendance.filter(r => r.status === 'PRESENT' || r.status === 'LATE').length;
        this.onLeaveToday = this.teamAttendance.filter(r => r.status === 'ON_LEAVE' || r.status === 'HALF_DAY').length;
        this.teamAttendanceRate = this.totalTeamMembers > 0
          ? Math.round((this.presentToday / this.totalTeamMembers) * 100)
          : (this.presentToday > 0 ? 100 : 92);
      }
    });
  }

  loadGroupLeaves(): void {
    this.leaveService.getAllLeaves().subscribe({
      next: (leaveRes) => {
        const allLeaves: LeaveRequestRecord[] = leaveRes.success && leaveRes.data ? leaveRes.data : [];
        const deptName = this.managedDepartment?.name?.toLowerCase() || '';
        this.teamLeaves = allLeaves.filter(l => {
          if (l.departmentName && deptName && l.departmentName.toLowerCase().includes(deptName.slice(0, 4))) return true;
          return this.teamMembers.some(m => m.id === l.employeeId || m.empId === l.employeeCode);
        });

        this.pendingLeavesCount = this.teamLeaves.filter(l => l.status === 'PENDING').length;
      }
    });
  }

  get filteredTeamMembers(): Employee[] {
    return this.teamMembers.filter(m => {
      const matchesSearch = !this.searchMember.trim() ||
        `${m.firstName} ${m.lastName}`.toLowerCase().includes(this.searchMember.toLowerCase()) ||
        (m.empId && m.empId.toLowerCase().includes(this.searchMember.toLowerCase())) ||
        (m.designation && m.designation.toLowerCase().includes(this.searchMember.toLowerCase())) ||
        (m.email && m.email.toLowerCase().includes(this.searchMember.toLowerCase()));

      const matchesStatus = this.statusFilter === 'ALL' || m.status === this.statusFilter;
      return matchesSearch && matchesStatus;
    });
  }

  get filteredTeamLeaves(): LeaveRequestRecord[] {
    if (this.leaveFilter === 'ALL') return this.teamLeaves;
    return this.teamLeaves.filter(l => l.status === this.leaveFilter);
  }

  // Edit Member Modal
  openEditModal(emp: Employee): void {
    this.editEmp = {
      id: emp.id,
      firstName: emp.firstName,
      lastName: emp.lastName,
      email: emp.email,
      designation: emp.designation || '',
      phone: emp.phone || '',
      address: emp.address || '',
      employmentType: emp.employmentType || 'FULL_TIME',
      status: emp.status || 'ACTIVE'
    };
    this.showEditModal = true;
  }

  closeEditModal(): void {
    this.showEditModal = false;
  }

  saveMemberData(): void {
    if (!this.editEmp.id) return;
    this.savingMember = true;
    const payload = {
      ...this.editEmp,
      departmentId: this.selectedDeptId
    };

    this.employeeService.update(this.editEmp.id, payload).subscribe({
      next: () => {
        this.savingMember = false;
        this.closeEditModal();
        this.loadGroupData();
      },
      error: () => {
        this.savingMember = false;
      }
    });
  }

  // Add Team Member Modal
  openAddModal(): void {
    this.newEmp = {
      firstName: '',
      lastName: '',
      email: '',
      phone: '',
      designation: '',
      employmentType: 'FULL_TIME',
      status: 'ACTIVE',
      address: '',
      dateOfJoining: new Date().toISOString().slice(0, 10)
    };
    this.showAddModal = true;
  }

  closeAddModal(): void {
    this.showAddModal = false;
  }

  submitAddMember(): void {
    if (!this.newEmp.firstName || !this.newEmp.lastName || !this.newEmp.email) {
      alert('Please fill in required fields (First Name, Last Name, Email).');
      return;
    }

    this.addingMember = true;
    const payload = {
      ...this.newEmp,
      departmentId: this.selectedDeptId
    };

    this.employeeService.create(payload).subscribe({
      next: () => {
        this.addingMember = false;
        this.closeAddModal();
        this.loadGroupData();
      },
      error: (err) => {
        this.addingMember = false;
        alert(err?.error?.message || 'Failed to add team member.');
      }
    });
  }

  // Snapshot Detail Modal
  openSnapshot(emp: Employee): void {
    this.selectedMember = emp;
    this.showSnapshotModal = true;
  }

  closeSnapshot(): void {
    this.selectedMember = null;
    this.showSnapshotModal = false;
  }

  // Leave Approvals Review
  openReviewModal(leave: LeaveRequestRecord, action: 'APPROVED' | 'REJECTED'): void {
    this.selectedLeave = leave;
    this.reviewAction = action;
    this.reviewRemarks = action === 'APPROVED' ? 'Approved by Group Manager. Please arrange work handover.' : '';
    this.showReviewModal = true;
  }

  closeReviewModal(): void {
    this.showReviewModal = false;
    this.selectedLeave = null;
  }

  submitLeaveReview(): void {
    if (!this.selectedLeave) return;
    this.reviewingLeave = true;
    this.leaveService.reviewLeave(this.selectedLeave.id, this.reviewAction, this.reviewRemarks).subscribe({
      next: () => {
        this.reviewingLeave = false;
        this.closeReviewModal();
        this.loadGroupLeaves();
      },
      error: () => {
        this.reviewingLeave = false;
      }
    });
  }

  // Analytics Computation
  calculateRoleAnalytics(members: Employee[]): void {
    const counts: { [role: string]: number } = {};
    members.forEach(m => {
      const r = m.designation || 'Staff Member';
      counts[r] = (counts[r] || 0) + 1;
    });

    const colors = ['#2563eb', '#10b981', '#f59e0b', '#8b5cf6', '#ec4899', '#06b6d4'];
    const total = members.length || 1;
    let offset = 0;
    const slices: GroupRoleSlice[] = [];

    Object.entries(counts).forEach(([role, count], idx) => {
      const pct = Math.round((count / total) * 100);
      const dashLength = (pct / 100) * this.donutCirc;
      slices.push({
        role,
        count,
        pct,
        color: colors[idx % colors.length],
        dashArray: `${dashLength} ${this.donutCirc}`,
        dashOffset: -offset
      });
      offset += dashLength;
    });

    this.roleBreakdown = slices;
  }

  // CSV Exports
  exportTeamRosterCsv(): void {
    const list = this.filteredTeamMembers;
    if (!list || list.length === 0) {
      alert('No team members to export.');
      return;
    }
    const rows = list.map(m => ({
      'Employee ID': m.empId || 'N/A',
      'Full Name': `${m.firstName} ${m.lastName}`,
      'Email': m.email,
      'Phone': m.phone || 'N/A',
      'Designation': m.designation || 'Specialist',
      'Department': this.managedDepartment?.name || 'Assigned Group',
      'Type': m.employmentType || 'FULL_TIME',
      'Status': m.status || 'ACTIVE',
      'Joined Date': m.dateOfJoining || 'N/A'
    }));

    const deptTag = (this.managedDepartment?.name || 'group').toLowerCase().replace(/\s+/g, '_');
    this.exportService.exportToCsv(`team_roster_${deptTag}_${new Date().toISOString().slice(0, 10)}.csv`, rows);
  }

  exportTeamAttendanceCsv(): void {
    if (!this.teamAttendance || this.teamAttendance.length === 0) {
      alert('No attendance records to export for this date.');
      return;
    }
    const rows = this.teamAttendance.map(r => ({
      'Employee Name': r.employeeName,
      'Employee Code': r.employeeCode,
      'Date': this.attendanceDate,
      'Clock-In': r.clockInTime,
      'Clock-Out': r.clockOutTime || '—',
      'Total Hours': r.totalHours ? `${r.totalHours.toFixed(2)} hrs` : '—',
      'Shift Status': r.status,
      'Notes': r.notes || ''
    }));

    this.exportService.exportToCsv(`group_attendance_${this.attendanceDate}.csv`, rows);
  }

  exportTeamLeavesCsv(): void {
    const list = this.filteredTeamLeaves;
    if (!list || list.length === 0) {
      alert('No team leave records to export.');
      return;
    }
    const rows = list.map(l => ({
      'Request ID': l.id,
      'Employee': l.employeeName,
      'Leave Type': l.leaveType,
      'Start Date': l.startDate,
      'End Date': l.endDate,
      'Days': l.totalDays,
      'Reason': l.reason,
      'Status': l.status,
      'Manager Remarks': l.reviewRemarks || ''
    }));

    this.exportService.exportToCsv(`group_leaves_${this.leaveFilter.toLowerCase()}.csv`, rows);
  }

  loadTeamReviews(): void {
    this.performanceService.getTeamReviews(this.selectedDeptId).subscribe({
      next: (res) => {
        if (res.success && res.data) {
          this.teamReviews = res.data;
        }
      }
    });
  }

  openAppraisalModal(r: PerformanceReview): void {
    this.selectedAppraisal = r;
    this.mgrTech = r.mgrScoreTechnical || r.selfScoreTechnical || 4.0;
    this.mgrDel = r.mgrScoreDelivery || r.selfScoreDelivery || 4.0;
    this.mgrCol = r.mgrScoreCollaboration || r.selfScoreCollaboration || 4.0;
    this.mgrLdr = r.mgrScoreLeadership || r.selfScoreLeadership || 3.5;
    this.mgrFeedback = r.managerFeedback || '';
    this.recommendedIncrement = r.recommendedIncrement || 10.0;
    this.recommendedPromotion = r.recommendedPromotion || false;
    this.recommendedDesignation = r.recommendedDesignation || '';
    this.showAppraisalModal = true;
  }

  closeAppraisalModal(): void {
    this.showAppraisalModal = false;
    this.selectedAppraisal = null;
  }

  submitManagerAppraisal(): void {
    if (!this.selectedAppraisal) return;
    this.submittingReview = true;

    const payload: Partial<PerformanceReview> = {
      mgrScoreTechnical: this.mgrTech,
      mgrScoreDelivery: this.mgrDel,
      mgrScoreCollaboration: this.mgrCol,
      mgrScoreLeadership: this.mgrLdr,
      managerFeedback: this.mgrFeedback,
      recommendedIncrement: this.recommendedIncrement,
      recommendedPromotion: this.recommendedPromotion,
      recommendedDesignation: this.recommendedDesignation
    };

    this.performanceService.submitManagerReview(this.selectedAppraisal.id, payload).subscribe({
      next: (res) => {
        this.submittingReview = false;
        if (res.success && res.data) {
          this.reviewSuccessMessage = `Appraisal for ${this.selectedAppraisal?.employeeName} submitted successfully!`;
          setTimeout(() => this.reviewSuccessMessage = '', 5000);
          this.loadTeamReviews();
          this.closeAppraisalModal();
        }
      },
      error: () => {
        this.submittingReview = false;
      }
    });
  }
}

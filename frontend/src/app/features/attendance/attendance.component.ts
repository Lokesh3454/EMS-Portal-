import { Component, OnInit, OnDestroy } from '@angular/core';
import { AttendanceService, AttendanceRecord, AttendanceSummary } from '../../core/services/attendance.service';
import { AuthService } from '../../core/services/auth.service';
import { ExportService } from '../../core/services/export.service';

@Component({
  selector: 'app-attendance',
  templateUrl: './attendance.component.html',
  styleUrls: ['./attendance.component.css']
})
export class AttendanceComponent implements OnInit, OnDestroy {
  isAdmin = false;
  currentTime = '';
  currentDateStr = '';
  private timerInterval: any;

  // Employee State
  todayRecord: AttendanceRecord | null = null;
  clockNotes = '';
  loadingClockAction = false;
  clockSuccessMsg = '';
  clockErrorMsg = '';

  // Active Elapsed Shift Timer
  elapsedTimeStr = '00:00:00';
  private elapsedInterval: any;

  // Monthly History (Employee)
  monthlyRecords: AttendanceRecord[] = [];
  selectedYear = new Date().getFullYear();
  selectedMonth = new Date().getMonth() + 1;
  loadingMonthly = false;

  // Admin Daily Roster
  companyRoster: AttendanceRecord[] = [];
  adminSummary: AttendanceSummary | null = null;
  adminSelectedDate = new Date().toISOString().substring(0, 10);
  loadingRoster = false;
  rosterSearch = '';

  months = [
    { value: 1, name: 'January' }, { value: 2, name: 'February' },
    { value: 3, name: 'March' }, { value: 4, name: 'April' },
    { value: 5, name: 'May' }, { value: 6, name: 'June' },
    { value: 7, name: 'July' }, { value: 8, name: 'August' },
    { value: 9, name: 'September' }, { value: 10, name: 'October' },
    { value: 11, name: 'November' }, { value: 12, name: 'December' }
  ];

  constructor(
    public authService: AuthService,
    private attendanceService: AttendanceService,
    private exportService: ExportService
  ) {}

  ngOnInit(): void {
    const role = this.authService.getRole();
    this.isAdmin = role === 'ROLE_ADMIN' || role === 'ROLE_HR' || role === 'ROLE_MANAGER';

    this.startDigitalClock();
    this.loadTodayStatus();
    this.loadMonthlyHistory();

    if (this.isAdmin) {
      this.loadCompanyRoster();
      this.loadAdminSummary();
    }
  }

  ngOnDestroy(): void {
    if (this.timerInterval) clearInterval(this.timerInterval);
    if (this.elapsedInterval) clearInterval(this.elapsedInterval);
  }

  startDigitalClock(): void {
    const update = () => {
      const now = new Date();
      this.currentTime = now.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' });
      this.currentDateStr = now.toLocaleDateString([], { weekday: 'long', month: 'short', day: 'numeric', year: 'numeric' });
    };
    update();
    this.timerInterval = setInterval(update, 1000);
  }

  loadTodayStatus(): void {
    this.attendanceService.getTodayStatus().subscribe({
      next: (res) => {
        if (res.success && res.data) {
          this.todayRecord = res.data;
          if (this.todayRecord?.clockInTime && !this.todayRecord?.clockOutTime) {
            this.startElapsedTimer(this.todayRecord.clockInTime);
          } else {
            if (this.elapsedInterval) clearInterval(this.elapsedInterval);
          }
        } else {
          this.todayRecord = null;
        }
      },
      error: () => { this.todayRecord = null; }
    });
  }

  startElapsedTimer(clockInTimeStr: string): void {
    if (this.elapsedInterval) clearInterval(this.elapsedInterval);
    const [hours, minutes, seconds] = clockInTimeStr.split(':').map(Number);
    const startTime = new Date();
    startTime.setHours(hours, minutes, seconds || 0, 0);

    const calc = () => {
      const diffMs = Math.max(0, new Date().getTime() - startTime.getTime());
      const totalSec = Math.floor(diffMs / 1000);
      const h = Math.floor(totalSec / 3600);
      const m = Math.floor((totalSec % 3600) / 60);
      const s = totalSec % 60;
      this.elapsedTimeStr = `${this.pad(h)}:${this.pad(m)}:${this.pad(s)}`;
    };
    calc();
    this.elapsedInterval = setInterval(calc, 1000);
  }

  pad(num: number): string {
    return num < 10 ? '0' + num : num.toString();
  }

  clockIn(): void {
    this.loadingClockAction = true;
    this.clockSuccessMsg = '';
    this.clockErrorMsg = '';

    this.attendanceService.clockIn(this.clockNotes).subscribe({
      next: (res) => {
        this.loadingClockAction = false;
        this.clockSuccessMsg = 'Clocked in successfully! Have a productive shift.';
        this.clockNotes = '';
        this.loadTodayStatus();
        this.loadMonthlyHistory();
        if (this.isAdmin) {
          this.loadCompanyRoster();
          this.loadAdminSummary();
        }
      },
      error: (err) => {
        this.loadingClockAction = false;
        this.clockErrorMsg = err?.error?.message || 'Failed to clock in. Please try again.';
      }
    });
  }

  clockOut(): void {
    this.loadingClockAction = true;
    this.clockSuccessMsg = '';
    this.clockErrorMsg = '';

    this.attendanceService.clockOut(this.clockNotes).subscribe({
      next: (res) => {
        this.loadingClockAction = false;
        this.clockSuccessMsg = 'Clocked out successfully! Great job today.';
        this.clockNotes = '';
        if (this.elapsedInterval) clearInterval(this.elapsedInterval);
        this.loadTodayStatus();
        this.loadMonthlyHistory();
        if (this.isAdmin) {
          this.loadCompanyRoster();
          this.loadAdminSummary();
        }
      },
      error: (err) => {
        this.loadingClockAction = false;
        this.clockErrorMsg = err?.error?.message || 'Failed to clock out. Please try again.';
      }
    });
  }

  loadMonthlyHistory(): void {
    this.loadingMonthly = true;
    this.attendanceService.getMyMonthlyAttendance(this.selectedYear, this.selectedMonth).subscribe({
      next: (res) => {
        this.loadingMonthly = false;
        if (res.success) {
          this.monthlyRecords = res.data || [];
        }
      },
      error: () => { this.loadingMonthly = false; }
    });
  }

  loadCompanyRoster(): void {
    this.loadingRoster = true;
    this.attendanceService.getCompanyDailyRoster(this.adminSelectedDate).subscribe({
      next: (res) => {
        this.loadingRoster = false;
        if (res.success) {
          this.companyRoster = res.data || [];
        }
      },
      error: () => { this.loadingRoster = false; }
    });
  }

  loadAdminSummary(): void {
    this.attendanceService.getAttendanceSummary(this.adminSelectedDate).subscribe({
      next: (res) => {
        if (res.success) {
          this.adminSummary = res.data;
        }
      }
    });
  }

  onDateChange(): void {
    this.loadCompanyRoster();
    this.loadAdminSummary();
  }

  get filteredRoster(): AttendanceRecord[] {
    if (!this.rosterSearch.trim()) return this.companyRoster;
    const term = this.rosterSearch.toLowerCase();
    return this.companyRoster.filter(r =>
      r.employeeName?.toLowerCase().includes(term) ||
      r.employeeCode?.toLowerCase().includes(term) ||
      r.departmentName?.toLowerCase().includes(term) ||
      r.status?.toLowerCase().includes(term)
    );
  }

  getStatusClass(status: string): string {
    switch (status) {
      case 'PRESENT': return 'status-present';
      case 'LATE': return 'status-late';
      case 'HALF_DAY': return 'status-halfday';
      case 'ABSENT': return 'status-absent';
      case 'ON_LEAVE': return 'status-leave';
      default: return 'status-default';
    }
  }

  formatHours(hrs?: number | null): string {
    if (hrs == null) return '—';
    const h = Math.floor(hrs);
    const m = Math.round((hrs - h) * 60);
    return `${h}h ${m}m`;
  }

  exportRosterCsv(): void {
    const data = this.filteredRoster.map(r => ({
      'Employee Code': r.employeeCode,
      'Employee Name': r.employeeName,
      'Department': r.departmentName,
      'Date': r.date,
      'Clock In': r.clockInTime,
      'Clock Out': r.clockOutTime || '—',
      'Total Hours': r.totalHours != null ? r.totalHours : '—',
      'Status': r.status,
      'Notes': r.notes || '—'
    }));
    this.exportService.exportToCsv(`attendance_roster_${this.adminSelectedDate}`, data);
  }

  exportMonthlyCsv(): void {
    const data = this.monthlyRecords.map(r => ({
      'Date': r.date,
      'Clock In': r.clockInTime,
      'Clock Out': r.clockOutTime || '—',
      'Total Hours': r.totalHours != null ? r.totalHours : '—',
      'Status': r.status,
      'Notes': r.notes || '—'
    }));
    this.exportService.exportToCsv(`my_attendance_${this.selectedYear}_${this.selectedMonth}`, data);
  }
}

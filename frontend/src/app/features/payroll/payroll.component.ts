import { Component, OnInit } from '@angular/core';
import { AuthService } from '../../core/services/auth.service';
import { PayrollService } from '../../core/services/payroll.service';
import { ExportService } from '../../core/services/export.service';
import { PayrollRecord, SalaryStructure } from '../../core/models/payroll.model';

@Component({
  selector: 'app-payroll',
  templateUrl: './payroll.component.html',
  styleUrls: ['./payroll.component.css']
})
export class PayrollComponent implements OnInit {
  currentRole = '';
  isAdminOrHr = false;
  loading = true;

  // Active Tab: 'my-payslips' | 'salary-structure' | 'admin-payroll'
  activeTab: 'my-payslips' | 'salary-structure' | 'admin-payroll' = 'my-payslips';

  // Employee Payslips
  myPayslips: PayrollRecord[] = [];
  selectedPayslip: PayrollRecord | null = null;
  myStructure: SalaryStructure | null = null;

  // Admin Payroll Batch & Records
  adminMonth = 9;
  adminYear = 2026;
  adminRecords: PayrollRecord[] = [];
  adminSearch = '';
  generating = false;
  actionMessage = '';

  // Payslip Modal
  showPayslipModal = false;
  modalPayslip: PayrollRecord | null = null;

  constructor(
    public authService: AuthService,
    private payrollService: PayrollService,
    private exportService: ExportService
  ) {}

  ngOnInit(): void {
    const role = this.authService.getRole();
    this.currentRole = role;
    this.isAdminOrHr = role === 'ROLE_ADMIN' || role === 'ROLE_HR';

    this.loadMyPayslips();
    this.loadMyStructure();

    if (this.isAdminOrHr) {
      this.loadAdminPayroll();
    }
  }

  loadMyPayslips(): void {
    this.payrollService.getMyPayslips().subscribe({
      next: (res) => {
        if (res.success && res.data) {
          this.myPayslips = res.data;
          if (this.myPayslips.length > 0) {
            this.selectedPayslip = this.myPayslips[0];
          }
        }
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  loadMyStructure(): void {
    this.payrollService.getMySalaryStructure().subscribe({
      next: (res) => {
        if (res.success && res.data) {
          this.myStructure = res.data;
        }
      }
    });
  }

  loadAdminPayroll(): void {
    this.payrollService.getPayslipsByPeriod(this.adminMonth, this.adminYear).subscribe({
      next: (res) => {
        if (res.success && res.data) {
          this.adminRecords = res.data;
        }
      }
    });
  }

  selectPayslip(p: PayrollRecord): void {
    this.selectedPayslip = p;
  }

  runBatchPayroll(): void {
    this.generating = true;
    this.payrollService.generateMonthlyPayroll(this.adminMonth, this.adminYear).subscribe({
      next: (res) => {
        this.generating = false;
        if (res.success && res.data) {
          this.adminRecords = res.data;
          this.actionMessage = `Batch payroll for ${this.getMonthName(this.adminMonth)} ${this.adminYear} generated successfully! (${res.data.length} employees processed)`;
          setTimeout(() => this.actionMessage = '', 6000);
          this.loadMyPayslips();
        }
      },
      error: (err) => {
        this.generating = false;
        this.actionMessage = 'Failed to generate payroll: ' + (err.error?.message || 'Server error');
      }
    });
  }

  openPayslipModal(p: PayrollRecord): void {
    this.modalPayslip = p;
    this.showPayslipModal = true;
  }

  closePayslipModal(): void {
    this.showPayslipModal = false;
    this.modalPayslip = null;
  }

  printPayslip(): void {
    window.print();
  }

  exportBankCSV(): void {
    const data = this.filteredAdminRecords.map(r => ({
      'Employee Code': r.empId,
      'Full Name': r.employeeName,
      'Department': r.departmentName,
      'Bank Name': r.bankName || 'HDFC Bank',
      'Account Number': r.accountNumber || 'N/A',
      'PAN': r.panNumber || 'N/A',
      'Gross Pay': r.grossPay,
      'Total Deductions': r.totalDeductions,
      'Net Pay': r.netPay,
      'Payment Status': r.status,
      'Transaction Ref': r.transactionReference || 'PENDING',
      'Disbursal Date': r.disbursalDate || 'N/A'
    }));
    this.exportService.exportToCsv(`EMS_Payroll_Disbursement_${this.adminMonth}_${this.adminYear}`, data);
  }

  get filteredAdminRecords(): PayrollRecord[] {
    if (!this.adminSearch.trim()) return this.adminRecords;
    const q = this.adminSearch.toLowerCase();
    return this.adminRecords.filter(r =>
      r.employeeName.toLowerCase().includes(q) ||
      r.empId.toLowerCase().includes(q) ||
      r.departmentName.toLowerCase().includes(q) ||
      r.designation.toLowerCase().includes(q)
    );
  }

  get totalAdminGross(): number {
    return this.adminRecords.reduce((acc, r) => acc + (r.grossPay || 0), 0);
  }

  get totalAdminDeductions(): number {
    return this.adminRecords.reduce((acc, r) => acc + (r.totalDeductions || 0), 0);
  }

  get totalAdminNet(): number {
    return this.adminRecords.reduce((acc, r) => acc + (r.netPay || 0), 0);
  }

  getMonthName(month: number): string {
    const months = ['January','February','March','April','May','June','July','August','September','October','November','December'];
    return months[month - 1] || '';
  }

  numberToWords(num: number): string {
    if (!num) return 'Zero';
    const a = ['','One ','Two ','Three ','Four ','Five ','Six ','Seven ','Eight ','Nine ','Ten ','Eleven ','Twelve ','Thirteen ','Fourteen ','Fifteen ','Sixteen ','Seventeen ','Eighteen ','Nineteen '];
    const b = ['', '', 'Twenty','Thirty','Forty','Fifty','Sixty','Seventy','Eighty','Ninety'];

    const numStr = Math.round(num).toString();
    if (numStr.length > 9) return 'Overflow';
    const n = ('000000000' + numStr).substr(-9).match(/^(\d{2})(\d{2})(\d{2})(\d{1})(\d{2})$/);
    if (!n) return '';
    let str = '';
    str += (Number(n[1]) != 0) ? (a[Number(n[1])] || b[Number(n[1][0])] + ' ' + a[Number(n[1][1])]) + 'Crore ' : '';
    str += (Number(n[2]) != 0) ? (a[Number(n[2])] || b[Number(n[2][0])] + ' ' + a[Number(n[2][1])]) + 'Lakh ' : '';
    str += (Number(n[3]) != 0) ? (a[Number(n[3])] || b[Number(n[3][0])] + ' ' + a[Number(n[3][1])]) + 'Thousand ' : '';
    str += (Number(n[4]) != 0) ? (a[Number(n[4])] || b[Number(n[4][0])] + ' ' + a[Number(n[4][1])]) + 'Hundred ' : '';
    str += (Number(n[5]) != 0) ? ((str != '') ? 'and ' : '') + (a[Number(n[5])] || b[Number(n[5][0])] + ' ' + a[Number(n[5][1])]) + 'Rupees Only' : 'Rupees Only';
    return str;
  }
}

export interface SalaryStructure {
  id?: number;
  employeeId: number;
  empId?: string;
  employeeName?: string;
  departmentName?: string;
  designation?: string;
  annualCtc: number;
  monthlyGross: number;
  basicPay: number;
  hra: number;
  specialAllowance: number;
  conveyanceAllowance?: number;
  medicalAllowance?: number;
  pfEmployee?: number;
  professionalTax?: number;
  incomeTaxTds?: number;
  bankName?: string;
  accountNumber?: string;
  ifscCode?: string;
  panNumber?: string;
  effectiveDate?: string;
}

export interface PayrollRecord {
  id: number;
  employeeId: number;
  empId: string;
  employeeName: string;
  departmentName: string;
  designation: string;
  email: string;
  payMonth: number;
  payYear: number;
  monthName: string;
  totalDaysInMonth: number;
  daysWorked: number;
  paidLeaves: number;
  lossOfPayDays: number;
  basicPay: number;
  hra: number;
  specialAllowance: number;
  otherAllowances: number;
  grossPay: number;
  pfDeduction: number;
  professionalTax: number;
  tdsDeduction: number;
  lossOfPayDeduction: number;
  totalDeductions: number;
  netPay: number;
  status: 'DRAFT' | 'PROCESSED' | 'PAID' | 'ON_HOLD';
  disbursalDate?: string;
  paymentMode?: string;
  transactionReference?: string;
  bankName?: string;
  accountNumber?: string;
  panNumber?: string;
  generatedAt?: string;
}

import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { PayrollRecord, SalaryStructure } from '../models/payroll.model';

@Injectable({
  providedIn: 'root'
})
export class PayrollService {
  private apiUrl = `${environment.apiUrl}/payroll`;

  constructor(private http: HttpClient) {}

  getMyPayslips(): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/my-payslips`);
  }

  getMySalaryStructure(): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/my-structure`);
  }

  getSalaryStructure(employeeId: number): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/structure/${employeeId}`);
  }

  saveSalaryStructure(dto: SalaryStructure): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/structure`, dto);
  }

  getPayslipsByPeriod(month: number, year: number): Observable<any> {
    const params = new HttpParams().set('month', month.toString()).set('year', year.toString());
    return this.http.get<any>(`${this.apiUrl}/period`, { params });
  }

  getDepartmentPayslips(deptId: number, month: number, year: number): Observable<any> {
    const params = new HttpParams().set('month', month.toString()).set('year', year.toString());
    return this.http.get<any>(`${this.apiUrl}/department/${deptId}`, { params });
  }

  getPayslipById(id: number): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/${id}`);
  }

  generateMonthlyPayroll(month: number, year: number): Observable<any> {
    const params = new HttpParams().set('month', month.toString()).set('year', year.toString());
    return this.http.post<any>(`${this.apiUrl}/generate`, {}, { params });
  }

  updatePayrollStatus(id: number, status: string): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/${id}/status`, { status });
  }
}

import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface LeaveRequestRecord {
  id: number;
  employeeId: number;
  employeeName: string;
  employeeCode: string;
  departmentName: string;
  leaveType: 'CASUAL' | 'SICK' | 'EARNED' | 'WFH';
  startDate: string;
  endDate: string;
  totalDays: number;
  reason: string;
  status: 'PENDING' | 'APPROVED' | 'REJECTED';
  appliedAt: string;
  reviewedBy?: string;
  reviewedAt?: string;
  reviewRemarks?: string;
}

export interface LeaveBalance {
  year: number;
  casualLeavesRemaining: number;
  sickLeavesRemaining: number;
  earnedLeavesRemaining: number;
  totalRemaining: number;
}

@Injectable({
  providedIn: 'root'
})
export class LeaveService {
  private apiUrl = `${environment.apiUrl}/leaves`;

  constructor(private http: HttpClient) {}

  applyLeave(data: { leaveType: string; startDate: string; endDate: string; reason: string }): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/apply`, data);
  }

  getMyLeaves(): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/my-requests`);
  }

  getLeaveBalance(year?: number): Observable<any> {
    let params = new HttpParams();
    if (year) params = params.set('year', year.toString());
    return this.http.get<any>(`${this.apiUrl}/balance`, { params });
  }

  getPendingLeaves(): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/pending`);
  }

  getAllLeaves(): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/all`);
  }

  reviewLeave(id: number, status: 'APPROVED' | 'REJECTED', remarks?: string): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/${id}/review`, { status, remarks });
  }

  getPendingCount(): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/pending-count`);
  }
}

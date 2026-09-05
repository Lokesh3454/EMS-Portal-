import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface AttendanceRecord {
  id: number;
  employeeId: number;
  employeeName: string;
  employeeCode: string;
  departmentName: string;
  date: string;
  clockInTime: string;
  clockOutTime?: string;
  totalHours?: number;
  status: 'PRESENT' | 'LATE' | 'HALF_DAY' | 'ABSENT' | 'ON_LEAVE';
  notes?: string;
}

export interface AttendanceSummary {
  totalEmployees: number;
  presentToday: number;
  lateToday: number;
  onLeaveToday: number;
  absentToday: number;
  attendanceRate: number;
}

@Injectable({
  providedIn: 'root'
})
export class AttendanceService {
  private apiUrl = `${environment.apiUrl}/attendance`;

  constructor(private http: HttpClient) {}

  clockIn(notes?: string): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/clock-in`, { notes });
  }

  clockOut(notes?: string): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/clock-out`, { notes });
  }

  getTodayStatus(): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/today`);
  }

  getMyMonthlyAttendance(year?: number, month?: number): Observable<any> {
    let params = new HttpParams();
    if (year) params = params.set('year', year.toString());
    if (month) params = params.set('month', month.toString());
    return this.http.get<any>(`${this.apiUrl}/my-monthly`, { params });
  }

  getCompanyDailyRoster(date?: string): Observable<any> {
    let params = new HttpParams();
    if (date) params = params.set('date', date);
    return this.http.get<any>(`${this.apiUrl}/company-daily`, { params });
  }

  getAttendanceSummary(date?: string): Observable<any> {
    let params = new HttpParams();
    if (date) params = params.set('date', date);
    return this.http.get<any>(`${this.apiUrl}/summary`, { params });
  }
}

import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AppraisalCycle, PerformanceReview, EmployeeGoal } from '../models/performance.model';

@Injectable({
  providedIn: 'root'
})
export class PerformanceService {
  private apiUrl = `${environment.apiUrl}/performance`;

  constructor(private http: HttpClient) {}

  getActiveCycle(): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/cycle/active`);
  }

  getMyReview(): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/my-review`);
  }

  submitSelfReview(review: Partial<PerformanceReview>): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/my-review/submit`, review);
  }

  getTeamReviews(deptId?: number): Observable<any> {
    let params = new HttpParams();
    if (deptId) {
      params = params.set('deptId', deptId.toString());
    }
    return this.http.get<any>(`${this.apiUrl}/team-reviews`, { params });
  }

  submitManagerReview(reviewId: number, review: Partial<PerformanceReview>): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/manager-review/${reviewId}`, review);
  }

  getMyGoals(): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/my-goals`);
  }

  createGoal(goal: Partial<EmployeeGoal>): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/goals`, goal);
  }

  updateGoalProgress(goalId: number, progress: number, status?: string): Observable<any> {
    const payload: any = { progress };
    if (status) payload.status = status;
    return this.http.put<any>(`${this.apiUrl}/goals/${goalId}/progress`, payload);
  }
}

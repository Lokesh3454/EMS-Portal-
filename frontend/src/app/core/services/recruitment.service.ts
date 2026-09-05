import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { JobPosting, Candidate } from '../models/recruitment.model';

@Injectable({
  providedIn: 'root'
})
export class RecruitmentService {
  private apiUrl = `${environment.apiUrl}/recruitment`;

  constructor(private http: HttpClient) {}

  getAllJobs(): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/jobs`);
  }

  createJob(job: Partial<JobPosting>): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/jobs`, job);
  }

  getCandidates(jobId?: number, stage?: string): Observable<any> {
    let params = new HttpParams();
    if (jobId) {
      params = params.set('jobId', jobId.toString());
    }
    if (stage && stage !== 'ALL') {
      params = params.set('stage', stage);
    }
    return this.http.get<any>(`${this.apiUrl}/candidates`, { params });
  }

  updateCandidateStage(candidateId: number, stage: string, feedback?: string): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/candidates/${candidateId}/stage`, { stage, feedback });
  }

  convertCandidateToEmployee(candidateId: number): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/candidates/${candidateId}/convert`, {});
  }
}

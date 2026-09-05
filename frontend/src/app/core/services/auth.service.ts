import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { Router } from '@angular/router';
import { environment } from '../../../environments/environment';
import { ApiResponse, LoginRequest, LoginResponse } from '../models/auth.model';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private apiUrl = environment.apiUrl;
  private currentUserSubject = new BehaviorSubject<LoginResponse | null>(this.getUserFromStorage());
  currentUser$ = this.currentUserSubject.asObservable();

  constructor(private http: HttpClient, private router: Router) {}

  login(credentials: LoginRequest): Observable<ApiResponse<LoginResponse>> {
    return this.http.post<ApiResponse<LoginResponse>>(`${this.apiUrl}/auth/login`, credentials).pipe(
      tap(response => {
        if (response.success && response.data) {
          localStorage.setItem('ems_token', response.data.token);
          localStorage.setItem('ems_user', JSON.stringify(response.data));
          this.currentUserSubject.next(response.data);
        }
      })
    );
  }

  logout(): void {
    localStorage.removeItem('ems_token');
    localStorage.removeItem('ems_user');
    this.currentUserSubject.next(null);
    this.router.navigate(['/login']);
  }

  getToken(): string | null {
    return localStorage.getItem('ems_token');
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }

  getCurrentUser(): LoginResponse | null {
    return this.currentUserSubject.value;
  }

  getRole(): string {
    return this.getCurrentUser()?.role || '';
  }

  isAdmin(): boolean { return this.getRole() === 'ROLE_ADMIN'; }
  isHR(): boolean { return this.getRole() === 'ROLE_HR'; }
  isManager(): boolean { return this.getRole() === 'ROLE_MANAGER'; }
  isEmployee(): boolean { return this.getRole() === 'ROLE_EMPLOYEE'; }
  canManageEmployees(): boolean { return this.isAdmin() || this.isHR(); }
  canAccessManagerPortal(): boolean { return this.isAdmin() || this.isHR() || this.isManager(); }

  changePassword(data: { currentPassword: string; newPassword: string }): Observable<ApiResponse<any>> {
    return this.http.post<ApiResponse<any>>(`${this.apiUrl}/auth/change-password`, data);
  }

  private getUserFromStorage(): LoginResponse | null {
    const user = localStorage.getItem('ems_user');
    return user ? JSON.parse(user) : null;
  }
}

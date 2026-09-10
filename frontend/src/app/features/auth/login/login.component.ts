import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent {
  email = '';
  password = '';
  loading = false;
  error = '';
  showPassword = false;

  constructor(private authService: AuthService, private router: Router) {
    if (this.authService.isLoggedIn()) this.router.navigate(['/dashboard']);
  }

  onSubmit(): void {
    if (!this.email || !this.password) { this.error = 'Please enter email and password.'; return; }
    this.loading = true; this.error = '';
    this.authService.login({ email: this.email, password: this.password }).subscribe({
      next: (res) => { if (res.success) this.router.navigate(['/dashboard']); },
      error: (err) => {
        this.loading = false;
        if (err.status === 0) {
          this.error = 'Unable to connect to the backend server. Please check your backend URL and ensure the service is running.';
        } else if (err.status === 401 || err.status === 403) {
          this.error = err?.error?.message || 'Invalid email or password. Please try again.';
        } else if (err.status >= 500) {
          this.error = err?.error?.message || 'Server error occurred. Please try again later.';
        } else {
          this.error = err?.error?.message || 'An unexpected error occurred. Please try again.';
        }
      },
      complete: () => { this.loading = false; }
    });
  }

  togglePassword(): void { this.showPassword = !this.showPassword; }
}

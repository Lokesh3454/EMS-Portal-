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
        this.error = err?.error?.message || 'Invalid email or password. Please try again.';
      },
      complete: () => { this.loading = false; }
    });
  }

  togglePassword(): void { this.showPassword = !this.showPassword; }
}

import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { EmployeeService } from '../../../core/services/employee.service';
import { AuthService } from '../../../core/services/auth.service';
import { Employee } from '../../../core/models/employee.model';

@Component({
  selector: 'app-employee-detail',
  templateUrl: './employee-detail.component.html',
  styleUrls: ['./employee-detail.component.css']
})
export class EmployeeDetailComponent implements OnInit {
  employee: Employee | null = null;
  loading = true;
  errorMessage = '';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private employeeService: EmployeeService,
    public authService: AuthService
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.loadEmployee(+id);
    } else {
      this.router.navigate(['/employees']);
    }
  }

  loadEmployee(id: number): void {
    this.loading = true;
    this.employeeService.getById(id).subscribe({
      next: (res) => {
        this.loading = false;
        if (res.success && res.data) {
          this.employee = res.data;
        } else {
          this.errorMessage = res.message || 'Employee not found';
        }
      },
      error: () => {
        this.loading = false;
        this.errorMessage = 'Failed to load employee details.';
      }
    });
  }

  getInitials(): string {
    if (!this.employee) return '';
    return `${this.employee.firstName?.charAt(0) || ''}${this.employee.lastName?.charAt(0) || ''}`.toUpperCase();
  }

  goBack(): void {
    this.router.navigate(['/employees']);
  }

  edit(): void {
    if (this.employee?.id) {
      this.router.navigate(['/employees', this.employee.id, 'edit']);
    }
  }
}

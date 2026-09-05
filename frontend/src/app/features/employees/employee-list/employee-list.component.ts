import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { EmployeeService } from '../../../core/services/employee.service';
import { DepartmentService } from '../../../core/services/department.service';
import { AuthService } from '../../../core/services/auth.service';
import { Employee, Department } from '../../../core/models/employee.model';

@Component({
  selector: 'app-employee-list',
  templateUrl: './employee-list.component.html',
  styleUrls: ['./employee-list.component.css']
})
export class EmployeeListComponent implements OnInit {
  employees: Employee[] = [];
  departments: Department[] = [];
  loading = true;
  deleteLoading = false;
  showDeleteModal = false;
  employeeToDelete: Employee | null = null;

  filters = { search: '', departmentId: '', status: '', page: 0, size: 10, sortBy: 'firstName', sortDir: 'asc' };
  totalElements = 0; totalPages = 0; currentPage = 0;
  pages: number[] = [];

  constructor(
    private employeeService: EmployeeService,
    private deptService: DepartmentService,
    public authService: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.loadDepartments();
    this.route.queryParams.subscribe(params => {
      this.filters.status = params['status'] || '';
      this.filters.page = 0;
      this.loadEmployees();
    });
  }

  loadDepartments(): void {
    this.deptService.getAll().subscribe(res => { if (res.success) this.departments = res.data; });
  }

  loadEmployees(): void {
    this.loading = true;
    this.employeeService.getAll(this.filters).subscribe({
      next: (res) => {
        if (res.success) {
          this.employees = res.data.content;
          this.totalElements = res.data.totalElements;
          this.totalPages = res.data.totalPages;
          this.currentPage = res.data.number;
          this.pages = Array.from({ length: this.totalPages }, (_, i) => i);
        }
        this.loading = false;
      },
      error: () => { this.loading = false; }
    });
  }

  onSearch(): void { this.filters.page = 0; this.loadEmployees(); }
  onPageChange(page: number): void { this.filters.page = page; this.loadEmployees(); }
  onPageSizeChange(): void { this.filters.page = 0; this.loadEmployees(); }

  viewEmployee(id: number): void { this.router.navigate(['/employees', id]); }
  editEmployee(id: number): void { this.router.navigate(['/employees', id, 'edit']); }
  confirmDelete(employee: Employee): void { this.employeeToDelete = employee; this.showDeleteModal = true; }
  cancelDelete(): void { this.showDeleteModal = false; this.employeeToDelete = null; }

  deleteEmployee(): void {
    if (!this.employeeToDelete?.id) return;
    this.deleteLoading = true;
    this.employeeService.delete(this.employeeToDelete.id).subscribe({
      next: () => { this.showDeleteModal = false; this.employeeToDelete = null; this.deleteLoading = false; this.loadEmployees(); },
      error: () => { this.deleteLoading = false; }
    });
  }

  getInitials(emp: Employee): string {
    return `${emp.firstName?.charAt(0) || ''}${emp.lastName?.charAt(0) || ''}`.toUpperCase();
  }

  getStatusClass(status: string): string {
    return status === 'ACTIVE' ? 'badge-active' : 'badge-inactive';
  }

  canManage(): boolean { return this.authService.canManageEmployees(); }
}

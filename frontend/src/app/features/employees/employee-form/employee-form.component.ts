import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { EmployeeService } from '../../../core/services/employee.service';
import { DepartmentService } from '../../../core/services/department.service';
import { Department } from '../../../core/models/employee.model';

@Component({
  selector: 'app-employee-form',
  templateUrl: './employee-form.component.html',
  styleUrls: ['./employee-form.component.css']
})
export class EmployeeFormComponent implements OnInit {
  employeeForm!: FormGroup;
  isEditMode = false;
  employeeId: number | null = null;
  departments: Department[] = [];
  loading = false;
  submitting = false;
  errorMessage = '';

  constructor(
    private fb: FormBuilder,
    private employeeService: EmployeeService,
    private departmentService: DepartmentService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.initForm();
    this.loadDepartments();

    this.route.paramMap.subscribe(params => {
      const id = params.get('id');
      if (id) {
        this.isEditMode = true;
        this.employeeId = +id;
        this.loadEmployee(this.employeeId);
      }
    });
  }

  private initForm(): void {
    this.employeeForm = this.fb.group({
      firstName: ['', [Validators.required, Validators.minLength(2)]],
      lastName: ['', [Validators.required, Validators.minLength(2)]],
      email: ['', [Validators.required, Validators.email]],
      empId: ['', [Validators.required]],
      phone: ['', [Validators.pattern('^[0-9+ -]{7,15}$')]],
      departmentId: [null, [Validators.required]],
      designation: ['', [Validators.required]],
      salary: [null, [Validators.min(0)]],
      hireDate: [new Date().toISOString().substring(0, 10), [Validators.required]],
      status: ['ACTIVE', [Validators.required]],
      address: ['']
    });
  }

  private loadDepartments(): void {
    this.departmentService.getAll().subscribe({
      next: (res) => {
        if (res.success) {
          this.departments = res.data;
        }
      }
    });
  }

  private loadEmployee(id: number): void {
    this.loading = true;
    this.employeeService.getById(id).subscribe({
      next: (res) => {
        this.loading = false;
        if (res.success && res.data) {
          const emp = res.data;
          this.employeeForm.patchValue({
            firstName: emp.firstName,
            lastName: emp.lastName,
            email: emp.email,
            empId: emp.empId,
            phone: emp.phone,
            departmentId: emp.departmentId,
            designation: emp.designation,
            salary: emp.salary,
            hireDate: emp.hireDate,
            status: emp.status || 'ACTIVE',
            address: emp.address
          });
        }
      },
      error: (err) => {
        this.loading = false;
        this.errorMessage = 'Failed to load employee details.';
      }
    });
  }

  isFieldInvalid(field: string): boolean {
    const control = this.employeeForm.get(field);
    return !!(control && control.invalid && (control.dirty || control.touched));
  }

  onSubmit(): void {
    if (this.employeeForm.invalid) {
      this.employeeForm.markAllAsTouched();
      return;
    }

    this.submitting = true;
    this.errorMessage = '';
    const formVal = this.employeeForm.value;

    const request$ = this.isEditMode && this.employeeId
      ? this.employeeService.update(this.employeeId, formVal)
      : this.employeeService.create(formVal);

    request$.subscribe({
      next: (res) => {
        this.submitting = false;
        if (res.success) {
          this.router.navigate(['/employees']);
        } else {
          this.errorMessage = res.message || 'Operation failed. Please try again.';
        }
      },
      error: (err) => {
        this.submitting = false;
        this.errorMessage = err.error?.message || 'Server error occurred. Please verify data.';
      }
    });
  }

  onCancel(): void {
    this.router.navigate(['/employees']);
  }
}

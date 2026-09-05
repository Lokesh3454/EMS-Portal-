import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { DepartmentService } from '../../../core/services/department.service';
import { AuthService } from '../../../core/services/auth.service';
import { Department } from '../../../core/models/employee.model';

@Component({
  selector: 'app-department-list',
  templateUrl: './department-list.component.html',
  styleUrls: ['./department-list.component.css']
})
export class DepartmentListComponent implements OnInit {
  departments: Department[] = [];
  loading = true;
  showModal = false;
  editingDepartment: Department | null = null;
  deptForm!: FormGroup;
  saving = false;
  errorMessage = '';

  constructor(
    private deptService: DepartmentService,
    public authService: AuthService,
    private fb: FormBuilder
  ) {}

  ngOnInit(): void {
    this.initForm();
    this.loadDepartments();
  }

  initForm(): void {
    this.deptForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(2)]],
      description: ['']
    });
  }

  loadDepartments(): void {
    this.loading = true;
    this.deptService.getAll().subscribe({
      next: (res) => {
        this.loading = false;
        if (res.success) {
          this.departments = res.data;
        }
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  openAddModal(): void {
    this.editingDepartment = null;
    this.deptForm.reset();
    this.errorMessage = '';
    this.showModal = true;
  }

  openEditModal(dept: Department): void {
    this.editingDepartment = dept;
    this.deptForm.patchValue({
      name: dept.name,
      description: dept.description
    });
    this.errorMessage = '';
    this.showModal = true;
  }

  closeModal(): void {
    this.showModal = false;
    this.editingDepartment = null;
    this.deptForm.reset();
  }

  saveDepartment(): void {
    if (this.deptForm.invalid) {
      this.deptForm.markAllAsTouched();
      return;
    }

    this.saving = true;
    this.errorMessage = '';
    const formVal = this.deptForm.value;

    const request$ = this.editingDepartment?.id
      ? this.deptService.update(this.editingDepartment.id, formVal)
      : this.deptService.create(formVal);

    request$.subscribe({
      next: (res) => {
        this.saving = false;
        if (res.success) {
          this.closeModal();
          this.loadDepartments();
        } else {
          this.errorMessage = res.message || 'Operation failed';
        }
      },
      error: (err) => {
        this.saving = false;
        this.errorMessage = err.error?.message || 'Server error occurred.';
      }
    });
  }

  deleteDepartment(dept: Department): void {
    if (!dept.id) return;
    if (confirm(`Are you sure you want to delete department "${dept.name}"?`)) {
      this.deptService.delete(dept.id).subscribe({
        next: (res) => {
          if (res.success) {
            this.loadDepartments();
          }
        }
      });
    }
  }
}

import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { EmployeeService } from '../../../core/services/employee.service';
import { AuthService } from '../../../core/services/auth.service';
import { Employee } from '../../../core/models/employee.model';

@Component({
  selector: 'app-my-profile',
  templateUrl: './my-profile.component.html',
  styleUrls: ['./my-profile.component.css']
})
export class MyProfileComponent implements OnInit {
  employee: Employee | null = null;
  loading = true;
  errorMessage = '';
  successMessage = '';

  // Edit Contact Modal
  showContactModal = false;
  contactForm!: FormGroup;
  savingContact = false;

  // Change Password Modal
  showPasswordModal = false;
  passwordForm!: FormGroup;
  savingPassword = false;
  passwordError = '';

  constructor(
    private employeeService: EmployeeService,
    public authService: AuthService,
    private fb: FormBuilder
  ) {}

  ngOnInit(): void {
    this.initForms();
    this.loadProfile();
  }

  initForms(): void {
    this.contactForm = this.fb.group({
      firstName: [''],
      lastName: [''],
      phone: ['', [Validators.pattern('^[0-9+ -]{7,15}$')]],
      address: ['']
    });

    this.passwordForm = this.fb.group({
      currentPassword: ['', [Validators.required]],
      newPassword: ['', [Validators.required, Validators.minLength(8)]],
      confirmPassword: ['', [Validators.required]]
    }, {
      validators: this.passwordMatchValidator
    });
  }

  passwordMatchValidator(form: FormGroup) {
    const newPass = form.get('newPassword')?.value;
    const confirmPass = form.get('confirmPassword')?.value;
    return newPass === confirmPass ? null : { passwordMismatch: true };
  }

  loadProfile(): void {
    this.loading = true;
    this.errorMessage = '';
    this.employeeService.getMyProfile().subscribe({
      next: (res) => {
        this.loading = false;
        if (res.success && res.data) {
          this.employee = res.data;
        } else {
          this.errorMessage = res.message || 'Profile data could not be retrieved.';
        }
      },
      error: (err) => {
        this.loading = false;
        this.errorMessage = err.error?.message || 'Failed to load profile.';
      }
    });
  }

  openEditContact(): void {
    if (!this.employee) return;
    this.contactForm.patchValue({
      firstName: this.employee.firstName || '',
      lastName: this.employee.lastName || '',
      phone: this.employee.phone || '',
      address: this.employee.address || ''
    });
    this.showContactModal = true;
  }

  closeEditContact(): void {
    this.showContactModal = false;
  }

  saveContact(): void {
    if (this.contactForm.invalid) return;
    this.savingContact = true;
    this.employeeService.updateMyProfile(this.contactForm.value).subscribe({
      next: (res) => {
        this.savingContact = false;
        if (res.success && res.data) {
          this.employee = res.data;
          this.closeEditContact();
          this.showSuccess('Your contact details have been updated successfully!');
        }
      },
      error: (err) => {
        this.savingContact = false;
        alert(err.error?.message || 'Failed to update contact info.');
      }
    });
  }

  openChangePassword(): void {
    this.passwordForm.reset();
    this.passwordError = '';
    this.showPasswordModal = true;
  }

  closeChangePassword(): void {
    this.showPasswordModal = false;
  }

  savePassword(): void {
    if (this.passwordForm.invalid) {
      this.passwordForm.markAllAsTouched();
      return;
    }
    this.savingPassword = true;
    this.passwordError = '';
    const { currentPassword, newPassword } = this.passwordForm.value;

    this.authService.changePassword({ currentPassword, newPassword }).subscribe({
      next: (res) => {
        this.savingPassword = false;
        if (res.success) {
          this.closeChangePassword();
          this.showSuccess('Your password has been changed successfully!');
        } else {
          this.passwordError = res.message || 'Password update failed.';
        }
      },
      error: (err) => {
        this.savingPassword = false;
        this.passwordError = err.error?.message || 'Incorrect current password or server error.';
      }
    });
  }

  getInitials(): string {
    if (!this.employee) return 'EM';
    return `${this.employee.firstName?.charAt(0) || ''}${this.employee.lastName?.charAt(0) || ''}`.toUpperCase();
  }

  showSuccess(msg: string): void {
    this.successMessage = msg;
    setTimeout(() => { this.successMessage = ''; }, 4500);
  }
}

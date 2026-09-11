import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LoginComponent } from './features/auth/login/login.component';
import { MainLayoutComponent } from './shared/main-layout/main-layout.component';
import { DashboardComponent } from './features/dashboard/dashboard.component';
import { EmployeeListComponent } from './features/employees/employee-list/employee-list.component';
import { EmployeeFormComponent } from './features/employees/employee-form/employee-form.component';
import { EmployeeDetailComponent } from './features/employees/employee-detail/employee-detail.component';
import { DepartmentListComponent } from './features/departments/department-list/department-list.component';
import { MyProfileComponent } from './features/profile/my-profile/my-profile.component';
import { AttendanceComponent } from './features/attendance/attendance.component';
import { LeavesComponent } from './features/leaves/leaves.component';
import { ManagerPortalComponent } from './features/manager/manager-portal/manager-portal.component';
import { PayrollComponent } from './features/payroll/payroll.component';
import { PerformanceComponent } from './features/performance/performance.component';
import { DocumentsComponent } from './features/documents/documents.component';
import { RecruitmentComponent } from './features/recruitment/recruitment.component';
import { AuthGuard } from './core/guards/auth.guard';
import { RoleGuard } from './core/guards/role.guard';

const routes: Routes = [
  { path: 'login', component: LoginComponent },
  {
    path: '',
    component: MainLayoutComponent,
    canActivate: [AuthGuard],
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      { path: 'dashboard', component: DashboardComponent },
      { path: 'attendance', component: AttendanceComponent },
      { path: 'leaves', component: LeavesComponent },
      { path: 'manager', component: ManagerPortalComponent, canActivate: [RoleGuard], data: { roles: ['ROLE_ADMIN', 'ROLE_HR', 'ROLE_MANAGER'] } },
      { path: 'payroll', component: PayrollComponent },
      { path: 'performance', component: PerformanceComponent },
      { path: 'documents', component: DocumentsComponent },
      { path: 'recruitment', component: RecruitmentComponent, canActivate: [RoleGuard], data: { roles: ['ROLE_ADMIN', 'ROLE_HR', 'ROLE_MANAGER'] } },
      { path: 'employees', component: EmployeeListComponent, canActivate: [RoleGuard], data: { roles: ['ROLE_ADMIN', 'ROLE_HR', 'ROLE_MANAGER'] } },
      { path: 'employees/new', component: EmployeeFormComponent, canActivate: [RoleGuard], data: { roles: ['ROLE_ADMIN', 'ROLE_HR'] } },
      { path: 'employees/:id/edit', component: EmployeeFormComponent, canActivate: [RoleGuard], data: { roles: ['ROLE_ADMIN', 'ROLE_HR'] } },
      { path: 'employees/:id', component: EmployeeDetailComponent, canActivate: [RoleGuard], data: { roles: ['ROLE_ADMIN', 'ROLE_HR', 'ROLE_MANAGER'] } },
      { path: 'departments', component: DepartmentListComponent, canActivate: [RoleGuard], data: { roles: ['ROLE_ADMIN', 'ROLE_HR'] } },
      { path: 'my-profile', component: MyProfileComponent },
      { path: 'profile', redirectTo: 'my-profile' },
      { path: 'access-denied', redirectTo: 'dashboard' }
    ]
  },
  { path: '**', redirectTo: 'dashboard' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }

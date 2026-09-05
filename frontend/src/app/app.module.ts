import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { LoginComponent } from './features/auth/login/login.component';
import { SidebarComponent } from './shared/sidebar/sidebar.component';
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
import { AiAssistantComponent } from './shared/ai-assistant/ai-assistant.component';
import { HeaderComponent } from './shared/header/header.component';
import { AuthInterceptor } from './core/interceptors/auth.interceptor';

@NgModule({
  declarations: [
    AppComponent,
    LoginComponent,
    SidebarComponent,
    HeaderComponent,
    MainLayoutComponent,
    DashboardComponent,
    EmployeeListComponent,
    EmployeeFormComponent,
    EmployeeDetailComponent,
    DepartmentListComponent,
    MyProfileComponent,
    AttendanceComponent,
    LeavesComponent,
    ManagerPortalComponent,
    PayrollComponent,
    PerformanceComponent,
    DocumentsComponent,
    RecruitmentComponent,
    AiAssistantComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    HttpClientModule,
    FormsModule,
    ReactiveFormsModule
  ],
  providers: [
    {
      provide: HTTP_INTERCEPTORS,
      useClass: AuthInterceptor,
      multi: true
    }
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }

import { Component, OnInit } from '@angular/core';
import { Router, NavigationEnd } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { LeaveService } from '../../core/services/leave.service';
import { filter } from 'rxjs/operators';

interface MenuItem {
  label: string;
  icon: string;
  route: string;
  roles: string[];
  badge?: number;
}

@Component({
  selector: 'app-sidebar',
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.css']
})
export class SidebarComponent implements OnInit {
  currentUrl = '';
  isCollapsed = false;
  user: any;
  pendingLeavesCount = 0;

  menuItems: MenuItem[] = [
    { label: 'Dashboard', icon: 'bi-speedometer2', route: '/dashboard', roles: ['ROLE_ADMIN','ROLE_HR','ROLE_MANAGER','ROLE_EMPLOYEE'] },
    { label: 'Attendance', icon: 'bi-clock-history', route: '/attendance', roles: ['ROLE_ADMIN','ROLE_HR','ROLE_MANAGER','ROLE_EMPLOYEE'] },
    { label: 'Leave Management', icon: 'bi-calendar-check-fill', route: '/leaves', roles: ['ROLE_ADMIN','ROLE_HR','ROLE_MANAGER','ROLE_EMPLOYEE'] },
    { label: 'Payroll & Payslips', icon: 'bi-wallet2', route: '/payroll', roles: ['ROLE_ADMIN','ROLE_HR','ROLE_MANAGER','ROLE_EMPLOYEE'] },
    { label: 'Performance & OKRs', icon: 'bi-award-fill', route: '/performance', roles: ['ROLE_ADMIN','ROLE_HR','ROLE_MANAGER','ROLE_EMPLOYEE'] },
    { label: 'Document Vault', icon: 'bi-file-earmark-lock-fill', route: '/documents', roles: ['ROLE_ADMIN','ROLE_HR','ROLE_MANAGER','ROLE_EMPLOYEE'] },
    { label: 'Recruitment (ATS)', icon: 'bi-person-lines-fill', route: '/recruitment', roles: ['ROLE_ADMIN','ROLE_HR','ROLE_MANAGER'] },
    { label: 'Manager Portal', icon: 'bi-briefcase-fill', route: '/manager', roles: ['ROLE_ADMIN','ROLE_HR','ROLE_MANAGER'] },
    { label: 'Employees', icon: 'bi-people-fill', route: '/employees', roles: ['ROLE_ADMIN','ROLE_HR','ROLE_MANAGER'] },
    { label: 'Add Employee', icon: 'bi-person-plus-fill', route: '/employees/new', roles: ['ROLE_ADMIN','ROLE_HR'] },
    { label: 'Departments', icon: 'bi-diagram-3-fill', route: '/departments', roles: ['ROLE_ADMIN','ROLE_HR'] },
    { label: 'My Profile', icon: 'bi-person-circle', route: '/my-profile', roles: ['ROLE_ADMIN','ROLE_HR','ROLE_MANAGER','ROLE_EMPLOYEE'] },
  ];

  constructor(
    public authService: AuthService,
    private leaveService: LeaveService,
    private router: Router
  ) {
    this.user = this.authService.getCurrentUser();
    this.router.events.pipe(filter(e => e instanceof NavigationEnd)).subscribe((e: any) => {
      this.currentUrl = e.url;
    });
    this.currentUrl = this.router.url;
  }

  ngOnInit(): void {
    const role = this.authService.getRole();
    if (role === 'ROLE_ADMIN' || role === 'ROLE_HR' || role === 'ROLE_MANAGER') {
      this.loadPendingCount();
    }
  }

  loadPendingCount(): void {
    this.leaveService.getPendingCount().subscribe({
      next: (res) => {
        if (res.success && res.data) {
          this.pendingLeavesCount = res.data.count || 0;
          const leaveItem = this.menuItems.find(i => i.route === '/leaves');
          if (leaveItem) {
            leaveItem.badge = this.pendingLeavesCount > 0 ? this.pendingLeavesCount : undefined;
          }
        }
      }
    });
  }

  get visibleMenuItems() {
    const role = this.authService.getRole();
    return this.menuItems.filter(item => item.roles.includes(role));
  }

  isActive(route: string): boolean {
    return this.currentUrl === route || this.currentUrl.startsWith(route + '/');
  }

  toggleSidebar(): void { this.isCollapsed = !this.isCollapsed; }

  logout(): void { this.authService.logout(); }

  getRoleLabel(role: string): string {
    const labels: any = {
      'ROLE_ADMIN': 'Administrator', 'ROLE_HR': 'HR Manager',
      'ROLE_MANAGER': 'Manager', 'ROLE_EMPLOYEE': 'Employee'
    };
    return labels[role] || role;
  }
}

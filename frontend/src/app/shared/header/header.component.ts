import { Component, OnInit, OnDestroy, Output, EventEmitter, HostListener, ElementRef } from '@angular/core';
import { AuthService } from '../../core/services/auth.service';
import { LoginResponse } from '../../core/models/auth.model';
import { LeaveService, LeaveRequestRecord } from '../../core/services/leave.service';
import { Router } from '@angular/router';

export interface AppNotification {
  id: string;
  title: string;
  message: string;
  time: string;
  type: 'leave' | 'attendance' | 'system' | 'info';
  route: string;
  read: boolean;
  icon: string;
  iconBg: string;
}

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.css']
})
export class HeaderComponent implements OnInit, OnDestroy {
  @Output() toggleSidebar = new EventEmitter<void>();

  currentUser: LoginResponse | null = null;
  isAdmin = false;
  showNotifications = false;
  currentTime = '';
  timeInterval: any;

  notifications: AppNotification[] = [];

  constructor(
    public authService: AuthService,
    private leaveService: LeaveService,
    private router: Router,
    private elementRef: ElementRef
  ) {}

  ngOnInit(): void {
    this.authService.currentUser$.subscribe(user => {
      this.currentUser = user;
      const role = this.authService.getRole();
      this.isAdmin = role === 'ROLE_ADMIN' || role === 'ROLE_HR' || role === 'ROLE_MANAGER';
      this.loadNotifications();
    });

    this.updateCurrentTime();
    this.timeInterval = setInterval(() => this.updateCurrentTime(), 1000);
  }

  ngOnDestroy(): void {
    if (this.timeInterval) {
      clearInterval(this.timeInterval);
    }
  }

  updateCurrentTime(): void {
    const now = new Date();
    this.currentTime = now.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' });
  }

  loadNotifications(): void {
    const defaultNotifs: AppNotification[] = [
      {
        id: 'notif-1',
        title: 'Attendance Shift Alert',
        message: 'Daily shift window active. Mark clock-in before 09:30 AM.',
        time: 'Today, 09:00 AM',
        type: 'attendance',
        route: '/attendance',
        read: false,
        icon: 'bi-clock-fill',
        iconBg: '#3b82f6'
      },
      {
        id: 'notif-2',
        title: 'Leave Policy FY2026',
        message: 'Annual leave quotas (CL: 12, SL: 10, EL: 15) active for 2026.',
        time: 'Yesterday',
        type: 'leave',
        route: '/leaves',
        read: false,
        icon: 'bi-calendar-heart-fill',
        iconBg: '#10b981'
      },
      {
        id: 'notif-3',
        title: 'Profile KYC & Security',
        message: 'Your emergency contacts and credentials are up to date.',
        time: '2 days ago',
        type: 'system',
        route: '/profile',
        read: true,
        icon: 'bi-shield-fill-check',
        iconBg: '#8b5cf6'
      }
    ];

    if (this.isAdmin) {
      this.leaveService.getPendingLeaves().subscribe({
        next: (res: any) => {
          const list: LeaveRequestRecord[] = res.data || [];
          const pendingNotifs: AppNotification[] = list.slice(0, 3).map((p: LeaveRequestRecord) => ({
            id: `leave-req-${p.id}`,
            title: `Pending Leave: ${p.employeeName}`,
            message: `${p.employeeName} applied for ${p.totalDays} day(s) of ${p.leaveType} leave.`,
            time: p.appliedAt ? new Date(p.appliedAt).toLocaleDateString() : 'Recent',
            type: 'leave',
            route: '/leaves',
            read: false,
            icon: 'bi-hourglass-split',
            iconBg: '#f59e0b'
          }));
          this.notifications = [...pendingNotifs, ...defaultNotifs];
        },
        error: () => {
          this.notifications = defaultNotifs;
        }
      });
    } else {
      this.notifications = defaultNotifs;
    }
  }

  get unreadCount(): number {
    return this.notifications.filter(n => !n.read).length;
  }

  toggleNotificationDropdown(event: MouseEvent): void {
    event.stopPropagation();
    this.showNotifications = !this.showNotifications;
  }

  markAllAsRead(event?: MouseEvent): void {
    if (event) event.stopPropagation();
    this.notifications.forEach(n => n.read = true);
  }

  openNotification(notif: AppNotification): void {
    notif.read = true;
    this.showNotifications = false;
    this.router.navigate([notif.route]);
  }

  goToProfile(): void {
    this.showNotifications = false;
    this.router.navigate(['/profile']);
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent): void {
    if (!this.elementRef.nativeElement.contains(event.target)) {
      this.showNotifications = false;
    }
  }
}

import { Component } from '@angular/core';
import { AuthService } from '../../core/services/auth.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-main-layout',
  template: `
    <div class="layout-wrapper" [class.sidebar-collapsed]="sidebarCollapsed">
      <app-sidebar (toggleEvent)="sidebarCollapsed = !sidebarCollapsed"></app-sidebar>
      <div class="main-content" [style.margin-left]="sidebarCollapsed ? '72px' : '260px'">
        <app-header (toggleSidebar)="sidebarCollapsed = !sidebarCollapsed"></app-header>
        <div class="content-area">
          <router-outlet></router-outlet>
        </div>
      </div>
      <app-ai-assistant></app-ai-assistant>
    </div>
  `,
  styles: [`
    .layout-wrapper { display: flex; min-height: 100vh; background: #f1f5f9; }
    .main-content { flex: 1; transition: margin-left 0.3s ease; min-height: 100vh; display: flex; flex-direction: column; }
    .content-area { padding: 28px; max-width: 1440px; width: 100%; box-sizing: border-box; }
  `]
})
export class MainLayoutComponent {
  sidebarCollapsed = false;
  constructor(public authService: AuthService, private router: Router) {}
}

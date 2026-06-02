import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AdminService, AdminStats } from '../../services/admin.service';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule],
  template: `
    <h1 class="page-title">Dashboard</h1>

    <div class="loading" *ngIf="loading">Loading...</div>

    <div class="grid" *ngIf="stats && !loading">
      <div class="stat">
        <span class="label">Total users</span>
        <span class="value">{{ stats.totalUsers }}</span>
      </div>
      <div class="stat">
        <span class="label">Active</span>
        <span class="value">{{ stats.activeUsers }}</span>
      </div>
      <div class="stat danger">
        <span class="label">Suspended</span>
        <span class="value">{{ stats.suspendedUsers }}</span>
      </div>
      <div class="stat">
        <span class="label">Admins</span>
        <span class="value">{{ stats.adminCount }}</span>
      </div>
      <div class="stat">
        <span class="label">Total posts</span>
        <span class="value">{{ stats.totalPosts }}</span>
      </div>
      <div class="stat">
        <span class="label">Active posts</span>
        <span class="value">{{ stats.activePosts }}</span>
      </div>
      <div class="stat warning">
        <span class="label">Pending reports</span>
        <span class="value">{{ stats.pendingReports }}</span>
      </div>
      <div class="stat">
        <span class="label">New users (7d)</span>
        <span class="value">{{ stats.newUsersLast7Days }}</span>
      </div>
    </div>

    <p class="err" *ngIf="error">{{ error }}</p>
  `,
  styles: [`
    .page-title { font-size: 22px; font-weight: 700; color: var(--text-primary); margin: 0 0 22px; }
    .grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(190px, 1fr)); gap: 16px; }
    .stat {
      background: var(--bg-card); border: 1px solid var(--border);
      border-radius: var(--radius); padding: 20px; display: flex; flex-direction: column; gap: 8px;
      box-shadow: var(--shadow-sm);
    }
    .stat .label { font-size: 13px; color: var(--text-muted); font-weight: 500; }
    .stat .value { font-size: 30px; font-weight: 700; color: var(--text-primary); }
    .stat.danger .value { color: var(--danger); }
    .stat.warning .value { color: var(--warning); }
    .loading { color: var(--text-muted); }
    .err { color: var(--danger); margin-top: 16px; }
  `]
})
export class AdminDashboardComponent implements OnInit {
  stats?: AdminStats;
  loading = true;
  error = '';

  constructor(private admin: AdminService) { }

  ngOnInit(): void {
    this.admin.getStats().subscribe({
      next: s => { this.stats = s; this.loading = false; },
      error: () => { this.error = 'Failed to load stats'; this.loading = false; }
    });
  }
}

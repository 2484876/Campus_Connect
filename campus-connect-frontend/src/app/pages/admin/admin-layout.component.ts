import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-admin-layout',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <div class="admin-shell">
      <aside class="admin-sidebar">
        <div class="admin-brand">Admin Console</div>
        <nav>
          <a routerLink="/admin/dashboard" routerLinkActive="active">Dashboard</a>
          <a routerLink="/admin/users" routerLinkActive="active">Users</a>
          <a routerLink="/admin/reports" routerLinkActive="active">Reports</a>
          <a routerLink="/admin/content" routerLinkActive="active">Content</a>
          <a routerLink="/admin/logs" routerLinkActive="active">Audit Log</a>
        </nav>
        <a class="admin-back" routerLink="/feed">Back to app</a>
      </aside>
      <main class="admin-main">
        <router-outlet></router-outlet>
      </main>
    </div>
  `,
  styles: [`
    .admin-shell { display: flex; min-height: calc(100vh - 60px); background: var(--bg-main); }
    .admin-sidebar {
      width: 230px; flex-shrink: 0; background: var(--bg-card);
      border-right: 1px solid var(--border); padding: 24px 16px;
      display: flex; flex-direction: column; gap: 6px; position: sticky; top: 60px;
      height: calc(100vh - 60px);
    }
    .admin-brand {
      font-weight: 700; font-size: 15px; color: var(--text-primary);
      padding: 0 12px 16px; letter-spacing: .3px;
    }
    .admin-sidebar nav { display: flex; flex-direction: column; gap: 4px; }
    .admin-sidebar a {
      text-decoration: none; color: var(--text-secondary); padding: 10px 12px;
      border-radius: var(--radius-sm); font-size: 14px; font-weight: 500; transition: .15s;
    }
    .admin-sidebar a:hover { background: var(--bg-hover); color: var(--text-primary); }
    .admin-sidebar a.active { background: var(--primary-light); color: var(--primary); }
    .admin-back {
      margin-top: auto; font-size: 13px !important; color: var(--text-muted) !important;
    }
    .admin-main { flex: 1; padding: 28px 32px; max-width: 1100px; }
    @media (max-width: 720px) {
      .admin-shell { flex-direction: column; }
      .admin-sidebar { width: auto; height: auto; position: static; flex-direction: row; flex-wrap: wrap; }
      .admin-sidebar nav { flex-direction: row; flex-wrap: wrap; }
      .admin-back { margin-top: 0; }
    }
  `]
})
export class AdminLayoutComponent { }

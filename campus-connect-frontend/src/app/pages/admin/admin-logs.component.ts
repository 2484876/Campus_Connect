import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AdminService, AdminLog } from '../../services/admin.service';

@Component({
  selector: 'app-admin-logs',
  standalone: true,
  imports: [CommonModule],
  template: `
    <h1 class="page-title">Audit log</h1>

    <div class="loading" *ngIf="loading">Loading...</div>

    <table class="tbl" *ngIf="!loading && logs.length">
      <thead>
        <tr><th>When</th><th>Admin</th><th>Action</th><th>Target</th><th>Details</th></tr>
      </thead>
      <tbody>
        <tr *ngFor="let l of logs">
          <td class="muted">{{ l.createdAt | date:'medium' }}</td>
          <td>{{ l.adminName }}</td>
          <td><span class="tag">{{ l.action }}</span></td>
          <td class="muted">
            {{ l.targetType }}<span *ngIf="l.targetId"> #{{ l.targetId }}</span>
            <span *ngIf="l.targetLabel"> ({{ l.targetLabel }})</span>
          </td>
          <td class="muted">{{ l.details }}</td>
        </tr>
      </tbody>
    </table>

    <p class="empty" *ngIf="!loading && !logs.length">No actions logged yet.</p>

    <div class="pager" *ngIf="totalPages > 1">
      <button class="btn" (click)="prev()" [disabled]="page === 0">Prev</button>
      <span>Page {{ page + 1 }} of {{ totalPages }}</span>
      <button class="btn" (click)="next()" [disabled]="page + 1 >= totalPages">Next</button>
    </div>
  `,
  styles: [`
    .page-title { font-size: 22px; font-weight: 700; color: var(--text-primary); margin: 0 0 22px; }
    .tbl { width: 100%; border-collapse: collapse; background: var(--bg-card);
      border: 1px solid var(--border); border-radius: var(--radius); overflow: hidden; }
    .tbl th, .tbl td { text-align: left; padding: 11px 14px; font-size: 13px;
      border-bottom: 1px solid var(--border); color: var(--text-primary); }
    .tbl th { background: var(--bg-sunken); font-weight: 600; color: var(--text-secondary); }
    .muted { color: var(--text-muted); }
    .tag { background: var(--primary-light); color: var(--primary); padding: 2px 9px;
      border-radius: 6px; font-size: 12px; font-weight: 600; }
    .btn { padding: 9px 16px; border: none; border-radius: var(--radius-sm); background: var(--primary);
      color: #fff; font-weight: 600; cursor: pointer; font-size: 14px; }
    .btn:disabled { opacity: .5; cursor: default; }
    .pager { display: flex; align-items: center; gap: 14px; margin-top: 18px; color: var(--text-secondary); font-size: 14px; }
    .loading, .empty { color: var(--text-muted); }
  `]
})
export class AdminLogsComponent implements OnInit {
  logs: AdminLog[] = [];
  page = 0;
  totalPages = 0;
  loading = true;

  constructor(private admin: AdminService) { }

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading = true;
    this.admin.getLogs(this.page).subscribe({
      next: res => { this.logs = res.content; this.totalPages = res.totalPages; this.loading = false; },
      error: () => { this.loading = false; }
    });
  }

  next(): void { this.page++; this.load(); }
  prev(): void { if (this.page > 0) { this.page--; this.load(); } }
}

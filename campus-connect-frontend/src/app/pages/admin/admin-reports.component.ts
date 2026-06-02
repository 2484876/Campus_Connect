import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AdminService, AdminReport } from '../../services/admin.service';

@Component({
  selector: 'app-admin-reports',
  standalone: true,
  imports: [CommonModule],
  template: `
    <h1 class="page-title">Pending reports</h1>

    <p class="msg" *ngIf="message">{{ message }}</p>
    <div class="loading" *ngIf="loading">Loading...</div>

    <div class="list" *ngIf="!loading && reports.length">
      <div class="card" *ngFor="let r of reports">
        <div class="head">
          <span class="type">{{ r.targetType }} #{{ r.targetId }}</span>
          <span class="reason">{{ r.reason }}</span>
        </div>
        <p class="details" *ngIf="r.details">{{ r.details }}</p>
        <div class="meta">Reported by {{ r.reporterName }} &middot; {{ r.createdAt | date:'medium' }}</div>
        <div class="acts">
          <button class="btn ok" (click)="resolve(r, 'RESOLVED')">Mark resolved</button>
          <button class="btn ghost" (click)="resolve(r, 'DISMISSED')">Dismiss</button>
          <button class="btn danger" (click)="removeTarget(r)" *ngIf="canRemove(r)">Remove content</button>
        </div>
      </div>
    </div>

    <p class="empty" *ngIf="!loading && !reports.length">No pending reports.</p>

    <div class="pager" *ngIf="totalPages > 1">
      <button class="btn ghost" (click)="prev()" [disabled]="page === 0">Prev</button>
      <span>Page {{ page + 1 }} of {{ totalPages }}</span>
      <button class="btn ghost" (click)="next()" [disabled]="page + 1 >= totalPages">Next</button>
    </div>
  `,
  styles: [`
    .page-title { font-size: 22px; font-weight: 700; color: var(--text-primary); margin: 0 0 22px; }
    .list { display: flex; flex-direction: column; gap: 14px; }
    .card { background: var(--bg-card); border: 1px solid var(--border); border-radius: var(--radius);
      padding: 18px; box-shadow: var(--shadow-sm); }
    .head { display: flex; gap: 12px; align-items: center; margin-bottom: 8px; }
    .type { font-weight: 700; color: var(--text-primary); font-size: 14px; }
    .reason { background: var(--accent-soft); color: var(--warning); padding: 3px 10px;
      border-radius: 20px; font-size: 12px; font-weight: 600; }
    .details { color: var(--text-secondary); font-size: 14px; margin: 4px 0 8px; }
    .meta { color: var(--text-muted); font-size: 12px; margin-bottom: 14px; }
    .acts { display: flex; gap: 10px; flex-wrap: wrap; }
    .btn { padding: 7px 14px; border: none; border-radius: var(--radius-sm); cursor: pointer;
      font-weight: 600; font-size: 13px; }
    .btn.ok { background: var(--primary); color: #fff; }
    .btn.ghost { background: var(--bg-hover); color: var(--text-secondary); }
    .btn.danger { background: var(--accent-soft); color: var(--danger); }
    .pager { display: flex; align-items: center; gap: 14px; margin-top: 18px; color: var(--text-secondary); font-size: 14px; }
    .loading, .empty { color: var(--text-muted); }
    .msg { color: var(--success); font-weight: 500; margin-bottom: 12px; }
  `]
})
export class AdminReportsComponent implements OnInit {
  reports: AdminReport[] = [];
  page = 0;
  totalPages = 0;
  loading = true;
  message = '';

  private removable = ['POST', 'COMMENT', 'COMMUNITY_POST', 'EVENT', 'STORY'];

  constructor(private admin: AdminService) { }

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading = true;
    this.admin.getReports(this.page).subscribe({
      next: res => { this.reports = res.content; this.totalPages = res.totalPages; this.loading = false; },
      error: () => { this.loading = false; this.flash('Failed to load reports'); }
    });
  }

  next(): void { this.page++; this.load(); }
  prev(): void { if (this.page > 0) { this.page--; this.load(); } }

  canRemove(r: AdminReport): boolean {
    return this.removable.includes((r.targetType || '').toUpperCase());
  }

  resolve(r: AdminReport, status: string): void {
    this.admin.resolveReport(r.id, status).subscribe({
      next: () => { this.flash('Report ' + status.toLowerCase()); this.load(); },
      error: e => this.flash(this.errMsg(e))
    });
  }

  removeTarget(r: AdminReport): void {
    if (!window.confirm('Remove the reported ' + r.targetType + '?')) return;
    this.admin.removeContent(r.targetType.toUpperCase(), r.targetId).subscribe({
      next: () => {
        this.admin.resolveReport(r.id, 'RESOLVED').subscribe(() => { this.flash('Content removed'); this.load(); });
      },
      error: e => this.flash(this.errMsg(e))
    });
  }

  private flash(msg: string): void {
    this.message = msg;
    setTimeout(() => { if (this.message === msg) this.message = ''; }, 3500);
  }

  private errMsg(e: any): string {
    return e?.error?.message || e?.error?.error || 'Action failed';
  }
}

import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AdminService, AdminContent } from '../../services/admin.service';

@Component({
  selector: 'app-admin-content',
  standalone: true,
  imports: [CommonModule],
  template: `
    <h1 class="page-title">Content moderation</h1>

    <p class="msg" *ngIf="message">{{ message }}</p>
    <div class="loading" *ngIf="loading">Loading...</div>

    <table class="tbl" *ngIf="!loading && items.length">
      <thead>
        <tr><th>Author</th><th>Preview</th><th>Status</th><th>Created</th><th></th></tr>
      </thead>
      <tbody>
        <tr *ngFor="let it of items" [class.dim]="!it.active">
          <td>{{ it.authorName }}</td>
          <td class="preview">{{ it.preview || '(no text)' }}</td>
          <td>
            <span class="badge" [class.on]="it.active" [class.off]="!it.active">
              {{ it.active ? 'Visible' : 'Removed' }}
            </span>
          </td>
          <td class="muted">{{ it.createdAt | date:'short' }}</td>
          <td>
            <button class="link danger" *ngIf="it.active" (click)="remove(it)">Remove</button>
          </td>
        </tr>
      </tbody>
    </table>

    <p class="empty" *ngIf="!loading && !items.length">No posts found.</p>

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
    .tbl th, .tbl td { text-align: left; padding: 12px 14px; font-size: 14px;
      border-bottom: 1px solid var(--border); color: var(--text-primary); vertical-align: top; }
    .tbl th { background: var(--bg-sunken); font-weight: 600; color: var(--text-secondary); font-size: 13px; }
    .tbl tr.dim { opacity: .55; }
    .preview { max-width: 420px; }
    .muted { color: var(--text-muted); }
    .badge { padding: 3px 10px; border-radius: 20px; font-size: 12px; font-weight: 600; }
    .badge.on { background: var(--primary-light); color: var(--primary); }
    .badge.off { background: var(--accent-soft); color: var(--danger); }
    .link { background: none; border: none; cursor: pointer; font-size: 13px; font-weight: 600; padding: 0; }
    .link.danger { color: var(--danger); }
    .btn { padding: 9px 16px; border: none; border-radius: var(--radius-sm); background: var(--primary);
      color: #fff; font-weight: 600; cursor: pointer; font-size: 14px; }
    .btn:disabled { opacity: .5; cursor: default; }
    .pager { display: flex; align-items: center; gap: 14px; margin-top: 18px; color: var(--text-secondary); font-size: 14px; }
    .loading, .empty { color: var(--text-muted); }
    .msg { color: var(--success); font-weight: 500; margin-bottom: 12px; }
  `]
})
export class AdminContentComponent implements OnInit {
  items: AdminContent[] = [];
  page = 0;
  totalPages = 0;
  loading = true;
  message = '';

  constructor(private admin: AdminService) { }

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading = true;
    this.admin.getPosts(this.page).subscribe({
      next: res => { this.items = res.content; this.totalPages = res.totalPages; this.loading = false; },
      error: () => { this.loading = false; this.flash('Failed to load content'); }
    });
  }

  next(): void { this.page++; this.load(); }
  prev(): void { if (this.page > 0) { this.page--; this.load(); } }

  remove(it: AdminContent): void {
    if (!window.confirm('Remove this ' + it.type.toLowerCase() + '?')) return;
    this.admin.removeContent(it.type, it.id).subscribe({
      next: () => { it.active = false; this.flash('Content removed'); },
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

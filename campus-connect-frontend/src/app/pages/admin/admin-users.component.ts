import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService, AdminUser } from '../../services/admin.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-admin-users',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <h1 class="page-title">Users</h1>

    <div class="filters">
      <input [(ngModel)]="q" (keyup.enter)="reload()" placeholder="Search name or email" class="inp" />
      <select [(ngModel)]="status" (change)="reload()" class="inp">
        <option value="">All statuses</option>
        <option value="ACTIVE">Active</option>
        <option value="SUSPENDED">Suspended</option>
      </select>
      <select [(ngModel)]="roleFilter" (change)="reload()" class="inp">
        <option value="">All roles</option>
        <option *ngFor="let r of roles" [value]="r">{{ r }}</option>
      </select>
      <button class="btn" (click)="reload()">Search</button>
    </div>

    <p class="msg" *ngIf="message">{{ message }}</p>

    <div class="loading" *ngIf="loading">Loading...</div>

    <table class="tbl" *ngIf="!loading && users.length">
      <thead>
        <tr>
          <th>Name</th><th>Email</th><th>Role</th><th>Status</th><th>Posts</th><th>Actions</th>
        </tr>
      </thead>
      <tbody>
        <tr *ngFor="let u of users" [class.dim]="!u.active">
          <td>{{ u.name }}</td>
          <td class="muted">{{ u.email }}</td>
          <td>
            <select [(ngModel)]="u.role" (change)="changeRole(u)" class="role-sel" [disabled]="u.id === selfId">
              <option *ngFor="let r of roles" [value]="r">{{ r }}</option>
            </select>
          </td>
          <td>
            <span class="badge" [class.on]="u.active" [class.off]="!u.active">
              {{ u.active ? 'Active' : 'Suspended' }}
            </span>
          </td>
          <td>{{ u.postCount }}</td>
          <td class="actions">
            <button class="link" *ngIf="u.active" (click)="suspend(u)" [disabled]="u.id === selfId">Suspend</button>
            <button class="link ok" *ngIf="!u.active" (click)="reactivate(u)">Reactivate</button>
            <button class="link danger" (click)="remove(u, 'SOFT')" [disabled]="u.id === selfId">Delete</button>
            <button class="link danger" (click)="remove(u, 'HARD')" [disabled]="u.id === selfId">Hard delete</button>
          </td>
        </tr>
      </tbody>
    </table>

    <p class="empty" *ngIf="!loading && !users.length">No users found.</p>

    <div class="pager" *ngIf="totalPages > 1">
      <button class="btn" (click)="prev()" [disabled]="page === 0">Prev</button>
      <span>Page {{ page + 1 }} of {{ totalPages }}</span>
      <button class="btn" (click)="next()" [disabled]="page + 1 >= totalPages">Next</button>
    </div>
  `,
  styles: [`
    .page-title { font-size: 22px; font-weight: 700; color: var(--text-primary); margin: 0 0 22px; }
    .filters { display: flex; gap: 10px; flex-wrap: wrap; margin-bottom: 18px; }
    .inp {
      padding: 9px 12px; border: 1px solid var(--border-strong); border-radius: var(--radius-sm);
      background: var(--bg-card); color: var(--text-primary); font-size: 14px;
    }
    .btn {
      padding: 9px 16px; border: none; border-radius: var(--radius-sm); background: var(--primary);
      color: #fff; font-weight: 600; cursor: pointer; font-size: 14px;
    }
    .btn:disabled { opacity: .5; cursor: default; }
    .tbl { width: 100%; border-collapse: collapse; background: var(--bg-card);
      border: 1px solid var(--border); border-radius: var(--radius); overflow: hidden; }
    .tbl th, .tbl td { text-align: left; padding: 12px 14px; font-size: 14px;
      border-bottom: 1px solid var(--border); color: var(--text-primary); }
    .tbl th { background: var(--bg-sunken); font-weight: 600; color: var(--text-secondary); font-size: 13px; }
    .tbl tr.dim { opacity: .55; }
    .muted { color: var(--text-muted); }
    .badge { padding: 3px 10px; border-radius: 20px; font-size: 12px; font-weight: 600; }
    .badge.on { background: var(--primary-light); color: var(--primary); }
    .badge.off { background: var(--accent-soft); color: var(--danger); }
    .role-sel { padding: 5px 8px; border: 1px solid var(--border-strong); border-radius: var(--radius-sm);
      background: var(--bg-card); color: var(--text-primary); font-size: 12px; max-width: 170px; }
    .actions { display: flex; gap: 12px; flex-wrap: wrap; }
    .link { background: none; border: none; cursor: pointer; font-size: 13px; font-weight: 600;
      color: var(--primary); padding: 0; }
    .link.ok { color: var(--success); }
    .link.danger { color: var(--danger); }
    .link:disabled { opacity: .4; cursor: default; }
    .pager { display: flex; align-items: center; gap: 14px; margin-top: 18px; color: var(--text-secondary); font-size: 14px; }
    .loading, .empty { color: var(--text-muted); }
    .msg { color: var(--success); font-weight: 500; margin-bottom: 12px; }
  `]
})
export class AdminUsersComponent implements OnInit {
  users: AdminUser[] = [];
  roles: string[] = [];
  q = '';
  status = '';
  roleFilter = '';
  page = 0;
  totalPages = 0;
  loading = true;
  message = '';
  selfId = 0;

  constructor(private admin: AdminService, private auth: AuthService) { }

  ngOnInit(): void {
    this.roles = this.admin.roles();
    this.selfId = this.auth.getCurrentUser()?.userId ?? 0;
    this.load();
  }

  load(): void {
    this.loading = true;
    this.admin.getUsers(this.q, this.status, this.roleFilter, this.page).subscribe({
      next: res => {
        this.users = res.content;
        this.totalPages = res.totalPages;
        this.loading = false;
      },
      error: () => { this.loading = false; this.flash('Failed to load users'); }
    });
  }

  reload(): void { this.page = 0; this.load(); }
  next(): void { this.page++; this.load(); }
  prev(): void { if (this.page > 0) { this.page--; this.load(); } }

  suspend(u: AdminUser): void {
    const reason = window.prompt('Reason for suspending ' + u.name + '?', '') ?? '';
    this.admin.setStatus(u.id, false, reason).subscribe({
      next: () => { u.active = false; this.flash(u.name + ' suspended'); },
      error: e => this.flash(this.errMsg(e))
    });
  }

  reactivate(u: AdminUser): void {
    this.admin.setStatus(u.id, true, '').subscribe({
      next: () => { u.active = true; this.flash(u.name + ' reactivated'); },
      error: e => this.flash(this.errMsg(e))
    });
  }

  changeRole(u: AdminUser): void {
    this.admin.setRole(u.id, u.role).subscribe({
      next: () => this.flash(u.name + ' role updated'),
      error: e => { this.flash(this.errMsg(e)); this.load(); }
    });
  }

  remove(u: AdminUser, mode: 'SOFT' | 'HARD'): void {
    const label = mode === 'HARD' ? 'PERMANENTLY delete' : 'delete';
    if (!window.confirm('Are you sure you want to ' + label + ' ' + u.name + '?')) return;
    this.admin.deleteUser(u.id, mode).subscribe({
      next: () => { this.flash(u.name + ' deleted'); this.load(); },
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

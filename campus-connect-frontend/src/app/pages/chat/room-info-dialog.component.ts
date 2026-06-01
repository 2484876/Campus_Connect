import { Component, EventEmitter, Input, Output, ChangeDetectorRef, OnInit, OnChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../services/api.service';
import { ChatRoomDTO, ChatRoomMemberDTO, ConnectionDTO } from '../../models';
import { PresenceDotComponent } from './presence-dot.component';

@Component({
  selector: 'app-room-info-dialog',
  standalone: true,
  imports: [CommonModule, FormsModule, PresenceDotComponent],
  template: `
    <div class="dlg-backdrop" (click)="close.emit()"></div>
    <div class="dlg">
      <div class="dlg-head">
        <h5>Group info</h5>
        <button class="x" (click)="close.emit()">
          <svg width="14" height="14" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
        </button>
      </div>

      <div class="dlg-body" *ngIf="!loading">
        <div class="group-header">
          <img *ngIf="room?.avatarUrl" [src]="room?.avatarUrl" class="group-avatar">
          <div *ngIf="!room?.avatarUrl" class="group-avatar-fallback">{{ initials(room?.name) }}</div>

          <div class="group-name-block" *ngIf="!editingName">
            <div class="group-name">{{ room?.name }}</div>
            <button *ngIf="isAdmin" class="rename-btn" (click)="startEditName()" title="Rename">
              <svg width="12" height="12" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
            </button>
          </div>
          <div class="group-name-edit" *ngIf="editingName">
            <input [(ngModel)]="newName" maxlength="100" class="name-input">
            <button class="btn-primary sm" (click)="saveName()">Save</button>
            <button class="btn-secondary sm" (click)="editingName = false">Cancel</button>
          </div>

          <div class="group-meta">{{ room?.memberCount }} members · created by {{ room?.createdByName }}</div>
        </div>

        <div class="section-head">
          <span>Members</span>
          <button *ngIf="isAdmin" class="add-btn" (click)="showAddMembers = !showAddMembers">
            <svg width="12" height="12" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
            Add
          </button>
        </div>

        <div class="add-members-panel" *ngIf="showAddMembers && isAdmin">
          <input class="search" [(ngModel)]="addSearch" placeholder="Search connections..." (input)="filterAddable()">
          <div class="addable-list">
            <div class="addable" *ngFor="let p of filteredAddable" (click)="toggleAdd(p.userId)">
              <input type="checkbox" [checked]="toAdd.has(p.userId)">
              <img [src]="p.profilePicUrl || avatar(p.name)" class="sm-avatar">
              <span>{{ p.name }}</span>
            </div>
            <div class="empty" *ngIf="filteredAddable.length === 0">No connections to add.</div>
          </div>
          <div class="add-actions">
            <button class="btn-primary sm" (click)="confirmAddMembers()" [disabled]="toAdd.size === 0">
              Add {{ toAdd.size > 0 ? '(' + toAdd.size + ')' : '' }}
            </button>
          </div>
        </div>

        <div class="members-list">
          <div class="member" *ngFor="let m of room?.members">
            <div class="member-avatar-wrap">
              <img [src]="m.profilePicUrl || avatar(m.name)" class="member-avatar">
              <app-presence-dot [status]="m.presence"></app-presence-dot>
            </div>
            <div class="member-info">
              <div class="member-name">
                {{ m.name }}
                <span class="role-badge" *ngIf="m.role === 'ADMIN'">Admin</span>
              </div>
              <div class="member-pos">{{ m.position }}</div>
            </div>
            <button *ngIf="isAdmin && m.userId !== currentUserId" class="kick-btn" (click)="removeMember(m)" title="Remove">
              <svg width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
            </button>
          </div>
        </div>

        <div class="leave-section">
          <button class="leave-btn" (click)="leave()">
            <svg width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" y1="12" x2="9" y2="12"/></svg>
            Leave group
          </button>
        </div>
      </div>

      <div class="dlg-body" *ngIf="loading">
        <div class="loading">Loading...</div>
      </div>
    </div>
  `,
  styles: [`
    .dlg-backdrop { position: fixed; inset: 0; background: rgba(0,0,0,0.5); z-index: 2000; }
    .dlg {
      position: fixed; top: 50%; left: 50%; transform: translate(-50%, -50%);
      background: #fff; border-radius: 8px; width: 90%; max-width: 480px; max-height: 85vh;
      display: flex; flex-direction: column; z-index: 2001;
      border: 1px solid #1a1a1a;
    }
    .dlg-head { display: flex; align-items: center; justify-content: space-between; padding: 12px 16px; border-bottom: 1px solid #e0e0e0; }
    .dlg-head h5 { margin: 0; font-size: 14px; font-weight: 700; color: #1a1a1a; }
    .x { background: transparent; border: none; cursor: pointer; color: #555; padding: 4px; }
    .x:hover { color: #000; }
    .dlg-body { padding: 16px; flex: 1; overflow-y: auto; }
    .group-header { text-align: center; padding-bottom: 16px; border-bottom: 1px solid #f0f0f0; margin-bottom: 12px; }
    .group-avatar, .group-avatar-fallback {
      width: 70px; height: 70px; border-radius: 50%; margin: 0 auto 10px;
      display: flex; align-items: center; justify-content: center;
      background: #1a1a1a; color: #fff; font-weight: 700; font-size: 22px;
      object-fit: cover;
    }
    .group-name-block { display: inline-flex; align-items: center; gap: 6px; }
    .group-name { font-size: 18px; font-weight: 700; color: #1a1a1a; }
    .rename-btn { background: transparent; border: none; cursor: pointer; color: #666; padding: 4px; border-radius: 4px; }
    .rename-btn:hover { background: #f0f0f0; color: #1a1a1a; }
    .group-name-edit { display: flex; align-items: center; gap: 6px; justify-content: center; }
    .name-input { padding: 5px 8px; border: 1px solid #1a1a1a; border-radius: 4px; font-size: 13px; outline: none; font-family: inherit; }
    .group-meta { font-size: 11px; color: #888; margin-top: 6px; }
    .section-head {
      display: flex; align-items: center; justify-content: space-between;
      font-size: 11px; font-weight: 700; color: #555; text-transform: uppercase; letter-spacing: 0.5px;
      margin: 8px 0;
    }
    .add-btn {
      background: #1a1a1a; color: #fff; border: none; padding: 4px 10px; border-radius: 12px;
      font-size: 10px; font-weight: 700; cursor: pointer; display: inline-flex; align-items: center; gap: 3px;
    }
    .add-btn:hover { background: #000; }
    .add-members-panel { background: #f8f8f8; border-radius: 6px; padding: 10px; margin-bottom: 10px; }
    .search { width: 100%; padding: 6px 10px; border: 1px solid #ddd; border-radius: 4px; font-size: 12px; outline: none; font-family: inherit; }
    .search:focus { border-color: #1a1a1a; }
    .addable-list { max-height: 160px; overflow-y: auto; margin-top: 6px; }
    .addable { display: flex; align-items: center; gap: 8px; padding: 5px 4px; cursor: pointer; border-radius: 4px; }
    .addable:hover { background: #efefef; }
    .sm-avatar { width: 24px; height: 24px; border-radius: 50%; object-fit: cover; }
    .addable span { font-size: 12px; color: #1a1a1a; }
    .empty { font-size: 11px; color: #888; padding: 10px; text-align: center; }
    .add-actions { display: flex; justify-content: flex-end; margin-top: 6px; }
    .members-list { max-height: 280px; overflow-y: auto; }
    .member { display: flex; align-items: center; gap: 10px; padding: 8px 0; border-bottom: 1px solid #f5f5f5; }
    .member-avatar-wrap { position: relative; }
    .member-avatar { width: 36px; height: 36px; border-radius: 50%; object-fit: cover; }
    app-presence-dot { position: absolute; bottom: 0; right: 0; }
    .member-info { flex: 1; }
    .member-name { font-size: 13px; font-weight: 600; color: #1a1a1a; display: flex; align-items: center; gap: 6px; }
    .role-badge { background: #1a1a1a; color: #fff; font-size: 9px; font-weight: 700; padding: 2px 6px; border-radius: 8px; text-transform: uppercase; letter-spacing: 0.5px; }
    .member-pos { font-size: 11px; color: #666; }
    .kick-btn { background: transparent; border: none; cursor: pointer; color: #888; padding: 4px; border-radius: 4px; }
    .kick-btn:hover { background: #fce8e6; color: #c0392b; }
    .leave-section { margin-top: 12px; padding-top: 12px; border-top: 1px solid #f0f0f0; }
    .leave-btn {
      width: 100%; background: transparent; color: #c0392b; border: 1px solid #c0392b;
      padding: 8px 14px; border-radius: 4px; cursor: pointer; font-size: 12px; font-weight: 700;
      display: inline-flex; align-items: center; justify-content: center; gap: 6px; font-family: inherit;
    }
    .leave-btn:hover { background: #c0392b; color: #fff; }
    .btn-primary { background: #1a1a1a; color: #fff; border: none; padding: 6px 14px; border-radius: 4px; cursor: pointer; font-size: 12px; font-weight: 600; font-family: inherit; }
    .btn-primary.sm { padding: 4px 10px; font-size: 11px; }
    .btn-primary:disabled { opacity: 0.5; cursor: not-allowed; }
    .btn-primary:hover:not(:disabled) { background: #000; }
    .btn-secondary { background: #f0f0f0; color: #1a1a1a; border: none; padding: 6px 14px; border-radius: 4px; cursor: pointer; font-size: 12px; font-weight: 600; font-family: inherit; }
    .btn-secondary.sm { padding: 4px 10px; font-size: 11px; }
    .btn-secondary:hover { background: #e0e0e0; }
    .loading { padding: 30px; text-align: center; font-size: 12px; color: #888; }
  `]
})
export class RoomInfoDialogComponent implements OnInit, OnChanges {
  @Input() roomId!: number;
  @Input() currentUserId!: number;
  @Output() close = new EventEmitter<void>();
  @Output() left = new EventEmitter<number>();
  @Output() updated = new EventEmitter<ChatRoomDTO>();

  room: ChatRoomDTO | null = null;
  loading = true;
  editingName = false;
  newName = '';
  showAddMembers = false;
  addSearch = '';
  connections: ConnectionDTO[] = [];
  filteredAddable: any[] = [];
  toAdd = new Set<number>();

  get isAdmin(): boolean { return this.room?.myRole === 'ADMIN'; }

  constructor(private api: ApiService, private cdr: ChangeDetectorRef) { }

  ngOnInit(): void { this.load(); }
  ngOnChanges(): void { this.load(); }

  load(): void {
    if (!this.roomId) return;
    this.loading = true;
    this.api.getRoom(this.roomId).subscribe({
      next: (r) => {
        this.room = r;
        this.loading = false;
        this.cdr.detectChanges();
        this.loadAddable();
      },
      error: () => { this.loading = false; this.cdr.detectChanges(); }
    });
  }

  loadAddable(): void {
    this.api.getConnections(0).subscribe(res => {
      this.connections = res.content;
      this.filterAddable();
    });
  }

  filterAddable(): void {
    const memberIds = new Set((this.room?.members || []).map(m => m.userId));
    const q = this.addSearch.toLowerCase().trim();
    this.filteredAddable = this.connections
      .filter(c => !memberIds.has(c.userId))
      .filter(c => !q || c.userName.toLowerCase().includes(q))
      .map(c => ({
        userId: c.userId,
        name: c.userName,
        profilePicUrl: c.userProfilePic
      }));
    this.cdr.detectChanges();
  }

  toggleAdd(uid: number): void {
    if (this.toAdd.has(uid)) this.toAdd.delete(uid);
    else this.toAdd.add(uid);
    this.cdr.detectChanges();
  }

  confirmAddMembers(): void {
    if (this.toAdd.size === 0) return;
    const ids = Array.from(this.toAdd);
    this.api.addRoomMembers(this.roomId, ids).subscribe({
      next: (r) => {
        this.room = r;
        this.toAdd.clear();
        this.showAddMembers = false;
        this.filterAddable();
        this.updated.emit(r);
        this.cdr.detectChanges();
      },
      error: () => alert('Failed to add members')
    });
  }

  removeMember(m: ChatRoomMemberDTO): void {
    if (!confirm(`Remove ${m.name} from this group?`)) return;
    this.api.removeRoomMember(this.roomId, m.userId).subscribe({
      next: () => {
        if (this.room) {
          this.room.members = (this.room.members || []).filter(x => x.userId !== m.userId);
          this.room.memberCount = (this.room.memberCount || 1) - 1;
          this.updated.emit(this.room);
        }
        this.cdr.detectChanges();
      }
    });
  }

  startEditName(): void {
    this.newName = this.room?.name || '';
    this.editingName = true;
    this.cdr.detectChanges();
  }

  saveName(): void {
    const n = this.newName.trim();
    if (!n) return;
    this.api.renameRoom(this.roomId, n).subscribe({
      next: () => {
        if (this.room) {
          this.room.name = n;
          this.updated.emit(this.room);
        }
        this.editingName = false;
        this.cdr.detectChanges();
      }
    });
  }

  leave(): void {
    if (!confirm('Leave this group?')) return;
    this.api.leaveRoom(this.roomId).subscribe({
      next: () => this.left.emit(this.roomId)
    });
  }

  initials(name: string | undefined): string {
    if (!name) return '?';
    return name.split(/\s+/).map(w => w[0]).slice(0, 2).join('').toUpperCase();
  }

  avatar(name: string): string {
    return `https://ui-avatars.com/api/?name=${encodeURIComponent(name)}&background=1a1a1a&color=fff`;
  }
}

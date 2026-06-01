import { Component, EventEmitter, Output, ChangeDetectorRef, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../services/api.service';
import { ConnectionDTO, ChatRoomDTO } from '../../models';

@Component({
  selector: 'app-new-chat-dialog',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="dlg-backdrop" (click)="close.emit()"></div>
    <div class="dlg">
      <div class="dlg-head">
        <h5>{{ step === 'pick' ? 'New chat' : 'Name your group' }}</h5>
        <button class="x" (click)="close.emit()">
          <svg width="14" height="14" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
        </button>
      </div>

      <div class="dlg-body" *ngIf="step === 'pick'">
        <input class="search" [(ngModel)]="search" placeholder="Search connections..." (input)="filter()">

        <div class="hint" *ngIf="selected.size === 1">Select 1+ more for a group chat, or click Start to DM.</div>
        <div class="hint" *ngIf="selected.size >= 2">Group chat with {{ selected.size }} people.</div>

        <div class="people-list">
          <div class="person" *ngFor="let p of filtered" (click)="toggle(p)">
            <input type="checkbox" [checked]="selected.has(p.userId)">
            <img [src]="p.profilePicUrl || avatar(p.name)" class="avatar">
            <div class="info">
              <div class="name">{{ p.name }}</div>
              <div class="pos">{{ p.position }}</div>
            </div>
          </div>
          <div class="empty" *ngIf="filtered.length === 0 && !loading">No connections found.</div>
          <div class="empty" *ngIf="loading">Loading...</div>
        </div>
      </div>

      <div class="dlg-body" *ngIf="step === 'name'">
        <label class="lbl">Group name</label>
        <input class="search" [(ngModel)]="groupName" placeholder="e.g. Project Phoenix" maxlength="100">
        <div class="member-preview">
          <div class="mp-title">{{ selected.size }} members</div>
          <div class="mp-chips">
            <span class="chip" *ngFor="let p of selectedList()">{{ p.name }}</span>
          </div>
        </div>
      </div>

      <div class="dlg-foot">
        <button class="btn-secondary" *ngIf="step === 'name'" (click)="step = 'pick'">Back</button>
        <button class="btn-secondary" (click)="close.emit()">Cancel</button>

        <button *ngIf="step === 'pick' && selected.size === 1" class="btn-primary" (click)="startDM()">
          Start chat
        </button>
        <button *ngIf="step === 'pick' && selected.size >= 2" class="btn-primary" (click)="step = 'name'">
          Next
        </button>
        <button *ngIf="step === 'name'" class="btn-primary" (click)="createGroup()" [disabled]="!groupName.trim() || creating">
          {{ creating ? 'Creating...' : 'Create group' }}
        </button>
      </div>
    </div>
  `,
  styles: [`
    .dlg-backdrop { position: fixed; inset: 0; background: rgba(0,0,0,0.5); z-index: 2000; }
    .dlg {
      position: fixed; top: 50%; left: 50%; transform: translate(-50%, -50%);
      background: #fff; border-radius: 8px; width: 90%; max-width: 460px; max-height: 80vh;
      display: flex; flex-direction: column; z-index: 2001;
      border: 1px solid #1a1a1a;
    }
    .dlg-head {
      display: flex; align-items: center; justify-content: space-between;
      padding: 12px 16px; border-bottom: 1px solid #e0e0e0;
    }
    .dlg-head h5 { margin: 0; font-size: 14px; font-weight: 700; color: #1a1a1a; }
    .x { background: transparent; border: none; cursor: pointer; color: #555; padding: 4px; }
    .x:hover { color: #000; }
    .dlg-body { padding: 12px 16px; flex: 1; overflow-y: auto; }
    .search {
      width: 100%; padding: 8px 12px; border: 1px solid #ddd; border-radius: 4px;
      font-size: 13px; outline: none; font-family: inherit;
    }
    .search:focus { border-color: #1a1a1a; }
    .hint { font-size: 11px; color: #666; margin: 8px 0; }
    .lbl { display: block; font-size: 11px; font-weight: 700; color: #555; text-transform: uppercase; letter-spacing: 0.5px; margin-bottom: 4px; }
    .people-list { margin-top: 10px; max-height: 320px; overflow-y: auto; }
    .person {
      display: flex; align-items: center; gap: 10px; padding: 8px 4px;
      cursor: pointer; border-radius: 4px;
    }
    .person:hover { background: #f6f6f6; }
    .person input[type=checkbox] { cursor: pointer; }
    .avatar { width: 32px; height: 32px; border-radius: 50%; object-fit: cover; background: #eee; }
    .info .name { font-size: 12px; font-weight: 600; color: #1a1a1a; }
    .info .pos { font-size: 10px; color: #666; }
    .empty { padding: 20px; text-align: center; font-size: 12px; color: #888; }
    .member-preview { margin-top: 12px; }
    .mp-title { font-size: 11px; color: #666; margin-bottom: 6px; }
    .mp-chips { display: flex; flex-wrap: wrap; gap: 4px; }
    .chip { font-size: 11px; padding: 3px 8px; background: #1a1a1a; color: #fff; border-radius: 12px; }
    .dlg-foot {
      display: flex; gap: 8px; justify-content: flex-end; padding: 12px 16px;
      border-top: 1px solid #e0e0e0;
    }
    .btn-primary, .btn-secondary {
      padding: 6px 14px; border: none; border-radius: 4px; cursor: pointer;
      font-size: 12px; font-weight: 600; font-family: inherit;
    }
    .btn-primary { background: #1a1a1a; color: #fff; }
    .btn-primary:hover:not(:disabled) { background: #000; }
    .btn-primary:disabled { opacity: 0.5; cursor: not-allowed; }
    .btn-secondary { background: #f0f0f0; color: #1a1a1a; }
    .btn-secondary:hover { background: #e0e0e0; }
  `]
})
export class NewChatDialogComponent implements OnInit {
  @Output() close = new EventEmitter<void>();
  @Output() dmCreated = new EventEmitter<number>();
  @Output() groupCreated = new EventEmitter<ChatRoomDTO>();

  connections: ConnectionDTO[] = [];
  filtered: any[] = [];
  selected = new Set<number>();
  selectedMap = new Map<number, any>();
  search = '';
  step: 'pick' | 'name' = 'pick';
  groupName = '';
  loading = true;
  creating = false;

  constructor(private api: ApiService, private cdr: ChangeDetectorRef) { }

  ngOnInit(): void {
    this.api.getConnections(0).subscribe({
      next: (res) => {
        this.connections = res.content;
        this.filtered = res.content.map(c => this.toUserView(c));
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: () => { this.loading = false; this.cdr.detectChanges(); }
    });
  }

  toUserView(c: ConnectionDTO): any {
    return {
      userId: c.userId,
      name: c.userName,
      profilePicUrl: c.userProfilePic,
      position: c.userPosition
    };
  }

  filter(): void {
    const q = this.search.toLowerCase().trim();
    const all = this.connections.map(c => this.toUserView(c));
    this.filtered = q
      ? all.filter(p => p.name.toLowerCase().includes(q))
      : all;
    this.cdr.detectChanges();
  }

  toggle(p: any): void {
    if (this.selected.has(p.userId)) {
      this.selected.delete(p.userId);
      this.selectedMap.delete(p.userId);
    } else {
      this.selected.add(p.userId);
      this.selectedMap.set(p.userId, p);
    }
    this.cdr.detectChanges();
  }

  selectedList(): any[] {
    return Array.from(this.selectedMap.values());
  }

  startDM(): void {
    const otherId = Array.from(this.selected)[0];
    this.dmCreated.emit(otherId);
  }

  createGroup(): void {
    if (!this.groupName.trim() || this.creating) return;
    this.creating = true;
    this.api.createRoom(this.groupName.trim(), Array.from(this.selected)).subscribe({
      next: (room) => {
        this.creating = false;
        this.groupCreated.emit(room);
      },
      error: () => {
        this.creating = false;
        alert('Failed to create group');
        this.cdr.detectChanges();
      }
    });
  }

  avatar(name: string): string {
    return `https://ui-avatars.com/api/?name=${encodeURIComponent(name)}&background=1a1a1a&color=fff`;
  }
}

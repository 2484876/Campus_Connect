import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule, ActivatedRoute } from '@angular/router';
import { ApiService } from '../../services/api.service';
import { ConnectionDTO, UserDTO, ConnectionSuggestionDTO } from '../../models';
import { ConnectRequestDialogComponent } from '../../components/connect-request-dialog/connect-request-dialog.component';

type Tab = 'connections' | 'pending' | 'sent' | 'suggestions' | 'search';

@Component({
  selector: 'app-connections',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, ConnectRequestDialogComponent],
  templateUrl: './connections.html',
  styleUrl: './connections.scss'
})
export class ConnectionsComponent implements OnInit {
  connections: ConnectionDTO[] = [];
  pending: ConnectionDTO[] = [];
  sent: ConnectionDTO[] = [];
  suggestions: ConnectionSuggestionDTO[] = [];
  searchResults: UserDTO[] = [];

  searchQuery = '';
  activeTab: Tab = 'connections';
  loading = true;

  dialogUser: { userId: number, name: string, profilePicUrl: string | null, position: string | null } | null = null;

  constructor(
    private api: ApiService,
    private route: ActivatedRoute,
    private cdr: ChangeDetectorRef
  ) { }

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      if (params['q']) {
        this.searchQuery = params['q'];
        this.activeTab = 'search';
        this.onSearch();
      }
    });
    this.loadAll();
  }

  setTab(t: Tab): void {
    this.activeTab = t;
  }

  loadAll(): void {
    this.loading = true;
    this.api.getConnections().subscribe({
      next: (res) => { this.connections = res.content; this.loading = false; this.cdr.detectChanges(); },
      error: () => { this.loading = false; this.cdr.detectChanges(); }
    });
    this.api.getPendingRequests().subscribe(res => {
      this.pending = res.content;
      this.cdr.detectChanges();
    });
    this.api.getSentRequests().subscribe({
      next: (res) => { this.sent = res.content; this.cdr.detectChanges(); },
      error: () => { }
    });
    this.api.getConnectionSuggestions(20).subscribe({
      next: (list) => { this.suggestions = list || []; this.cdr.detectChanges(); },
      error: () => { }
    });
  }

  onSearch(): void {
    if (!this.searchQuery.trim()) {
      this.searchResults = [];
      this.cdr.detectChanges();
      return;
    }
    this.activeTab = 'search';
    this.api.searchUsers(this.searchQuery).subscribe(res => {
      this.searchResults = res.content;
      this.cdr.detectChanges();
    });
  }

  accept(c: ConnectionDTO): void {
    this.api.acceptConnection(c.connectionId).subscribe(() => {
      this.pending = this.pending.filter(p => p.connectionId !== c.connectionId);
      c.status = 'ACCEPTED';
      this.connections.unshift(c);
      this.cdr.detectChanges();
    });
  }

  reject(c: ConnectionDTO): void {
    this.api.rejectConnection(c.connectionId).subscribe(() => {
      this.pending = this.pending.filter(p => p.connectionId !== c.connectionId);
      this.cdr.detectChanges();
    });
  }

  withdraw(c: ConnectionDTO): void {
    if (!confirm('Withdraw your connection request to ' + c.userName + '?')) return;
    this.api.withdrawConnection(c.connectionId).subscribe(() => {
      this.sent = this.sent.filter(s => s.connectionId !== c.connectionId);
      this.cdr.detectChanges();
    });
  }

  remove(c: ConnectionDTO, index: number): void {
    if (!confirm('Remove connection with ' + c.userName + '?')) return;
    this.api.removeConnection(c.connectionId).subscribe(() => {
      this.connections.splice(index, 1);
      this.cdr.detectChanges();
    });
  }

  openConnectDialog(user: { userId: number, name: string, profilePicUrl: string | null, position: string | null }): void {
    this.dialogUser = user;
  }

  closeDialog(): void {
    this.dialogUser = null;
  }

  onConnectionSent(c: ConnectionDTO): void {
    // remove from suggestions, add to sent
    if (this.dialogUser) {
      this.suggestions = this.suggestions.filter(s => s.userId !== this.dialogUser!.userId);
      // also clear from search results visually
      this.searchResults = this.searchResults.map(u =>
        u.id === this.dialogUser!.userId ? { ...u, connectionStatus: 'PENDING' } : u
      );
    }
    this.sent.unshift(c);
    this.dialogUser = null;
    this.cdr.detectChanges();
  }

  sendQuickRequest(user: UserDTO): void {
    this.openConnectDialog({
      userId: user.id,
      name: user.name,
      profilePicUrl: user.profilePicUrl || null,
      position: user.position
    });
  }

  sendFromSuggestion(s: ConnectionSuggestionDTO): void {
    this.openConnectDialog({
      userId: s.userId,
      name: s.name,
      profilePicUrl: s.profilePicUrl,
      position: s.position
    });
  }

  getAvatar(name: string, url?: string | null): string {
    return url || `https://ui-avatars.com/api/?name=${encodeURIComponent(name)}&background=2d5f3f&color=fff`;
  }

  timeAgo(dateStr: string): string {
    const s = Math.floor((Date.now() - new Date(dateStr).getTime()) / 1000);
    if (s < 60) return 'just now';
    if (s < 3600) return Math.floor(s / 60) + 'm';
    if (s < 86400) return Math.floor(s / 3600) + 'h';
    if (s < 2592000) return Math.floor(s / 86400) + 'd';
    return Math.floor(s / 2592000) + 'mo';
  }
}

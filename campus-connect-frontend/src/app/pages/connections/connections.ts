import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule, ActivatedRoute } from '@angular/router';
import { ApiService } from '../../services/api.service';
import { ConnectionDTO, UserDTO } from '../../models';

@Component({
  selector: 'app-connections',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './connections.html',
  styleUrl: './connections.scss'
})
export class ConnectionsComponent implements OnInit {
  connections: ConnectionDTO[] = [];
  pending: ConnectionDTO[] = [];
  suggestions: UserDTO[] = [];
  searchResults: UserDTO[] = [];
  searchQuery = '';
  activeTab = 'connections';
  loading = true;

  constructor(
    private api: ApiService,
    private route: ActivatedRoute,
    private cdr: ChangeDetectorRef
  ) { }

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      if (params['q']) {
        this.searchQuery = params['q'];
        this.onSearch();
      }
    });
    this.loadAll();
  }

  loadAll(): void {
    this.loading = true;
    this.api.getConnections().subscribe(res => {
      this.connections = res.content;
      this.loading = false;
      this.cdr.detectChanges();
    });
    this.api.getPendingRequests().subscribe(res => {
      this.pending = res.content;
      this.cdr.detectChanges();
    });
    this.api.getSuggestions().subscribe(res => {
      this.suggestions = res.content;
      this.cdr.detectChanges();
    });
  }

  onSearch(): void {
    if (!this.searchQuery.trim()) {
      this.searchResults = [];
      this.cdr.detectChanges();
      return;
    }
    this.api.searchUsers(this.searchQuery).subscribe(res => {
      this.searchResults = res.content;
      this.cdr.detectChanges();
    });
  }

  accept(c: ConnectionDTO): void {
    this.api.acceptConnection(c.connectionId).subscribe(() => {
      this.pending = this.pending.filter(p => p.connectionId !== c.connectionId);
      c.status = 'ACCEPTED';
      this.connections.push(c);
      this.cdr.detectChanges();
    });
  }

  reject(c: ConnectionDTO): void {
    this.api.rejectConnection(c.connectionId).subscribe(() => {
      this.pending = this.pending.filter(p => p.connectionId !== c.connectionId);
      this.cdr.detectChanges();
    });
  }

  sendRequest(user: UserDTO): void {
    this.api.sendConnectionRequest(user.id).subscribe(() => {
      user.connectionStatus = 'PENDING';
      this.cdr.detectChanges();
    });
  }

  remove(c: ConnectionDTO, index: number): void {
    if (confirm('Remove connection with ' + c.userName + '?')) {
      this.api.removeConnection(c.connectionId).subscribe(() => {
        this.connections.splice(index, 1);
        this.cdr.detectChanges();
      });
    }
  }

  getAvatar(name: string): string {
    return `https://ui-avatars.com/api/?name=${encodeURIComponent(name)}&background=0a66c2&color=fff`;
  }

  getRoleBadgeClass(role: string): string {
    return 'badge-role badge-' + role.toLowerCase();
  }
}

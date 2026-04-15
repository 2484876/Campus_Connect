import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { ApiService } from '../../services/api.service';
import { WebSocketService } from '../../services/websocket.service';
import { NotificationStateService } from '../../services/notification-state.service';
import { NotificationDTO } from '../../models';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-notifications',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './notifications.component.html',
  styleUrls: ['./notifications.component.scss']
})
export class NotificationsComponent implements OnInit, OnDestroy {
  notifications: NotificationDTO[] = [];
  loading = true;
  page = 0;
  isLast = false;
  private wsSub?: Subscription;

  constructor(
    private api: ApiService,
    private ws: WebSocketService,
    private notifState: NotificationStateService,
    private cdr: ChangeDetectorRef
  ) { }

  ngOnInit(): void {
    this.loadNotifications();
    this.markAllRead();

    this.wsSub = this.ws.newNotification$.subscribe(notif => {
      notif.isRead = false;
      this.notifications.unshift(notif);
      this.cdr.detectChanges();
    });
  }

  ngOnDestroy(): void {
    this.wsSub?.unsubscribe();
  }

  loadNotifications(): void {
    this.loading = true;
    this.api.getNotifications(this.page).subscribe({
      next: (res) => {
        this.notifications = this.page === 0 ? res.content : [...this.notifications, ...res.content];
        this.isLast = res.last;
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  loadMore(): void {
    if (this.isLast || this.loading) return;
    this.page++;
    this.loadNotifications();
  }

  markAllRead(): void {
    this.api.markNotificationsRead().subscribe({
      next: () => {
        this.notifState.clear();
        this.notifications.forEach(n => n.isRead = true);
        this.cdr.detectChanges();
      }
    });
  }

  getAvatar(name: string): string {
    return `https://ui-avatars.com/api/?name=${encodeURIComponent(name)}&background=0a66c2&color=fff`;
  }

  getIcon(type: string): string {
    switch (type) {
      case 'LIKE': return 'like';
      case 'COMMENT': return 'comment';
      case 'CONNECTION_REQUEST': return 'connect';
      case 'CONNECTION_ACCEPTED': return 'accepted';
      case 'MESSAGE': return 'message';
      default: return 'default';
    }
  }

  getLink(notif: NotificationDTO): string[] {
    switch (notif.type) {
      case 'LIKE':
      case 'COMMENT':
        return ['/feed'];
      case 'CONNECTION_REQUEST':
      case 'CONNECTION_ACCEPTED':
        return ['/profile', notif.actorId.toString()];
      case 'MESSAGE':
        return ['/chat', notif.actorId.toString()];
      default:
        return ['/feed'];
    }
  }

  timeAgo(dateStr: string): string {
    const s = Math.floor((Date.now() - new Date(dateStr).getTime()) / 1000);
    if (s < 60) return 'just now';
    if (s < 3600) return Math.floor(s / 60) + 'm ago';
    if (s < 86400) return Math.floor(s / 3600) + 'h ago';
    if (s < 604800) return Math.floor(s / 86400) + 'd ago';
    return Math.floor(s / 2592000) + 'mo ago';
  }
}

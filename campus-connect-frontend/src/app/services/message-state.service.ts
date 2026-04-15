import { Injectable, OnDestroy } from '@angular/core';
import { BehaviorSubject, Subscription } from 'rxjs';
import { ApiService } from './api.service';
import { WebSocketService } from './websocket.service';
import { AuthService } from './auth.service';

@Injectable({ providedIn: 'root' })
export class MessageStateService implements OnDestroy {

  public unreadCount$ = new BehaviorSubject<number>(0);
  public activeChatUserId: number | null = null;
  private currentUserId: number;
  private wsSub?: Subscription;
  private initialized = false;

  constructor(
    private api: ApiService,
    private ws: WebSocketService,
    private auth: AuthService
  ) {
    this.currentUserId = this.auth.getCurrentUser()?.userId || 0;
  }

  init(): void {
    if (this.initialized) return;
    this.initialized = true;
    this.refreshCount();

    this.wsSub = this.ws.newMessage$.subscribe(msg => {
      if (msg.senderId !== this.currentUserId && !this.isActiveChat(msg.senderId)) {
        this.increment();
      }
    });
  }

  ngOnDestroy(): void {
    this.wsSub?.unsubscribe();
  }

  refreshCount(): void {
    this.api.getConversations().subscribe({
      next: (convos) => {
        const total = convos.reduce((sum, c) => sum + (c.unreadCount || 0), 0);
        this.unreadCount$.next(total);
      },
      error: () => this.unreadCount$.next(0)
    });
  }

  increment(): void {
    this.unreadCount$.next(this.unreadCount$.value + 1);
  }

  decrement(count: number): void {
    const current = this.unreadCount$.value;
    this.unreadCount$.next(Math.max(0, current - count));
  }

  setActiveChat(userId: number | null): void {
    this.activeChatUserId = userId;
  }

  isActiveChat(userId: number): boolean {
    return this.activeChatUserId === userId;
  }
}

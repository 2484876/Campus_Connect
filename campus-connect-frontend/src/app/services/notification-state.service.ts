import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { ApiService } from './api.service';

@Injectable({ providedIn: 'root' })
export class NotificationStateService {

  public unreadCount$ = new BehaviorSubject<number>(0);

  constructor(private api: ApiService) { }

  refreshCount(): void {
    this.api.getUnreadCount().subscribe({
      next: (res) => this.unreadCount$.next(res.count ?? res),
      error: () => this.unreadCount$.next(0)
    });
  }

  increment(): void {
    this.unreadCount$.next(this.unreadCount$.value + 1);
  }

  clear(): void {
    this.unreadCount$.next(0);
  }
}

import { Component, OnInit, OnDestroy, ChangeDetectorRef, HostListener, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { ApiService } from '../../services/api.service';
import { WebSocketService } from '../../services/websocket.service';
import { NotificationStateService } from '../../services/notification-state.service';
import { MessageStateService } from '../../services/message-state.service';
import { Router } from '@angular/router';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.scss']
})
export class NavbarComponent implements OnInit, OnDestroy {
  user: any;
  unreadCount = 0;
  messageCount = 0;
  searchQuery = '';
  userProfilePic = '';
  showDropdown = false;
  private notifSub?: Subscription;
  private countSub?: Subscription;
  private msgCountSub?: Subscription;

  constructor(
    public authService: AuthService,
    private api: ApiService,
    private ws: WebSocketService,
    private notifState: NotificationStateService,
    private msgState: MessageStateService,
    private router: Router,
    private cdr: ChangeDetectorRef,
    private elementRef: ElementRef
  ) { }

  ngOnInit(): void {
    this.user = this.authService.getCurrentUser();

    this.notifState.refreshCount();
    this.msgState.refreshCount();

    this.countSub = this.notifState.unreadCount$.subscribe(count => {
      this.unreadCount = count;
      this.cdr.detectChanges();
    });

    this.msgCountSub = this.msgState.unreadCount$.subscribe(count => {
      this.messageCount = count;
      this.cdr.detectChanges();
    });

    if (this.user) {
      this.api.getProfile().subscribe(profile => {
        this.userProfilePic = profile.profilePicUrl;
        this.cdr.detectChanges();
      });
    }

    this.notifSub = this.ws.newNotification$.subscribe(() => {
      this.notifState.increment();
    });
  }

  ngOnDestroy(): void {
    this.notifSub?.unsubscribe();
    this.countSub?.unsubscribe();
    this.msgCountSub?.unsubscribe();
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: Event): void {
    if (this.showDropdown) {
      const clickedInside = this.elementRef.nativeElement
        .querySelector('.profile-trigger')
        ?.contains(event.target as Node);
      if (!clickedInside) {
        this.showDropdown = false;
        this.cdr.detectChanges();
      }
    }
  }

  onSearch(): void {
    if (this.searchQuery.trim()) {
      this.router.navigate(['/connections'], { queryParams: { q: this.searchQuery } });
    }
  }

  toggleDropdown(event: Event): void {
    event.stopPropagation();
    this.showDropdown = !this.showDropdown;
    this.cdr.detectChanges();
  }

  logout(): void {
    this.ws.disconnect();
    this.authService.logout();
  }
}

import { Component, Input, Output, EventEmitter, OnInit, OnDestroy, ChangeDetectorRef, ElementRef, ViewChild, AfterViewChecked } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { ApiService } from '../../services/api.service';
import { AuthService } from '../../services/auth.service';
import { EventChatMessageDTO } from '../../models';

@Component({
  selector: 'app-event-chat',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './event-chat.component.html',
  styleUrls: ['./event-chat.component.scss']
})
export class EventChatComponent implements OnInit, OnDestroy, AfterViewChecked {
  @Input() eventId!: number;
  @Output() close = new EventEmitter<void>();
  @ViewChild('scrollBox') scrollBox!: ElementRef<HTMLDivElement>;

  messages: EventChatMessageDTO[] = [];
  draft = '';
  sending = false;
  loading = true;
  private pollTimer: any = null;
  private shouldScrollBottom = true;
  myUserId: number;

  constructor(
    private api: ApiService,
    private auth: AuthService,
    private cdr: ChangeDetectorRef
  ) {
    this.myUserId = this.auth.getCurrentUser()?.userId || 0;
  }

  ngOnInit(): void {
    this.loadMessages(true);
    this.pollTimer = setInterval(() => this.loadMessages(false), 4000);
  }

  ngOnDestroy(): void {
    if (this.pollTimer) clearInterval(this.pollTimer);
  }

  ngAfterViewChecked(): void {
    if (this.shouldScrollBottom && this.scrollBox?.nativeElement) {
      this.scrollBox.nativeElement.scrollTop = this.scrollBox.nativeElement.scrollHeight;
      this.shouldScrollBottom = false;
    }
  }

  loadMessages(initial: boolean): void {
    this.api.getEventChatMessages(this.eventId, 0, 100).subscribe({
      next: (page) => {
        const newCount = page.content.length;
        const wasAtBottom = this.isAtBottom();
        const oldLength = this.messages.length;
        this.messages = page.content;
        if (initial || newCount > oldLength) {
          if (initial || wasAtBottom) this.shouldScrollBottom = true;
        }
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: () => { this.loading = false; this.cdr.detectChanges(); }
    });
  }

  isAtBottom(): boolean {
    const el = this.scrollBox?.nativeElement;
    if (!el) return true;
    return (el.scrollHeight - el.scrollTop - el.clientHeight) < 100;
  }

  send(): void {
    const txt = this.draft.trim();
    if (!txt || this.sending) return;
    this.sending = true;
    const myDraft = txt;
    this.draft = '';
    this.api.sendEventChatMessage(this.eventId, myDraft).subscribe({
      next: (msg) => {
        if (!this.messages.find(m => m.id === msg.id)) this.messages.push(msg);
        this.shouldScrollBottom = true;
        this.sending = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.draft = myDraft;
        this.sending = false;
        this.cdr.detectChanges();
      }
    });
  }

  onKey(e: KeyboardEvent): void {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      this.send();
    }
  }

  avatarFor(m: EventChatMessageDTO): string {
    return m.userProfilePic || `https://ui-avatars.com/api/?name=${encodeURIComponent(m.userName)}&background=2d5f3f&color=fff`;
  }

  isMine(m: EventChatMessageDTO): boolean { return m.userId === this.myUserId; }

  formatTime(t: string): string {
    return new Date(t).toLocaleTimeString('en-IN', { hour: '2-digit', minute: '2-digit' });
  }

  shouldShowAvatar(idx: number): boolean {
    if (idx === 0) return true;
    const prev = this.messages[idx - 1];
    const cur = this.messages[idx];
    if (prev.userId !== cur.userId) return true;
    const gap = new Date(cur.createdAt).getTime() - new Date(prev.createdAt).getTime();
    return gap > 5 * 60 * 1000;
  }
}

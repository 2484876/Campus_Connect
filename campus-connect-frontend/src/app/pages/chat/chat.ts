import { Component, OnInit, OnDestroy, ViewChild, ElementRef, ChangeDetectorRef, ChangeDetectionStrategy, NgZone } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { ApiService } from '../../services/api.service';
import { AuthService } from '../../services/auth.service';
import { WebSocketService } from '../../services/websocket.service';
import { MessageStateService } from '../../services/message-state.service';
import { ConversationDTO, MessageDTO, ReadReceiptDTO, TypingDTO, MessageDeleteDTO, ReactionNotificationDTO, ReactionDTO } from '../../models';
import { Subscription } from 'rxjs';

export interface GroupedReaction {
  emoji: string;
  count: number;
  users: string[];
  reactedByMe: boolean;
}

@Component({
  selector: 'app-chat',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './chat.html',
  styleUrls: ['./chat.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ChatComponent implements OnInit, OnDestroy {
  conversations: ConversationDTO[] = [];
  messages: MessageDTO[] = [];
  selectedUserId: number | null = null;
  selectedUserName = '';
  selectedUserPic = '';
  newMessage = '';
  currentUserId: number;
  loading = true;
  showNewMsgBtn = false;
  typingUserName = '';
  isOtherTyping = false;
  contextMenuMsgId: number | null = null;
  contextMenuX = 0;
  contextMenuY = 0;
  contextMenuIsMine = false;
  contextMenuCanDeleteForAll = false;
  contextMenuIsDeleted = false;
  replyingTo: MessageDTO | null = null;

  emojiPickerMsgId: number | null = null;
  quickEmojis: string[] = ['👍', '❤️', '😂', '😮', '😢', '🎉'];
  groupedReactionsCache: Map<number, GroupedReaction[]> = new Map();
  emojiPickerX = 0;
  emojiPickerY = 0;

  private subs: Subscription[] = [];
  private isNearBottom = true;
  private typingStopTimeout: any = null;
  private typingReceiverTimeout: any = null;
  private typingInterval: any = null;
  private isSendingTyping = false;

  @ViewChild('msgContainer') private msgContainer!: ElementRef<HTMLDivElement>;
  @ViewChild('msgInput') private msgInput!: ElementRef<HTMLInputElement>;

  constructor(
    private api: ApiService,
    private auth: AuthService,
    private ws: WebSocketService,
    private msgState: MessageStateService,
    private route: ActivatedRoute,
    private cdr: ChangeDetectorRef,
    private zone: NgZone
  ) {
    this.currentUserId = this.auth.getCurrentUser()?.userId || 0;
  }

  ngOnInit(): void {
    this.api.getConversations().subscribe(convos => {
      this.conversations = convos;
      this.loading = false;
      this.cdr.markForCheck();
    });

    this.subs.push(
      this.route.params.subscribe(params => {
        if (params['userId']) {
          const uid = +params['userId'];
          this.openChat(uid);
          this.api.getUser(uid).subscribe(u => {
            this.selectedUserName = u.name;
            this.selectedUserPic = u.profilePicUrl || this.avatar(u.name);
            this.cdr.markForCheck();
          });
        }
      }),
      this.ws.newMessage$.subscribe(msg => this.handleIncoming(msg)),
      this.ws.readReceipt$.subscribe(receipt => this.handleReadReceipt(receipt)),
      this.ws.typing$.subscribe(typing => this.handleTyping(typing)),
      this.ws.messageDeleted$.subscribe(del => this.handleMessageDeleted(del)),
      this.ws.reaction$.subscribe(reaction => this.handleReactionNotification(reaction))
    );
  }

  ngOnDestroy(): void {
    this.subs.forEach(s => s.unsubscribe());
    this.msgState.setActiveChat(null);
    if (this.typingStopTimeout) clearTimeout(this.typingStopTimeout);
    if (this.typingReceiverTimeout) clearTimeout(this.typingReceiverTimeout);
    if (this.typingInterval) clearInterval(this.typingInterval);
  }

  selectConversation(c: ConversationDTO): void {
    this.selectedUserName = c.userName;
    this.selectedUserPic = c.userProfilePic || this.avatar(c.userName);
    this.openChat(c.userId);
    this.markRead(c);
  }

  send(): void {
    const text = this.newMessage.trim();
    if (!text || !this.selectedUserId) return;

    this.stopTyping();
    const targetUserId = this.selectedUserId;
    const replyToId = this.replyingTo?.id || null;
    this.newMessage = '';
    this.replyingTo = null;
    this.showNewMsgBtn = false;
    this.cdr.markForCheck();

    this.api.sendMessageWithReply(targetUserId, text, replyToId).subscribe(msg => {
      this.updateConvoPreview(targetUserId, msg.content, msg.createdAt);
      this.cdr.markForCheck();
    });
  }

  onInputChange(): void {
    if (this.newMessage.trim() && this.selectedUserId) {
      if (!this.isSendingTyping) {
        this.isSendingTyping = true;
        this.ws.sendTyping(this.selectedUserId, true);
        this.typingInterval = setInterval(() => {
          if (this.selectedUserId && this.isSendingTyping) {
            this.ws.sendTyping(this.selectedUserId, true);
          }
        }, 1500);
      }
      if (this.typingStopTimeout) clearTimeout(this.typingStopTimeout);
      this.typingStopTimeout = setTimeout(() => {
        this.stopTyping();
      }, 2500);
    } else {
      this.stopTyping();
    }
  }

  replyToContextMsg(): void {
    const msg = this.messages.find(m => m.id === this.contextMenuMsgId);
    if (msg) this.setReply(msg);
  }

  setReply(msg: MessageDTO): void {
    if (msg.deleted) return;
    this.replyingTo = msg;
    this.closeContextMenu();
    this.cdr.markForCheck();
    setTimeout(() => this.msgInput?.nativeElement?.focus(), 50);
  }

  cancelReply(): void {
    this.replyingTo = null;
    this.cdr.markForCheck();
  }

  scrollToMessage(msgId: number | null): void {
    if (!msgId) return;
    const el = document.getElementById('msg-' + msgId);
    if (el) {
      el.scrollIntoView({ behavior: 'smooth', block: 'center' });
      el.classList.add('highlight');
      setTimeout(() => el.classList.remove('highlight'), 1500);
    }
  }

  goBack(): void {
    this.stopTyping();
    this.selectedUserId = null;
    this.selectedUserName = '';
    this.messages = [];
    this.showNewMsgBtn = false;
    this.isOtherTyping = false;
    this.contextMenuMsgId = null;
    this.replyingTo = null;
    this.emojiPickerMsgId = null;
    this.groupedReactionsCache.clear();
    this.msgState.setActiveChat(null);
    this.cdr.markForCheck();
  }

  onScroll(): void {
    const el = this.msgContainer?.nativeElement;
    if (!el) return;
    this.isNearBottom = (el.scrollHeight - el.scrollTop - el.clientHeight) < 120;
    if (this.isNearBottom && this.showNewMsgBtn) {
      this.showNewMsgBtn = false;
      this.cdr.markForCheck();
    }
  }

  scrollToNew(): void {
    this.showNewMsgBtn = false;
    this.instantScroll();
    this.cdr.markForCheck();
  }

  onRightClick(event: MouseEvent, msg: MessageDTO): void {
    event.preventDefault();
    const isMine = msg.senderId === this.currentUserId;
    const isDeleted = msg.deleted;
    const ageMs = Date.now() - new Date(msg.createdAt).getTime();
    const withinOneHour = ageMs < 3600000;

    this.contextMenuIsMine = isMine;
    this.contextMenuIsDeleted = isDeleted;
    this.contextMenuCanDeleteForAll = isMine && !isDeleted && withinOneHour;
    this.contextMenuMsgId = msg.id;

    const menuWidth = 220;
    const menuHeight = isDeleted ? 50 : (this.contextMenuCanDeleteForAll ? 150 : 100);
    const viewW = window.innerWidth;
    const viewH = window.innerHeight;

    let x = event.clientX;
    let y = event.clientY;

    if (x + menuWidth > viewW) x = viewW - menuWidth - 10;
    if (x < 10) x = 10;
    if (y + menuHeight > viewH) y = viewH - menuHeight - 10;
    if (y < 10) y = 10;

    this.contextMenuX = x;
    this.contextMenuY = y;
    this.cdr.markForCheck();
  }

  closeContextMenu(): void {
    this.contextMenuMsgId = null;
    this.cdr.markForCheck();
  }

  deleteForMe(msgId: number): void {
    this.closeContextMenu();
    this.api.deleteMessage(msgId, 'FOR_ME').subscribe(() => {
      this.messages = this.messages.filter(m => m.id !== msgId);
      this.groupedReactionsCache.delete(msgId);
      this.cdr.markForCheck();
    });
  }

  deleteForEveryone(msgId: number): void {
    this.closeContextMenu();
    this.api.deleteMessage(msgId, 'FOR_EVERYONE').subscribe({
      error: (err: any) => {
        alert(err.error?.error || 'Cannot delete this message');
      }
    });
  }

  toggleEmojiPicker(msgId: number, event: MouseEvent): void {
    event.stopPropagation();
    if (this.emojiPickerMsgId === msgId) {
      this.emojiPickerMsgId = null;
      this.cdr.markForCheck();
      return;
    }
    this.emojiPickerMsgId = msgId;

    const btn = event.currentTarget as HTMLElement;
    const rect = btn.getBoundingClientRect();
    const pickerWidth = 210;
    const pickerHeight = 40;

    let x = rect.left + rect.width / 2 - pickerWidth / 2;
    let y = rect.top - pickerHeight - 6;

    if (x + pickerWidth > window.innerWidth - 8) {
      x = window.innerWidth - pickerWidth - 8;
    }
    if (x < 8) x = 8;

    if (y < 8) {
      y = rect.bottom + 6;
    }

    this.emojiPickerX = x;
    this.emojiPickerY = y;
    this.cdr.markForCheck();
  }

  reactToMessage(msgId: number, emoji: string, event: MouseEvent): void {
    event.stopPropagation();
    this.emojiPickerMsgId = null;
    this.api.toggleReaction(msgId, emoji).subscribe();
  }

  toggleExistingReaction(msgId: number, emoji: string, event: MouseEvent): void {
    event.stopPropagation();
    this.api.toggleReaction(msgId, emoji).subscribe();
  }

  getGroupedReactions(msgId: number): GroupedReaction[] {
    return this.groupedReactionsCache.get(msgId) || [];
  }

  hasReactions(msg: MessageDTO): boolean {
    return msg.reactions && msg.reactions.length > 0;
  }

  onChatAreaClick(): void {
    this.closeContextMenu();
    this.emojiPickerMsgId = null;
    this.cdr.markForCheck();
  }

  trackMsg(_i: number, msg: MessageDTO): number {
    return msg.id;
  }

  isMyMessage(msg: MessageDTO): boolean {
    return msg.senderId === this.currentUserId;
  }

  avatar(name: string): string {
    return `https://ui-avatars.com/api/?name=${encodeURIComponent(name)}&background=0a66c2&color=fff`;
  }

  timeAgo(dateStr: string): string {
    const s = Math.floor((Date.now() - new Date(dateStr).getTime()) / 1000);
    if (s < 60) return 'now';
    if (s < 3600) return Math.floor(s / 60) + 'm';
    if (s < 86400) return Math.floor(s / 3600) + 'h';
    return Math.floor(s / 86400) + 'd';
  }

  getReplyName(msg: MessageDTO): string {
    if (!msg.replyToSenderName) return '';
    return msg.replyToSenderName === this.auth.getCurrentUser()?.name ? 'You' : msg.replyToSenderName;
  }

  private buildReactionsCache(msgId: number, reactions: ReactionDTO[]): void {
    if (!reactions || reactions.length === 0) {
      this.groupedReactionsCache.delete(msgId);
      return;
    }
    const groups: { [emoji: string]: { count: number; users: string[]; reactedByMe: boolean } } = {};
    for (const r of reactions) {
      if (!groups[r.emoji]) {
        groups[r.emoji] = { count: 0, users: [], reactedByMe: false };
      }
      groups[r.emoji].count++;
      groups[r.emoji].users.push(r.userName);
      if (r.userId === this.currentUserId) {
        groups[r.emoji].reactedByMe = true;
      }
    }
    this.groupedReactionsCache.set(msgId, Object.keys(groups).map(emoji => ({
      emoji,
      count: groups[emoji].count,
      users: groups[emoji].users,
      reactedByMe: groups[emoji].reactedByMe
    })));
  }

  private rebuildAllReactionsCache(): void {
    this.groupedReactionsCache.clear();
    for (const msg of this.messages) {
      if (msg.reactions && msg.reactions.length > 0) {
        this.buildReactionsCache(msg.id, msg.reactions);
      }
    }
  }

  private handleReactionNotification(notification: ReactionNotificationDTO): void {
    const msg = this.messages.find(m => m.id === notification.messageId);
    if (!msg) return;

    if (!msg.reactions) msg.reactions = [];

    if (notification.action === 'ADDED') {
      const alreadyExists = msg.reactions.some(
        r => r.userId === notification.userId && r.emoji === notification.emoji
      );
      if (!alreadyExists) {
        msg.reactions = [...msg.reactions, {
          id: 0,
          messageId: notification.messageId,
          userId: notification.userId,
          userName: notification.userName,
          emoji: notification.emoji,
          createdAt: new Date().toISOString()
        }];
      }
    } else if (notification.action === 'REMOVED') {
      msg.reactions = msg.reactions.filter(
        r => !(r.userId === notification.userId && r.emoji === notification.emoji)
      );
    }

    this.buildReactionsCache(msg.id, msg.reactions);
    this.cdr.markForCheck();
  }

  private openChat(userId: number): void {
    this.selectedUserId = userId;
    this.showNewMsgBtn = false;
    this.isNearBottom = true;
    this.isOtherTyping = false;
    this.contextMenuMsgId = null;
    this.replyingTo = null;
    this.emojiPickerMsgId = null;
    this.msgState.setActiveChat(userId);
    this.api.getConversation(userId).subscribe(res => {
      this.messages = (res.content || []).slice().reverse();
      this.rebuildAllReactionsCache();
      this.cdr.markForCheck();
      this.instantScroll();
    });
    this.markReadById(userId);
  }

  private handleIncoming(msg: MessageDTO): void {
    this.updateConvoList(msg);

    if (msg.senderId === this.selectedUserId || msg.receiverId === this.selectedUserId) {
      if (this.messages.some(m => m.id === msg.id)) return;

      if (!msg.reactions) msg.reactions = [];
      this.messages = [...this.messages, msg];
      this.buildReactionsCache(msg.id, msg.reactions);
      this.cdr.markForCheck();

      if (this.isNearBottom || msg.senderId === this.currentUserId) {
        this.instantScroll();
        this.showNewMsgBtn = false;
      } else {
        this.showNewMsgBtn = true;
        this.cdr.markForCheck();
      }

      if (msg.senderId === this.selectedUserId) {
        this.api.markAsRead(msg.senderId).subscribe();
      }
    }

    this.cdr.markForCheck();
  }

  private handleReadReceipt(receipt: ReadReceiptDTO): void {
    if (receipt.senderUserId !== this.currentUserId) return;
    let changed = false;
    for (const msg of this.messages) {
      if (receipt.messageIds.includes(msg.id)) {
        msg.readStatus = true;
        msg.readAt = receipt.readAt;
        changed = true;
      }
    }
    if (changed) {
      this.messages = [...this.messages];
      this.cdr.markForCheck();
    }
  }

  private handleTyping(typing: TypingDTO): void {
    if (typing.userId !== this.selectedUserId) return;

    if (typing.typing) {
      this.isOtherTyping = true;
      this.typingUserName = typing.userName;
      if (this.typingReceiverTimeout) clearTimeout(this.typingReceiverTimeout);
      this.typingReceiverTimeout = setTimeout(() => {
        this.isOtherTyping = false;
        this.cdr.markForCheck();
      }, 4000);
    } else {
      this.isOtherTyping = false;
      if (this.typingReceiverTimeout) {
        clearTimeout(this.typingReceiverTimeout);
        this.typingReceiverTimeout = null;
      }
    }

    this.cdr.markForCheck();
  }

  private handleMessageDeleted(del: MessageDeleteDTO): void {
    if (del.deleteType === 'FOR_ME') {
      this.messages = this.messages.filter(m => m.id !== del.messageId);
      this.groupedReactionsCache.delete(del.messageId);
    } else {
      const msg = this.messages.find(m => m.id === del.messageId);
      if (msg) {
        msg.deleted = true;
        msg.deletedBy = del.deletedBy;
        msg.deleteType = 'FOR_EVERYONE';
        msg.content = 'This message was deleted';
        msg.reactions = [];
        this.groupedReactionsCache.delete(del.messageId);
      }
      this.messages = [...this.messages];
    }

    const convo = this.conversations.find(c =>
      c.userId === del.otherUserId || c.userId === del.deletedBy
    );
    if (convo) {
      const last = this.messages[this.messages.length - 1];
      if (last && last.id === del.messageId) {
        convo.lastMessage = 'This message was deleted';
      }
    }

    this.cdr.markForCheck();
  }

  private updateConvoList(msg: MessageDTO): void {
    if (msg.senderId !== this.currentUserId) {
      const existing = this.conversations.find(c => c.userId === msg.senderId);
      if (existing) {
        existing.lastMessage = msg.content;
        existing.lastMessageTime = msg.createdAt;
        if (msg.senderId !== this.selectedUserId) {
          existing.unreadCount = (existing.unreadCount || 0) + 1;
        }
      } else {
        this.conversations.unshift({
          userId: msg.senderId,
          userName: msg.senderName,
          userProfilePic: msg.senderProfilePic,
          lastMessage: msg.content,
          lastMessageTime: msg.createdAt,
          unreadCount: msg.senderId !== this.selectedUserId ? 1 : 0
        });
      }
    } else {
      const existing = this.conversations.find(c => c.userId === msg.receiverId);
      if (existing) {
        existing.lastMessage = msg.content;
        existing.lastMessageTime = msg.createdAt;
      }
    }
  }

  private updateConvoPreview(userId: number, content: string, time: string): void {
    const c = this.conversations.find(x => x.userId === userId);
    if (c) {
      c.lastMessage = content;
      c.lastMessageTime = time;
    }
  }

  private markRead(convo: ConversationDTO): void {
    const prev = convo.unreadCount || 0;
    if (prev > 0) {
      this.api.markAsRead(convo.userId).subscribe(() => {
        convo.unreadCount = 0;
        this.msgState.decrement(prev);
        this.cdr.markForCheck();
      });
    }
  }

  private markReadById(userId: number): void {
    const convo = this.conversations.find(c => c.userId === userId);
    if (convo) this.markRead(convo);
  }

  private stopTyping(): void {
    if (this.isSendingTyping && this.selectedUserId) {
      this.isSendingTyping = false;
      this.ws.sendTyping(this.selectedUserId, false);
    }
    if (this.typingStopTimeout) {
      clearTimeout(this.typingStopTimeout);
      this.typingStopTimeout = null;
    }
    if (this.typingInterval) {
      clearInterval(this.typingInterval);
      this.typingInterval = null;
    }
  }

  private instantScroll(): void {
    this.zone.runOutsideAngular(() => {
      requestAnimationFrame(() => {
        const el = this.msgContainer?.nativeElement;
        if (el) el.scrollTop = el.scrollHeight;
      });
    });
  }
}

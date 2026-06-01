import { Component, OnInit, OnDestroy, ViewChild, ElementRef, ChangeDetectorRef, ChangeDetectionStrategy, NgZone, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { ApiService } from '../../services/api.service';
import { AuthService } from '../../services/auth.service';
import { WebSocketService } from '../../services/websocket.service';
import { MessageStateService } from '../../services/message-state.service';
import { ConversationDTO, MessageDTO, ReadReceiptDTO, TypingDTO, MessageDeleteDTO, ReactionNotificationDTO, ReactionDTO, AttachmentDTO, ChatRoomDTO, PresenceDTO } from '../../models';
import { Subscription } from 'rxjs';
import { PresenceDotComponent } from './presence-dot.component';
import { VoiceRecorderComponent } from './voice-recorder.component';
import { NewChatDialogComponent } from './new-chat-dialog.component';
import { RoomInfoDialogComponent } from './room-info-dialog.component';

export interface GroupedReaction {
  emoji: string;
  count: number;
  users: string[];
  reactedByMe: boolean;
}

@Component({
  selector: 'app-chat',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, PresenceDotComponent, VoiceRecorderComponent, NewChatDialogComponent, RoomInfoDialogComponent],
  templateUrl: './chat.html',
  styleUrls: ['./chat.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ChatComponent implements OnInit, OnDestroy {

  conversations: ConversationDTO[] = [];
  filteredConvos: ConversationDTO[] = [];
  activeTab: 'ALL' | 'UNREAD' | 'GROUPS' = 'ALL';
  convoSearch = '';

  messages: MessageDTO[] = [];
  pinnedMessages: MessageDTO[] = [];
  pinnedExpanded = false;

  selectedKind: 'DM' | 'ROOM' | null = null;
  selectedUserId: number | null = null;
  selectedRoomId: number | null = null;
  selectedName = '';
  selectedPic = '';
  selectedPresence: string = 'OFFLINE';
  selectedRoom: ChatRoomDTO | null = null;

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
  contextMenuCanEdit = false;
  contextMenuIsDeleted = false;
  contextMenuIsPinned = false;
  replyingTo: MessageDTO | null = null;
  editingMsgId: number | null = null;
  editText = '';

  emojiPickerMsgId: number | null = null;
  quickEmojis: string[] = ['👍', '❤️', '😂', '😮', '😢', '🎉'];
  groupedReactionsCache: Map<number, GroupedReaction[]> = new Map();
  emojiPickerX = 0;
  emojiPickerY = 0;

  pendingAttachments: AttachmentDTO[] = [];
  uploadingFile = false;

  searchResults: MessageDTO[] = [];
  searchMode = false;
  globalSearch = '';

  showNewChatDialog = false;
  showRoomInfo = false;

  presenceMap = new Map<number, string>();

  private subs: Subscription[] = [];
  private isNearBottom = true;
  private typingStopTimeout: any = null;
  private heartbeatTimer: any = null;

  @ViewChild('msgContainer') private msgContainer!: ElementRef<HTMLDivElement>;
  @ViewChild('msgInput') private msgInput!: ElementRef<HTMLInputElement>;
  @ViewChild('fileInput') private fileInput!: ElementRef<HTMLInputElement>;
  @ViewChild('imageInput') private imageInput!: ElementRef<HTMLInputElement>;

  constructor(
    private api: ApiService,
    private auth: AuthService,
    private ws: WebSocketService,
    private msgState: MessageStateService,
    private route: ActivatedRoute,
    private router: Router,
    private cdr: ChangeDetectorRef,
    private zone: NgZone
  ) {
    this.currentUserId = this.auth.getCurrentUser()?.userId || 0;
  }

  ngOnInit(): void {
    this.loadConversations();
    this.startPresenceHeartbeat();

    this.subs.push(
      this.route.params.subscribe(params => {
        if (params['userId']) {
          const uid = +params['userId'];
          this.openDM(uid);
        } else if (params['roomId']) {
          const rid = +params['roomId'];
          this.openRoom(rid);
        }
      }),
      this.ws.newMessage$.subscribe(msg => this.handleIncoming(msg)),
      this.ws.roomMessage$.subscribe(msg => this.handleIncoming(msg)),
      this.ws.readReceipt$.subscribe(r => this.handleReadReceipt(r)),
      this.ws.typing$.subscribe(t => this.handleTyping(t)),
      this.ws.roomTyping$.subscribe(t => this.handleTyping(t)),
      this.ws.messageDeleted$.subscribe(d => this.handleMessageDeleted(d)),
      this.ws.reaction$.subscribe(r => this.handleReactionNotification(r)),
      this.ws.roomReaction$.subscribe(r => this.handleReactionNotification(r)),
      this.ws.presence$.subscribe(p => this.handlePresence(p))
    );
  }

  ngOnDestroy(): void {
    this.subs.forEach(s => s.unsubscribe());
    if (this.heartbeatTimer) clearInterval(this.heartbeatTimer);
    if (this.selectedRoomId) this.ws.unsubscribeRoom(this.selectedRoomId);
    this.api.setPresenceStatus('OFFLINE').subscribe();
    this.msgState.setActiveChat(null);
  }

  @HostListener('window:beforeunload')
  beforeUnload(): void {
    this.api.setPresenceStatus('OFFLINE').subscribe();
  }

  loadConversations(): void {
    this.api.getConversations().subscribe(convos => {
      this.conversations = convos;
      this.applyConvoFilter();
      this.loading = false;
      const ids = convos.filter(c => c.kind === 'DM' && c.userId).map(c => c.userId!);
      if (ids.length > 0) {
        this.api.bulkPresence(ids).subscribe(list => {
          list.forEach(p => this.presenceMap.set(p.userId, p.status));
          this.cdr.markForCheck();
        });
      }
      this.cdr.markForCheck();
    });
  }

  startPresenceHeartbeat(): void {
    this.api.presenceHeartbeat().subscribe();
    this.heartbeatTimer = setInterval(() => {
      this.api.presenceHeartbeat().subscribe();
    }, 60000);
  }

  setTab(tab: 'ALL' | 'UNREAD' | 'GROUPS'): void {
    this.activeTab = tab;
    this.applyConvoFilter();
  }

  applyConvoFilter(): void {
    let list = [...this.conversations];
    if (this.activeTab === 'UNREAD') list = list.filter(c => c.unreadCount > 0);
    else if (this.activeTab === 'GROUPS') list = list.filter(c => c.kind === 'ROOM');
    const q = this.convoSearch.toLowerCase().trim();
    if (q) list = list.filter(c => c.userName.toLowerCase().includes(q));
    this.filteredConvos = list;
    this.cdr.markForCheck();
  }

  onConvoSearch(): void {
    this.applyConvoFilter();
  }

  onGlobalSearch(): void {
    const q = this.globalSearch.trim();
    if (q.length < 2) {
      this.searchMode = false;
      this.searchResults = [];
      this.cdr.markForCheck();
      return;
    }
    this.searchMode = true;
    this.api.searchMessages(q).subscribe(results => {
      this.searchResults = results;
      this.cdr.markForCheck();
    });
  }

  clearGlobalSearch(): void {
    this.globalSearch = '';
    this.searchMode = false;
    this.searchResults = [];
    this.cdr.markForCheck();
  }

  openDM(userId: number): void {
    if (this.selectedRoomId) this.ws.unsubscribeRoom(this.selectedRoomId);
    this.selectedKind = 'DM';
    this.selectedUserId = userId;
    this.selectedRoomId = null;
    this.selectedRoom = null;
    this.messages = [];
    this.pinnedMessages = [];
    this.groupedReactionsCache.clear();
    this.msgState.setActiveChat(userId);

    this.api.getUser(userId).subscribe(u => {
      this.selectedName = u.name;
      this.selectedPic = u.profilePicUrl || this.avatar(u.name);
      this.cdr.markForCheck();
    });

    this.api.getPresence(userId).subscribe(p => {
      this.selectedPresence = p.status;
      this.presenceMap.set(userId, p.status);
      this.cdr.markForCheck();
    });

    this.api.getConversation(userId, 0).subscribe((page: any) => {
      this.messages = (page.content || []).slice().reverse();
      this.cdr.markForCheck();
      setTimeout(() => this.scrollToBottom(), 50);
    });

    this.api.markAsRead(userId).subscribe(() => {
      const c = this.conversations.find(x => x.kind === 'DM' && x.userId === userId);
      if (c) c.unreadCount = 0;
      this.msgState.refreshCount();
      this.applyConvoFilter();
    });

    this.api.getPinnedDm(userId).subscribe(pins => {
      this.pinnedMessages = pins;
      this.cdr.markForCheck();
    });
  }

  openRoom(roomId: number): void {
    if (this.selectedRoomId) this.ws.unsubscribeRoom(this.selectedRoomId);
    this.selectedKind = 'ROOM';
    this.selectedRoomId = roomId;
    this.selectedUserId = null;
    this.messages = [];
    this.pinnedMessages = [];
    this.groupedReactionsCache.clear();
    this.msgState.setActiveChat(null);

    this.ws.subscribeRoom(roomId);

    this.api.getRoom(roomId).subscribe(r => {
      this.selectedRoom = r;
      this.selectedName = r.name;
      this.selectedPic = r.avatarUrl || this.groupAvatar(r.name);
      this.cdr.markForCheck();
    });

    this.api.getRoomMessages(roomId, 0, 50).subscribe((page: any) => {
      this.messages = (page.content || []).slice().reverse();
      this.cdr.markForCheck();
      setTimeout(() => this.scrollToBottom(), 50);
    });

    this.api.markRoomRead(roomId).subscribe(() => {
      const c = this.conversations.find(x => x.kind === 'ROOM' && x.roomId === roomId);
      if (c) c.unreadCount = 0;
      this.msgState.refreshCount();
      this.applyConvoFilter();
    });

    this.api.getPinnedRoom(roomId).subscribe(pins => {
      this.pinnedMessages = pins;
      this.cdr.markForCheck();
    });
  }

  openConvo(c: ConversationDTO): void {
    if (c.kind === 'DM' && c.userId) {
      this.router.navigate(['/chat', c.userId]);
    } else if (c.kind === 'ROOM' && c.roomId) {
      this.router.navigate(['/chat/room', c.roomId]);
    }
  }

  sendMessage(): void {
    const content = (this.newMessage || '').trim();
    const hasContent = content.length > 0;
    const hasAttachments = this.pendingAttachments.length > 0;
    if (!hasContent && !hasAttachments) return;
    if (!this.selectedKind) return;

    let messageType = 'TEXT';
    if (hasAttachments) {
      const first = this.pendingAttachments[0];
      if (first.attachmentType === 'IMAGE') messageType = 'IMAGE';
      else if (first.attachmentType === 'VOICE') messageType = 'VOICE';
      else messageType = 'FILE';
    }

    const payload: any = {
      content: hasContent ? content : (messageType === 'IMAGE' ? 'Photo' : messageType === 'VOICE' ? 'Voice message' : 'File'),
      messageType,
      replyToId: this.replyingTo ? this.replyingTo.id : null,
      attachments: this.pendingAttachments.length > 0 ? this.pendingAttachments : null
    };
    if (this.selectedKind === 'DM') payload.receiverId = this.selectedUserId;
    else payload.chatRoomId = this.selectedRoomId;

    this.newMessage = '';
    this.pendingAttachments = [];
    this.replyingTo = null;

    this.api.sendMessageFull(payload).subscribe({
      next: () => {
        this.stopTypingSignal();
      },
      error: () => alert('Failed to send message')
    });
  }

  onInputKeydown(e: KeyboardEvent): void {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      this.sendMessage();
    } else {
      this.signalTyping();
    }
  }

  signalTyping(): void {
    const recId = this.selectedKind === 'DM' ? this.selectedUserId : null;
    const roomId = this.selectedKind === 'ROOM' ? this.selectedRoomId : null;
    this.ws.sendTyping(recId, roomId, true);
    if (this.typingStopTimeout) clearTimeout(this.typingStopTimeout);
    this.typingStopTimeout = setTimeout(() => this.stopTypingSignal(), 2000);
  }

  stopTypingSignal(): void {
    const recId = this.selectedKind === 'DM' ? this.selectedUserId : null;
    const roomId = this.selectedKind === 'ROOM' ? this.selectedRoomId : null;
    this.ws.sendTyping(recId, roomId, false);
    if (this.typingStopTimeout) { clearTimeout(this.typingStopTimeout); this.typingStopTimeout = null; }
  }

  handleIncoming(msg: MessageDTO): void {
    const inThisChat =
      (this.selectedKind === 'DM' && msg.chatRoomId == null &&
        ((msg.senderId === this.currentUserId && msg.receiverId === this.selectedUserId) ||
          (msg.senderId === this.selectedUserId && msg.receiverId === this.currentUserId)))
      ||
      (this.selectedKind === 'ROOM' && msg.chatRoomId === this.selectedRoomId);

    if (inThisChat) {
      const idx = this.messages.findIndex(m => m.id === msg.id);
      if (idx >= 0) {
        this.messages[idx] = msg;
      } else {
        this.messages.push(msg);
        if (this.isNearBottom || msg.senderId === this.currentUserId) {
          setTimeout(() => this.scrollToBottom(), 50);
        } else {
          this.showNewMsgBtn = true;
        }
      }
      this.groupedReactionsCache.delete(msg.id);
      this.cdr.markForCheck();

      if (msg.senderId !== this.currentUserId) {
        if (this.selectedKind === 'DM') {
          this.api.markAsRead(this.selectedUserId!).subscribe();
        } else if (this.selectedKind === 'ROOM') {
          this.api.markRoomRead(this.selectedRoomId!).subscribe();
        }
      }
    } else {
      this.loadConversations();
    }
  }

  handleReadReceipt(receipt: ReadReceiptDTO): void {
    if (!this.selectedUserId || this.selectedKind !== 'DM') return;
    if (receipt.readByUserId !== this.selectedUserId) return;
    this.messages.forEach(m => {
      if (receipt.messageIds.includes(m.id)) {
        m.readStatus = true;
        m.readAt = receipt.readAt;
      }
    });
    this.cdr.markForCheck();
  }

  handleTyping(t: TypingDTO): void {
    if (t.userId === this.currentUserId) return;
    if (this.selectedKind === 'DM' && t.userId !== this.selectedUserId) return;
    this.isOtherTyping = t.typing;
    this.typingUserName = t.userName;
    this.cdr.markForCheck();
  }

  handleMessageDeleted(del: MessageDeleteDTO): void {
    const m = this.messages.find(x => x.id === del.messageId);
    if (!m) return;
    if (del.deleteType === 'FOR_EVERYONE') {
      m.deleted = true;
      m.content = 'This message was deleted';
      m.attachments = [];
    } else {
      this.messages = this.messages.filter(x => x.id !== del.messageId);
    }
    this.cdr.markForCheck();
  }

  handleReactionNotification(n: ReactionNotificationDTO): void {
    const m = this.messages.find(x => x.id === n.messageId);
    if (!m) return;
    if (n.action === 'ADDED') {
      m.reactions = m.reactions || [];
      const exists = m.reactions.some(r => r.userId === n.userId && r.emoji === n.emoji);
      if (!exists) {
        m.reactions.push({
          id: Date.now(),
          messageId: n.messageId,
          userId: n.userId,
          userName: n.userName,
          emoji: n.emoji,
          createdAt: new Date().toISOString()
        });
      }
    } else if (n.action === 'REMOVED') {
      m.reactions = (m.reactions || []).filter(r => !(r.userId === n.userId && r.emoji === n.emoji));
    }
    this.groupedReactionsCache.delete(n.messageId);
    this.cdr.markForCheck();
  }

  handlePresence(p: PresenceDTO): void {
    this.presenceMap.set(p.userId, p.status);
    if (this.selectedKind === 'DM' && p.userId === this.selectedUserId) {
      this.selectedPresence = p.status;
    }
    this.cdr.markForCheck();
  }

  onMessageContextMenu(e: MouseEvent, msg: MessageDTO): void {
    e.preventDefault();
    this.contextMenuMsgId = msg.id;
    const menuWidth = 200;
    const menuHeight = 220;
    const margin = 12;
    let x = e.clientX;
    let y = e.clientY;
    if (x + menuWidth + margin > window.innerWidth) {
      x = window.innerWidth - menuWidth - margin;
    }
    if (y + menuHeight + margin > window.innerHeight) {
      y = window.innerHeight - menuHeight - margin;
    }
    this.contextMenuX = x;
    this.contextMenuY = y;
    this.contextMenuIsMine = msg.senderId === this.currentUserId;
    this.contextMenuIsDeleted = msg.deleted;
    this.contextMenuIsPinned = msg.pinned;
    const createdMs = new Date(msg.createdAt).getTime();
    this.contextMenuCanDeleteForAll = this.contextMenuIsMine && (Date.now() - createdMs) < 3600 * 1000;
    this.contextMenuCanEdit = this.contextMenuIsMine && !msg.deleted && (Date.now() - createdMs) < 15 * 60 * 1000 && msg.messageType === 'TEXT';
    this.cdr.markForCheck();
  }

  closeContextMenu(): void {
    this.contextMenuMsgId = null;
    this.cdr.markForCheck();
  }

  replyAction(): void {
    const m = this.messages.find(x => x.id === this.contextMenuMsgId);
    if (m) this.replyingTo = m;
    this.closeContextMenu();
    setTimeout(() => this.msgInput?.nativeElement.focus(), 50);
  }

  cancelReply(): void { this.replyingTo = null; }

  editAction(): void {
    const m = this.messages.find(x => x.id === this.contextMenuMsgId);
    if (m) {
      this.editingMsgId = m.id;
      this.editText = m.content;
    }
    this.closeContextMenu();
  }

  saveEdit(): void {
    if (!this.editingMsgId) return;
    const txt = this.editText.trim();
    if (!txt) return;
    this.api.editMessage(this.editingMsgId, txt).subscribe({
      next: () => {
        this.editingMsgId = null;
        this.editText = '';
      },
      error: () => alert('Edit failed')
    });
  }

  cancelEdit(): void {
    this.editingMsgId = null;
    this.editText = '';
  }

  deleteForMe(): void {
    const id = this.contextMenuMsgId;
    if (!id) return;
    this.api.deleteMessage(id, 'FOR_ME').subscribe();
    this.closeContextMenu();
  }

  deleteForEveryone(): void {
    const id = this.contextMenuMsgId;
    if (!id) return;
    if (!confirm('Delete this message for everyone?')) return;
    this.api.deleteMessage(id, 'FOR_EVERYONE').subscribe();
    this.closeContextMenu();
  }

  pinAction(): void {
    const id = this.contextMenuMsgId;
    if (!id) return;
    this.api.pinMessage(id).subscribe({
      next: (updated) => {
        const m = this.messages.find(x => x.id === id);
        if (m) m.pinned = true;
        this.refreshPinned();
        this.cdr.markForCheck();
      },
      error: (err) => alert(err?.error?.error || 'Max 3 pins per chat')
    });
    this.closeContextMenu();
  }

  unpinAction(): void {
    const id = this.contextMenuMsgId;
    if (!id) return;
    this.api.unpinMessage(id).subscribe({
      next: () => {
        const m = this.messages.find(x => x.id === id);
        if (m) m.pinned = false;
        this.refreshPinned();
        this.cdr.markForCheck();
      }
    });
    this.closeContextMenu();
  }

  refreshPinned(): void {
    if (this.selectedKind === 'DM' && this.selectedUserId) {
      this.api.getPinnedDm(this.selectedUserId).subscribe(p => {
        this.pinnedMessages = p;
        this.cdr.markForCheck();
      });
    } else if (this.selectedKind === 'ROOM' && this.selectedRoomId) {
      this.api.getPinnedRoom(this.selectedRoomId).subscribe(p => {
        this.pinnedMessages = p;
        this.cdr.markForCheck();
      });
    }
  }

  jumpToPinned(p: MessageDTO): void {
    const el = document.getElementById('msg-' + p.id);
    if (el) {
      el.scrollIntoView({ behavior: 'smooth', block: 'center' });
      el.classList.add('highlight');
      setTimeout(() => el.classList.remove('highlight'), 1500);
    }
  }

  openEmojiPicker(e: MouseEvent, msg: MessageDTO): void {
    e.preventDefault();
    e.stopPropagation();
    this.emojiPickerMsgId = msg.id;
    const pickerWidth = 220;
    const pickerHeight = 44;
    const margin = 12;
    let x = e.clientX;
    let y = e.clientY;
    if (x + pickerWidth + margin > window.innerWidth) {
      x = window.innerWidth - pickerWidth - margin;
    }
    if (x < margin) x = margin;
    if (y + pickerHeight + margin > window.innerHeight) {
      y = e.clientY - pickerHeight - 8;
    }
    if (y < margin) y = margin;
    this.emojiPickerX = x;
    this.emojiPickerY = y;
    this.cdr.markForCheck();
  }

  closeEmojiPicker(): void {
    this.emojiPickerMsgId = null;
    this.cdr.markForCheck();
  }

  reactToMsg(emoji: string): void {
    const id = this.emojiPickerMsgId;
    if (!id) return;
    this.api.toggleReaction(id, emoji).subscribe();
    this.closeEmojiPicker();
  }

  groupedReactions(m: MessageDTO): GroupedReaction[] {
    if (this.groupedReactionsCache.has(m.id)) {
      return this.groupedReactionsCache.get(m.id)!;
    }
    const map = new Map<string, GroupedReaction>();
    (m.reactions || []).forEach(r => {
      const g = map.get(r.emoji) || { emoji: r.emoji, count: 0, users: [], reactedByMe: false };
      g.count++;
      g.users.push(r.userName);
      if (r.userId === this.currentUserId) g.reactedByMe = true;
      map.set(r.emoji, g);
    });
    const arr = Array.from(map.values());
    this.groupedReactionsCache.set(m.id, arr);
    return arr;
  }

  toggleReactionGroup(m: MessageDTO, g: GroupedReaction): void {
    this.api.toggleReaction(m.id, g.emoji).subscribe();
  }

  onAttachFileClick(): void { this.fileInput.nativeElement.click(); }
  onAttachImageClick(): void { this.imageInput.nativeElement.click(); }

  onFilePicked(e: any): void {
    const file: File = e.target.files?.[0];
    if (!file) return;
    this.uploadingFile = true;
    this.cdr.markForCheck();
    this.api.uploadFile(file).subscribe({
      next: (res: any) => {
        this.pendingAttachments.push({
          attachmentType: 'FILE',
          url: res.url,
          fileName: res.fileName,
          fileSize: res.fileSize
        });
        this.uploadingFile = false;
        e.target.value = '';
        this.cdr.markForCheck();
      },
      error: () => {
        this.uploadingFile = false;
        alert('Upload failed');
        this.cdr.markForCheck();
      }
    });
  }

  onImagePicked(e: any): void {
    const file: File = e.target.files?.[0];
    if (!file) return;
    this.uploadingFile = true;
    this.cdr.markForCheck();
    this.api.uploadImage(file).subscribe({
      next: (res: any) => {
        this.pendingAttachments.push({
          attachmentType: 'IMAGE',
          url: res.url,
          fileName: file.name,
          fileSize: file.size
        });
        this.uploadingFile = false;
        e.target.value = '';
        this.cdr.markForCheck();
      },
      error: () => {
        this.uploadingFile = false;
        alert('Upload failed');
        this.cdr.markForCheck();
      }
    });
  }

  onVoiceRecorded(payload: { blob: Blob, duration: number }): void {
    if (!payload.blob || !this.selectedKind) return;
    this.uploadingFile = true;
    this.cdr.markForCheck();
    const f = new File([payload.blob], 'voice-' + Date.now() + '.webm', { type: 'audio/webm' });
    this.api.uploadVoice(f, payload.duration).subscribe({
      next: (res: any) => {
        this.uploadingFile = false;
        const payload2: any = {
          content: 'Voice message',
          messageType: 'VOICE',
          attachments: [{
            attachmentType: 'VOICE',
            url: res.url,
            fileName: res.fileName,
            fileSize: res.fileSize,
            durationSeconds: res.durationSeconds
          }]
        };
        if (this.selectedKind === 'DM') payload2.receiverId = this.selectedUserId;
        else payload2.chatRoomId = this.selectedRoomId;
        this.api.sendMessageFull(payload2).subscribe();
        this.cdr.markForCheck();
      },
      error: () => {
        this.uploadingFile = false;
        alert('Voice upload failed');
        this.cdr.markForCheck();
      }
    });
  }

  removePendingAttachment(idx: number): void {
    this.pendingAttachments.splice(idx, 1);
    this.cdr.markForCheck();
  }

  formatFileSize(b: number | undefined): string {
    if (!b) return '';
    if (b < 1024) return b + ' B';
    if (b < 1024 * 1024) return (b / 1024).toFixed(1) + ' KB';
    return (b / 1024 / 1024).toFixed(1) + ' MB';
  }

  formatDuration(s: number | undefined): string {
    if (!s) return '0:00';
    const m = Math.floor(s / 60);
    const sec = s % 60;
    return `${m}:${sec < 10 ? '0' : ''}${sec}`;
  }

  onMsgScroll(): void {
    if (!this.msgContainer) return;
    const el = this.msgContainer.nativeElement;
    const threshold = 100;
    this.isNearBottom = el.scrollHeight - el.scrollTop - el.clientHeight < threshold;
    if (this.isNearBottom && this.showNewMsgBtn) {
      this.showNewMsgBtn = false;
      this.cdr.markForCheck();
    }
  }

  scrollToBottom(): void {
    if (!this.msgContainer) return;
    const el = this.msgContainer.nativeElement;
    el.scrollTop = el.scrollHeight;
    this.showNewMsgBtn = false;
    this.isNearBottom = true;
  }

  onNewChatClick(): void {
    this.showNewChatDialog = true;
    this.cdr.markForCheck();
  }

  onDMCreated(uid: number): void {
    this.showNewChatDialog = false;
    this.router.navigate(['/chat', uid]);
  }

  onGroupCreated(room: ChatRoomDTO): void {
    this.showNewChatDialog = false;
    this.loadConversations();
    this.router.navigate(['/chat/room', room.id]);
  }

  onRoomInfoUpdated(room: ChatRoomDTO): void {
    this.selectedRoom = room;
    this.selectedName = room.name;
    const c = this.conversations.find(x => x.kind === 'ROOM' && x.roomId === room.id);
    if (c) { c.userName = room.name; c.memberCount = room.memberCount; }
    this.applyConvoFilter();
    this.cdr.markForCheck();
  }

  onRoomLeft(roomId: number): void {
    this.showRoomInfo = false;
    this.selectedKind = null;
    this.selectedRoomId = null;
    this.selectedRoom = null;
    this.messages = [];
    this.ws.unsubscribeRoom(roomId);
    this.loadConversations();
    this.router.navigate(['/chat']);
  }

  avatar(name: string): string {
    return `https://ui-avatars.com/api/?name=${encodeURIComponent(name || '?')}&background=1a1a1a&color=fff`;
  }

  groupAvatar(name: string): string {
    return `https://ui-avatars.com/api/?name=${encodeURIComponent(name || 'G')}&background=1a1a1a&color=fff`;
  }

  presenceFor(c: ConversationDTO): string {
    if (c.kind !== 'DM' || !c.userId) return '';
    return this.presenceMap.get(c.userId) || c.presence || 'OFFLINE';
  }

  trackByConvo(_: number, c: ConversationDTO): string {
    return c.kind + ':' + (c.userId || c.roomId);
  }

  trackByMsg(_: number, m: MessageDTO): number {
    return m.id;
  }

  formatTime(iso: string): string {
    if (!iso) return '';
    const d = new Date(iso);
    return d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
  }

  formatDay(iso: string): string {
    if (!iso) return '';
    const d = new Date(iso);
    const today = new Date();
    if (d.toDateString() === today.toDateString()) return 'Today';
    const yesterday = new Date(); yesterday.setDate(today.getDate() - 1);
    if (d.toDateString() === yesterday.toDateString()) return 'Yesterday';
    return d.toLocaleDateString();
  }

  shouldShowDateSep(idx: number): boolean {
    if (idx === 0) return true;
    const prev = this.messages[idx - 1];
    const curr = this.messages[idx];
    return new Date(prev.createdAt).toDateString() !== new Date(curr.createdAt).toDateString();
  }
}

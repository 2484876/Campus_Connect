import { Injectable, NgZone } from '@angular/core';
import { AuthService } from './auth.service';
import { Subject } from 'rxjs';
import { Client, StompSubscription } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { MessageDTO, ReadReceiptDTO, TypingDTO, MessageDeleteDTO, ReactionNotificationDTO, PresenceDTO } from '../models';

@Injectable({ providedIn: 'root' })
export class WebSocketService {

  private client: Client | null = null;
  private connected = false;
  private roomSubs = new Map<number, StompSubscription[]>();

  public newMessage$ = new Subject<MessageDTO>();
  public newNotification$ = new Subject<any>();
  public readReceipt$ = new Subject<ReadReceiptDTO>();
  public typing$ = new Subject<TypingDTO>();
  public messageDeleted$ = new Subject<MessageDeleteDTO>();
  public reaction$ = new Subject<ReactionNotificationDTO>();
  public presence$ = new Subject<PresenceDTO>();
  public roomMessage$ = new Subject<MessageDTO>();
  public roomTyping$ = new Subject<TypingDTO>();
  public roomReaction$ = new Subject<ReactionNotificationDTO>();

  constructor(
    private auth: AuthService,
    private zone: NgZone
  ) { }

  connect(): void {
    const user = this.auth.getCurrentUser();
    const token = this.auth.getToken();
    if (!user || !token || this.connected) return;

    this.client = new Client({
      webSocketFactory: () => new SockJS('http://localhost:8080/ws'),
      connectHeaders: {
        Authorization: 'Bearer ' + token
      },
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      debug: (str) => { }
    });

    this.client.onConnect = () => {
      this.connected = true;
      const uid = user.userId;

      this.client?.subscribe('/queue/messages/' + uid, (message) => {
        this.zone.run(() => this.newMessage$.next(JSON.parse(message.body)));
      });

      this.client?.subscribe('/queue/notifications/' + uid, (message) => {
        this.zone.run(() => this.newNotification$.next(JSON.parse(message.body)));
      });

      this.client?.subscribe('/queue/read-receipt/' + uid, (message) => {
        this.zone.run(() => this.readReceipt$.next(JSON.parse(message.body)));
      });

      this.client?.subscribe('/queue/typing/' + uid, (message) => {
        this.zone.run(() => this.typing$.next(JSON.parse(message.body)));
      });

      this.client?.subscribe('/queue/message-deleted/' + uid, (message) => {
        this.zone.run(() => this.messageDeleted$.next(JSON.parse(message.body)));
      });

      this.client?.subscribe('/queue/reactions/' + uid, (message) => {
        this.zone.run(() => this.reaction$.next(JSON.parse(message.body)));
      });

      this.client?.subscribe('/topic/presence', (message) => {
        this.zone.run(() => this.presence$.next(JSON.parse(message.body)));
      });
    };

    this.client.onStompError = (frame) => {
      console.error('WS STOMP error:', frame);
    };

    this.client.onDisconnect = () => { this.connected = false; };
    this.client.onWebSocketClose = () => { this.connected = false; };

    this.client.activate();
  }

  subscribeRoom(roomId: number): void {
    if (!this.client || !this.connected) return;
    if (this.roomSubs.has(roomId)) return;
    const subs: StompSubscription[] = [];

    const sMsg = this.client.subscribe('/topic/room/' + roomId, (m) => {
      this.zone.run(() => this.roomMessage$.next(JSON.parse(m.body)));
    });
    subs.push(sMsg);

    const sTyp = this.client.subscribe('/topic/room/' + roomId + '/typing', (m) => {
      this.zone.run(() => this.roomTyping$.next(JSON.parse(m.body)));
    });
    subs.push(sTyp);

    const sRx = this.client.subscribe('/topic/room/' + roomId + '/reactions', (m) => {
      this.zone.run(() => this.roomReaction$.next(JSON.parse(m.body)));
    });
    subs.push(sRx);

    this.roomSubs.set(roomId, subs);
  }

  unsubscribeRoom(roomId: number): void {
    const subs = this.roomSubs.get(roomId);
    if (subs) {
      subs.forEach(s => { try { s.unsubscribe(); } catch (e) { } });
      this.roomSubs.delete(roomId);
    }
  }

  sendTyping(receiverId: number | null, roomId: number | null, typing: boolean): void {
    if (this.client && this.connected) {
      const payload: any = { typing };
      if (receiverId != null) payload.receiverId = receiverId;
      if (roomId != null) payload.roomId = roomId;
      this.client.publish({
        destination: '/app/chat.typing',
        body: JSON.stringify(payload)
      });
    }
  }

  sendReaction(messageId: number, emoji: string): void {
    if (this.client && this.connected) {
      this.client.publish({
        destination: '/app/chat.react',
        body: JSON.stringify({ messageId, emoji })
      });
    }
  }

  disconnect(): void {
    if (this.client) {
      this.roomSubs.forEach((subs) => subs.forEach(s => { try { s.unsubscribe(); } catch (e) { } }));
      this.roomSubs.clear();
      this.client.deactivate();
      this.connected = false;
    }
  }

  isConnected(): boolean { return this.connected; }
}

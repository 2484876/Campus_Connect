import { Injectable, NgZone } from '@angular/core';
import { AuthService } from './auth.service';
import { Subject } from 'rxjs';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { MessageDTO, ReadReceiptDTO, TypingDTO, MessageDeleteDTO, ReactionNotificationDTO } from '../models';

@Injectable({ providedIn: 'root' })
export class WebSocketService {

  private client: Client | null = null;
  private connected = false;

  public newMessage$ = new Subject<MessageDTO>();
  public newNotification$ = new Subject<any>();
  public readReceipt$ = new Subject<ReadReceiptDTO>();
  public typing$ = new Subject<TypingDTO>();
  public messageDeleted$ = new Subject<MessageDeleteDTO>();
  public reaction$ = new Subject<ReactionNotificationDTO>();

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
      debug: (str) => {
        console.log('WS:', str);
      }
    });

    this.client.onConnect = () => {
      this.connected = true;
      const uid = user.userId;

      this.client?.subscribe('/queue/messages/' + uid, (message) => {
        this.zone.run(() => {
          this.newMessage$.next(JSON.parse(message.body));
        });
      });

      this.client?.subscribe('/queue/notifications/' + uid, (message) => {
        this.zone.run(() => {
          this.newNotification$.next(JSON.parse(message.body));
        });
      });

      this.client?.subscribe('/queue/read-receipt/' + uid, (message) => {
        this.zone.run(() => {
          this.readReceipt$.next(JSON.parse(message.body));
        });
      });

      this.client?.subscribe('/queue/typing/' + uid, (message) => {
        this.zone.run(() => {
          this.typing$.next(JSON.parse(message.body));
        });
      });

      this.client?.subscribe('/queue/message-deleted/' + uid, (message) => {
        this.zone.run(() => {
          this.messageDeleted$.next(JSON.parse(message.body));
        });
      });

      this.client?.subscribe('/queue/reactions/' + uid, (message) => {
        this.zone.run(() => {
          this.reaction$.next(JSON.parse(message.body));
        });
      });
    };

    this.client.onStompError = (frame) => {
      console.error('WS STOMP error:', frame);
    };

    this.client.onDisconnect = () => {
      this.connected = false;
    };

    this.client.onWebSocketClose = () => {
      this.connected = false;
    };

    this.client.activate();
  }

  sendTyping(receiverId: number, typing: boolean): void {
    if (this.client && this.connected) {
      this.client.publish({
        destination: '/app/chat.typing',
        body: JSON.stringify({ receiverId, typing })
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
      this.client.deactivate();
      this.connected = false;
    }
  }

  isConnected(): boolean {
    return this.connected;
  }
}

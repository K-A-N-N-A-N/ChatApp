import { Component, ElementRef, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { ChatService } from '../services/chat.service';
import { WsService } from '../services/ws.service';

interface Message {
  id?: string;
  senderId: string;
  senderUsername: string;
  content: string;
  createdAt: string;
  deliveredCount?: number;
  readCount?: number;
}

interface UserPresence {
  userId: string;
  status: string;
  lastSeen: string;
}

@Component({
  selector: 'app-chat',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './chat.component.html',
  styleUrls: ['./chat.component.css']
})
export class ChatComponent {

  currentUserId!: string;
  currentUsername!: string;
  chatRoomId!: string;

  message = '';
  messages: Message[] = [];
  wsConnected = false;

  // Track user presence
  userPresences: Map<string, UserPresence> = new Map();
  otherUserIds: string[] = [];

  @ViewChild('chatBody') chatBody!: ElementRef;

  constructor(
    private route: ActivatedRoute,
    private auth: AuthService,
    private chat: ChatService,
    private ws: WsService
  ) {}

  ngOnInit() {
    const routeId = this.route.snapshot.paramMap.get('chatRoomId');
    const queryId = this.route.snapshot.queryParamMap.get('chatRoomId');
    this.chatRoomId = (routeId || queryId || '').trim();

    if (!this.chatRoomId) {
      console.error('Missing chatRoomId (route param or query param)');
      return;
    }

    this.auth.getMe().subscribe(user => {
      this.currentUserId = user.userId;
      this.currentUsername = user.username;

      // Load message history
      this.chat.loadMessages(this.chatRoomId).subscribe(history => {
        this.messages = history.map((m: any) => ({
          id: m.id,
          senderId: m.senderId,
          senderUsername: m.senderUsername,
          content: m.content,
          createdAt: m.createdAt,
          deliveredCount: m.deliveredCount || 0,
          readCount: m.readCount || 0
        }));

        this.scrollToBottom();

        // Load initial presence
        this.chat.getPresence(this.chatRoomId).subscribe(presences => {
          presences.forEach(p => {
            this.userPresences.set(p.userId, p);
            if (p.userId !== this.currentUserId) {
              this.otherUserIds.push(p.userId);
            }
          });
        });

        // Connect WebSocket
        this.ws.connect(
          this.chatRoomId,
          (msg: any) => this.onNewMessage(msg),
          (presence: any) => this.onPresenceUpdate(presence),
          (receipt: any) => this.onReadReceipt(receipt),
          () => {
            this.wsConnected = true;
            // NOW mark all messages as read AFTER WebSocket is connected
            this.markVisibleMessagesAsRead();
          }
        );
      });
    });
  }

  onNewMessage(msg: any) {
    const newMessage: Message = {
      id: msg.id,
      senderId: msg.senderId,
      senderUsername: msg.senderUsername,
      content: msg.content,
      createdAt: msg.createdAt,
      deliveredCount: msg.deliveredCount || 0,
      readCount: msg.readCount || 0
    };
    this.messages.push(newMessage);
    this.scrollToBottom();

    // Auto-mark as read if it's from someone else
    if (msg.senderId !== this.currentUserId && msg.id) {
      setTimeout(() => this.markMessageAsRead(msg.id), 500);
    }
  }

  onPresenceUpdate(presence: any) {
    this.userPresences.set(presence.userId, presence);
  }

  onReadReceipt(receipt: any) {
    // Update the message's read/delivered counts
    const msg = this.messages.find(m => m.id === receipt.messageId);
    if (msg) {
      msg.deliveredCount = receipt.deliveredCount;
      msg.readCount = receipt.readCount;
    }
  }

  send() {
    if (!this.message.trim()) return;
    if (!this.wsConnected) return;
    try {
      this.ws.sendMessage(this.message);
    } catch (e) {
      console.error(e);
    }
    this.message = '';
  }

  private markVisibleMessagesAsRead() {
    // Mark all messages from others as read when component loads
    this.messages
      .filter(m => m.senderId !== this.currentUserId && m.id)
      .forEach(m => {
        if (m.id) {
          this.markMessageAsRead(m.id);
        }
      });
  }

  private markMessageAsRead(messageId: string) {
    this.ws.markMessageAsRead(messageId);
  }

  private scrollToBottom() {
    setTimeout(() => {
      this.chatBody.nativeElement.scrollTop =
        this.chatBody.nativeElement.scrollHeight;
    });
  }

  // Helper methods for template
  isOnline(userId: string): boolean {
    return this.userPresences.get(userId)?.status === 'ONLINE';
  }

  getLastSeen(userId: string): string {
    const presence = this.userPresences.get(userId);
    if (!presence || presence.status === 'ONLINE') return '';

    const lastSeen = new Date(presence.lastSeen);
    const now = new Date();
    const diffMs = now.getTime() - lastSeen.getTime();
    const diffMins = Math.floor(diffMs / 60000);

    if (diffMins < 1) return 'just now';
    if (diffMins < 60) return `${diffMins}m ago`;
    const diffHours = Math.floor(diffMins / 60);
    if (diffHours < 24) return `${diffHours}h ago`;
    return `${Math.floor(diffHours / 24)}d ago`;
  }

  getOtherUserId(): string | null {
    return this.otherUserIds.length > 0 ? this.otherUserIds[0] : null;
  }

  ngOnDestroy() {
    this.ws.disconnect();
    this.wsConnected = false;
  }
}

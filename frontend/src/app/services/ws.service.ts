import { Injectable } from '@angular/core';
import { Client } from '@stomp/stompjs';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class WsService {

  private client!: Client;
  private connectedChatRoomId?: string;
  private isConnected = false;

  connect(
    chatRoomId: string,
    onMessage: (msg: any) => void,
    onPresence: (presence: any) => void,
    onReadReceipt: (receipt: any) => void,
    onConnected?: () => void
  ) {

    this.client = new Client({
      webSocketFactory: () =>
        new WebSocket(
          `${environment.apiBaseUrl.replace('http', 'ws')}/ws?chatRoomId=${chatRoomId}`
        ),
      reconnectDelay: 5000,
      debug: () => {}
    });

    this.client.onConnect = () => {
      this.connectedChatRoomId = chatRoomId;
      this.isConnected = true;

      // Subscribe to messages
      this.client.subscribe(
        `/topic/chatroom/${chatRoomId}`,
        message => onMessage(JSON.parse(message.body))
      );

      // Subscribe to presence updates
      this.client.subscribe(
        `/topic/chatroom/${chatRoomId}/presence`,
        message => onPresence(JSON.parse(message.body))
      );

      // Subscribe to read receipts
      this.client.subscribe(
        `/topic/chatroom/${chatRoomId}/receipts`,
        message => onReadReceipt(JSON.parse(message.body))
      );

      onConnected?.();
    };

    this.client.onDisconnect = () => {
      this.isConnected = false;
    };

    this.client.activate();
  }

  disconnect() {
    this.connectedChatRoomId = undefined;
    this.isConnected = false;
    try {
      this.client?.deactivate();
    } catch {
      // ignore
    }
  }

  connected() {
    return !!this.client && this.isConnected && this.client.connected;
  }

  sendMessage(content: string) {
    if (!this.client || !this.client.connected) {
      throw new Error('WebSocket is not connected yet.');
    }
    this.client.publish({
      destination: '/app/chat.send',
      body: JSON.stringify({ content })
    });
  }

  markMessageAsRead(messageId: string) {
    if (!this.client || !this.client.connected) {
      console.warn('Cannot mark as read - WebSocket not connected');
      return;
    }
    this.client.publish({
      destination: '/app/chat.markRead',
      body: JSON.stringify({ messageId })
    });
  }
}

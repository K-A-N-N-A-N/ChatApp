import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ChatService {

  constructor(private http: HttpClient) {}

  loadMessages(chatRoomId: string) {
    return this.http.get<any[]>(
      `${environment.apiBaseUrl}/chatrooms/${chatRoomId}/messages`,
      { withCredentials: true }
    );
  }

  getPresence(chatRoomId: string) {
    return this.http.get<any[]>(
      `${environment.apiBaseUrl}/chatrooms/${chatRoomId}/presence`,
      { withCredentials: true }
    );
  }
}

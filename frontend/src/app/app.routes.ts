import { Routes } from '@angular/router';
import { ChatComponent } from './chat/chat.component';

export const routes: Routes = [
  { path: 'chat/:chatRoomId', component: ChatComponent },
  { path: '**', redirectTo: 'chat/test-room' }
];

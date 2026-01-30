import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  constructor(private http: HttpClient) {}

  getMe() {
    return this.http.get<{ userId: string; username: string }>(
      `${environment.apiBaseUrl}/auth/me`,
      { withCredentials: true }
    );
  }
}

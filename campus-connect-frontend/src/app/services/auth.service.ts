import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, Subject, tap } from 'rxjs';
import { AuthResponse } from '../models';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class AuthService {

  private api = environment.apiUrl + '/auth';
  public loginSuccess$ = new Subject<AuthResponse>();

  constructor(private http: HttpClient) { }

  register(data: any): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(this.api + '/register', data)
      .pipe(tap(res => {
        this.saveAuth(res);
        this.loginSuccess$.next(res);
      }));
  }

  login(data: any): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(this.api + '/login', data)
      .pipe(tap(res => {
        this.saveAuth(res);
        this.loginSuccess$.next(res);
      }));
  }

  private saveAuth(res: AuthResponse): void {
    sessionStorage.setItem('token', res.token);
    sessionStorage.setItem('user', JSON.stringify(res));
  }

  getToken(): string | null {
    return sessionStorage.getItem('token');
  }

  getCurrentUser(): AuthResponse | null {
    const u = sessionStorage.getItem('user');
    return u ? JSON.parse(u) : null;
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }

  logout(): void {
    sessionStorage.clear();
    window.location.href = '/login';
  }
}

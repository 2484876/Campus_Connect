import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface AdminPage<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  last: boolean;
  number: number;
}

export interface AdminUser {
  id: number;
  name: string;
  email: string;
  role: string;
  department: string;
  position: string;
  profilePicUrl: string;
  active: boolean;
  createdAt: string;
  postCount: number;
}

export interface AdminStats {
  totalUsers: number;
  activeUsers: number;
  suspendedUsers: number;
  adminCount: number;
  totalPosts: number;
  activePosts: number;
  pendingReports: number;
  newUsersLast7Days: number;
}

export interface AdminReport {
  id: number;
  reporterId: number;
  reporterName: string;
  targetType: string;
  targetId: number;
  reason: string;
  details: string;
  status: string;
  createdAt: string;
}

export interface AdminContent {
  id: number;
  type: string;
  authorId: number;
  authorName: string;
  preview: string;
  active: boolean;
  createdAt: string;
}

export interface AdminLog {
  id: number;
  adminId: number;
  adminName: string;
  action: string;
  targetType: string;
  targetId: number;
  targetLabel: string;
  details: string;
  createdAt: string;
}

@Injectable({ providedIn: 'root' })
export class AdminService {

  private api = environment.apiUrl + '/admin';

  constructor(private http: HttpClient) { }

  getStats(): Observable<AdminStats> {
    return this.http.get<AdminStats>(`${this.api}/stats`);
  }

  getUsers(q: string, status: string, role: string, page: number, size = 20): Observable<AdminPage<AdminUser>> {
    let params = `page=${page}&size=${size}`;
    if (q) params += `&q=${encodeURIComponent(q)}`;
    if (status) params += `&status=${status}`;
    if (role) params += `&role=${role}`;
    return this.http.get<AdminPage<AdminUser>>(`${this.api}/users?${params}`);
  }

  getUser(id: number): Observable<AdminUser> {
    return this.http.get<AdminUser>(`${this.api}/users/${id}`);
  }

  setStatus(id: number, active: boolean, reason: string): Observable<void> {
    return this.http.put<void>(`${this.api}/users/${id}/status`, { active, reason });
  }

  setRole(id: number, role: string): Observable<void> {
    return this.http.put<void>(`${this.api}/users/${id}/role`, { role });
  }

  deleteUser(id: number, mode: 'SOFT' | 'HARD'): Observable<void> {
    return this.http.delete<void>(`${this.api}/users/${id}?mode=${mode}`);
  }

  getReports(page: number, size = 20): Observable<AdminPage<AdminReport>> {
    return this.http.get<AdminPage<AdminReport>>(`${this.api}/reports?page=${page}&size=${size}`);
  }

  resolveReport(id: number, status: string): Observable<void> {
    return this.http.put<void>(`${this.api}/reports/${id}?status=${status}`, {});
  }

  getPosts(page: number, size = 20): Observable<AdminPage<AdminContent>> {
    return this.http.get<AdminPage<AdminContent>>(`${this.api}/content/posts?page=${page}&size=${size}`);
  }

  removeContent(type: string, id: number): Observable<void> {
    return this.http.delete<void>(`${this.api}/content/${type}/${id}`);
  }

  getLogs(page: number, size = 30): Observable<AdminPage<AdminLog>> {
    return this.http.get<AdminPage<AdminLog>>(`${this.api}/logs?page=${page}&size=${size}`);
  }

  roles(): string[] {
    return [
      'PROGRAMMER_ANALYST_TRAINEE', 'PROGRAMMER_ANALYST', 'ASSOCIATE', 'SENIOR_ASSOCIATE',
      'MANAGER', 'SENIOR_MANAGER', 'ASSOCIATE_DIRECTOR', 'DIRECTOR', 'SENIOR_DIRECTOR',
      'AVP', 'VP', 'SVP', 'ADMIN'
    ];
  }
}

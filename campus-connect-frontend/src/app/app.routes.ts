import { Routes } from '@angular/router';
import { inject } from '@angular/core';
import { AuthService } from './services/auth.service';
import { Router } from '@angular/router';

export const authGuard = () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  if (auth.isLoggedIn()) return true;
  router.navigate(['/login']);
  return false;
};

export const routes: Routes = [
  { path: 'login', loadComponent: () => import('./pages/login/login').then(m => m.LoginComponent) },
  { path: 'register', loadComponent: () => import('./pages/register/register').then(m => m.RegisterComponent) },
  { path: 'feed', loadComponent: () => import('./pages/feed/feed').then(m => m.FeedComponent), canActivate: [authGuard] },
  { path: 'profile/:id', loadComponent: () => import('./pages/profile/profile').then(m => m.ProfileComponent), canActivate: [authGuard] },
  { path: 'connections', loadComponent: () => import('./pages/connections/connections').then(m => m.ConnectionsComponent), canActivate: [authGuard] },
  { path: 'chat', loadComponent: () => import('./pages/chat/chat').then(m => m.ChatComponent), canActivate: [authGuard] },
  { path: 'chat/:userId', loadComponent: () => import('./pages/chat/chat').then(m => m.ChatComponent), canActivate: [authGuard] },
  { path: 'events', loadComponent: () => import('./pages/events/events').then(m => m.EventsComponent), canActivate: [authGuard] },
  { path: 'events/:id', loadComponent: () => import('./pages/event-detail/event-detail.component').then(m => m.EventDetailComponent), canActivate: [authGuard] },
  { path: 'notifications', loadComponent: () => import('./pages/notifications/notifications.component').then(m => m.NotificationsComponent), canActivate: [authGuard] },
  { path: 'communities', loadComponent: () => import('./pages/communities/communities.component').then(m => m.CommunitiesComponent), canActivate: [authGuard] },
  { path: 'communities/:id', loadComponent: () => import('./pages/community-detail/community-detail.component').then(m => m.CommunityDetailComponent), canActivate: [authGuard] },
  { path: 'communities/:id/posts/:postId', loadComponent: () => import('./pages/community-post/community-post.component').then(m => m.CommunityPostComponent), canActivate: [authGuard] },
  { path: '', redirectTo: 'feed', pathMatch: 'full' },
  { path: '**', redirectTo: 'feed' }
];

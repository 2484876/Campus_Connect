import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class ThemeService {
  private themeSubject = new BehaviorSubject<'light' | 'dark'>('light');
  theme$ = this.themeSubject.asObservable();

  init(): void {
    const saved = localStorage.getItem('cc_theme') as 'light' | 'dark' | null;
    const prefersDark = typeof window !== 'undefined' && window.matchMedia &&
      window.matchMedia('(prefers-color-scheme: dark)').matches;
    const initial = saved || (prefersDark ? 'dark' : 'light');
    this.apply(initial);
  }

  current(): 'light' | 'dark' {
    return this.themeSubject.value;
  }

  toggle(): void {
    this.apply(this.themeSubject.value === 'light' ? 'dark' : 'light');
  }

  set(theme: 'light' | 'dark'): void {
    this.apply(theme);
  }

  private apply(theme: 'light' | 'dark'): void {
    document.documentElement.setAttribute('data-theme', theme);
    localStorage.setItem('cc_theme', theme);
    this.themeSubject.next(theme);
  }
}

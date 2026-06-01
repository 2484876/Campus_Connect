import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

export type ToastType = 'success' | 'error' | 'info' | 'warning';

export interface ToastAction {
  label: string;
  onClick: () => void;
}

export interface ToastOptions {
  durationMs?: number;
  sticky?: boolean;
  action?: ToastAction;
}

export interface Toast {
  id: number;
  type: ToastType;
  message: string;
  title?: string;
  durationMs: number;
  sticky: boolean;
  action?: ToastAction;
  createdAt: number;
  paused: boolean;
  pausedAt?: number;
  elapsedBeforePause: number;
}

export interface ConfirmDialog {
  id: number;
  title: string;
  message: string;
  confirmLabel: string;
  cancelLabel: string;
  confirmStyle: 'primary' | 'danger';
  resolve: (ok: boolean) => void;
}

export interface ConfirmOptions {
  title?: string;
  confirmLabel?: string;
  cancelLabel?: string;
  confirmStyle?: 'primary' | 'danger';
}

@Injectable({ providedIn: 'root' })
export class ToastService {

  private nextId = 1;
  private readonly MAX_TOASTS = 5;

  private toastsSubject = new BehaviorSubject<Toast[]>([]);
  toasts$ = this.toastsSubject.asObservable();

  private confirmSubject = new BehaviorSubject<ConfirmDialog | null>(null);
  confirm$ = this.confirmSubject.asObservable();

  success(message: string, opts: ToastOptions = {}): number {
    return this.show('success', message, opts);
  }

  error(message: string, opts: ToastOptions = {}): number {
    return this.show('error', message, { durationMs: 6000, ...opts });
  }

  info(message: string, opts: ToastOptions = {}): number {
    return this.show('info', message, opts);
  }

  warning(message: string, opts: ToastOptions = {}): number {
    return this.show('warning', message, { durationMs: 5000, ...opts });
  }

  private show(type: ToastType, message: string, opts: ToastOptions): number {
    const id = this.nextId++;
    const toast: Toast = {
      id,
      type,
      message,
      durationMs: opts.durationMs ?? 4000,
      sticky: opts.sticky ?? false,
      action: opts.action,
      createdAt: Date.now(),
      paused: false,
      elapsedBeforePause: 0
    };

    let current = this.toastsSubject.value;
    if (current.length >= this.MAX_TOASTS) {
      current = current.slice(1);
    }
    this.toastsSubject.next([...current, toast]);

    if (!toast.sticky) {
      this.scheduleAutoDismiss(id, toast.durationMs);
    }

    return id;
  }

  private scheduleAutoDismiss(id: number, ms: number): void {
    setTimeout(() => {
      const t = this.toastsSubject.value.find(x => x.id === id);
      if (!t) return;
      if (t.paused) {
        this.scheduleAutoDismiss(id, 250);
        return;
      }
      const elapsed = t.elapsedBeforePause + (Date.now() - t.createdAt);
      if (elapsed >= t.durationMs) {
        this.dismiss(id);
      } else {
        this.scheduleAutoDismiss(id, t.durationMs - elapsed);
      }
    }, ms);
  }

  pause(id: number): void {
    const list = this.toastsSubject.value.map(t => {
      if (t.id !== id || t.paused) return t;
      return {
        ...t,
        paused: true,
        pausedAt: Date.now(),
        elapsedBeforePause: t.elapsedBeforePause + (Date.now() - t.createdAt)
      };
    });
    this.toastsSubject.next(list);
  }

  resume(id: number): void {
    const list = this.toastsSubject.value.map(t => {
      if (t.id !== id || !t.paused) return t;
      return { ...t, paused: false, createdAt: Date.now() };
    });
    this.toastsSubject.next(list);
  }

  dismiss(id: number): void {
    this.toastsSubject.next(this.toastsSubject.value.filter(t => t.id !== id));
  }

  dismissAll(): void {
    this.toastsSubject.next([]);
  }

  confirm(message: string, opts: ConfirmOptions = {}): Promise<boolean> {
    return new Promise<boolean>((resolve) => {
      const dialog: ConfirmDialog = {
        id: this.nextId++,
        title: opts.title ?? 'Confirm',
        message,
        confirmLabel: opts.confirmLabel ?? 'Confirm',
        cancelLabel: opts.cancelLabel ?? 'Cancel',
        confirmStyle: opts.confirmStyle ?? 'primary',
        resolve
      };
      this.confirmSubject.next(dialog);
    });
  }

  resolveConfirm(ok: boolean): void {
    const c = this.confirmSubject.value;
    if (c) {
      c.resolve(ok);
      this.confirmSubject.next(null);
    }
  }
}

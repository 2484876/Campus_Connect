import { Component, OnInit, OnDestroy, ChangeDetectionStrategy, ChangeDetectorRef, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Subscription } from 'rxjs';
import { ToastService, Toast, ConfirmDialog } from '../../services/toast.service';

@Component({
  selector: 'app-toast-container',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './toast-container.component.html',
  styleUrls: ['./toast-container.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ToastContainerComponent implements OnInit, OnDestroy {

  toasts: Toast[] = [];
  confirmDialog: ConfirmDialog | null = null;
  private subs: Subscription[] = [];
  private tickTimer: any = null;

  constructor(private toast: ToastService, private cdr: ChangeDetectorRef) { }

  ngOnInit(): void {
    this.subs.push(
      this.toast.toasts$.subscribe(list => {
        this.toasts = list;
        this.cdr.markForCheck();
      }),
      this.toast.confirm$.subscribe(c => {
        this.confirmDialog = c;
        this.cdr.markForCheck();
      })
    );
    this.tickTimer = setInterval(() => this.cdr.markForCheck(), 100);
  }

  ngOnDestroy(): void {
    this.subs.forEach(s => s.unsubscribe());
    if (this.tickTimer) clearInterval(this.tickTimer);
  }

  @HostListener('document:keydown.escape')
  onEsc(): void {
    if (this.confirmDialog) {
      this.toast.resolveConfirm(false);
    }
  }

  iconFor(type: string): string {
    if (type === 'success') return '✓';
    if (type === 'error') return '✕';
    if (type === 'warning') return '!';
    return 'i';
  }

  progressPct(t: Toast): number {
    if (t.sticky) return 100;
    const elapsed = t.paused ? t.elapsedBeforePause : t.elapsedBeforePause + (Date.now() - t.createdAt);
    const pct = Math.max(0, 100 - (elapsed / t.durationMs) * 100);
    return pct;
  }

  pause(t: Toast): void { if (!t.sticky) this.toast.pause(t.id); }
  resume(t: Toast): void { if (!t.sticky) this.toast.resume(t.id); }
  dismiss(t: Toast): void { this.toast.dismiss(t.id); }

  runAction(t: Toast): void {
    if (t.action) {
      t.action.onClick();
      this.toast.dismiss(t.id);
    }
  }

  confirmYes(): void { this.toast.resolveConfirm(true); }
  confirmNo(): void { this.toast.resolveConfirm(false); }

  trackById(_: number, t: Toast): number { return t.id; }
}

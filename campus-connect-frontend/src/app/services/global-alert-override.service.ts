import { Injectable } from '@angular/core';
import { ToastService } from './toast.service';

@Injectable({ providedIn: 'root' })
export class GlobalAlertOverride {

  private originalAlert: typeof window.alert;
  private installed = false;

  constructor(private toast: ToastService) {
    this.originalAlert = window.alert.bind(window);
  }

  install(): void {
    if (this.installed) return;
    this.installed = true;

    window.alert = (message?: any): void => {
      const msg = message == null ? '' : String(message);
      const lower = msg.toLowerCase();
      if (lower.includes('fail') || lower.includes('error') || lower.includes('cannot') ||
        lower.includes('invalid') || lower.includes('denied') || lower.includes('wrong') ||
        lower.includes('exceed') || lower.includes('not allowed')) {
        this.toast.error(msg);
      } else if (lower.includes('warn') || lower.includes('expir') || lower.includes('soon') ||
        lower.includes('limit')) {
        this.toast.warning(msg);
      } else if (lower.includes('success') || lower.includes('saved') || lower.includes('sent') ||
        lower.includes('created') || lower.includes('updated') || lower.includes('deleted') ||
        lower.includes('done') || lower.includes('complete') || lower.includes('added')) {
        this.toast.success(msg);
      } else {
        this.toast.info(msg);
      }
    };
  }

  uninstall(): void {
    if (!this.installed) return;
    window.alert = this.originalAlert;
    this.installed = false;
  }
}

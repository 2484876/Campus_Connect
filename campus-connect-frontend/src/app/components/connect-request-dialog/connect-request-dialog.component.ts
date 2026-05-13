import { Component, EventEmitter, Input, Output, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../services/api.service';
import { ConnectionDTO } from '../../models';

@Component({
  selector: 'app-connect-request-dialog',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './connect-request-dialog.component.html',
  styleUrls: ['./connect-request-dialog.component.scss']
})
export class ConnectRequestDialogComponent {
  @Input() userId!: number;
  @Input() userName!: string;
  @Input() userAvatarUrl: string | null = null;
  @Input() position: string | null = null;
  @Output() close = new EventEmitter<void>();
  @Output() sent = new EventEmitter<ConnectionDTO>();

  message = '';
  submitting = false;
  error: string | null = null;

  constructor(private api: ApiService, private cdr: ChangeDetectorRef) { }

  get charCount(): number { return this.message.length; }
  get atLimit(): boolean { return this.message.length >= 300; }

  send(): void {
    if (this.submitting) return;
    this.submitting = true;
    this.error = null;
    this.api.sendConnectionRequest(this.userId, this.message.trim() || undefined).subscribe({
      next: (c) => { this.submitting = false; this.sent.emit(c); },
      error: (err) => {
        this.submitting = false;
        this.error = err?.error?.error || 'Could not send request';
        this.cdr.detectChanges();
      }
    });
  }

  avatar(): string {
    return this.userAvatarUrl || `https://ui-avatars.com/api/?name=${encodeURIComponent(this.userName)}&background=2d5f3f&color=fff`;
  }
}

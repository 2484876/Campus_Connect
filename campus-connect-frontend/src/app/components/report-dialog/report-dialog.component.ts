import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../services/api.service';

@Component({
  selector: 'app-report-dialog',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './report-dialog.component.html',
  styleUrls: ['./report-dialog.component.scss']
})
export class ReportDialogComponent {
  @Input() targetType: 'POST' | 'COMMENT' | 'USER' | 'COMMUNITY_POST' = 'POST';
  @Input() targetId: number = 0;
  @Output() close = new EventEmitter<void>();

  reasons = [
    { value: 'SPAM', label: 'Spam or misleading' },
    { value: 'HARASSMENT', label: 'Harassment or bullying' },
    { value: 'HATE_SPEECH', label: 'Hate speech or discrimination' },
    { value: 'NUDITY', label: 'Nudity or sexual content' },
    { value: 'VIOLENCE', label: 'Violence or dangerous behavior' },
    { value: 'MISINFORMATION', label: 'False information' },
    { value: 'OTHER', label: 'Something else' }
  ];

  selectedReason: string = '';
  details: string = '';
  submitting = false;
  done = false;
  error = '';

  constructor(private api: ApiService) { }

  submit(): void {
    if (!this.selectedReason) return;
    this.submitting = true;
    this.error = '';
    this.api.submitReport({
      targetType: this.targetType,
      targetId: this.targetId,
      reason: this.selectedReason,
      details: this.details
    }).subscribe({
      next: () => { this.done = true; this.submitting = false; },
      error: (err) => {
        this.error = err?.error?.message || 'Could not submit report';
        this.submitting = false;
      }
    });
  }

  closeDialog(): void { this.close.emit(); }
}

import { Component, EventEmitter, Input, Output, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../services/api.service';

interface KudosCategory {
  value: string;
  label: string;
  emoji: string;
  color: string;
}

@Component({
  selector: 'app-kudos-dialog',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './kudos-dialog.component.html',
  styleUrls: ['./kudos-dialog.component.scss']
})
export class KudosDialogComponent {
  @Input() receiverId!: number;
  @Input() receiverName: string = '';
  @Output() close = new EventEmitter<void>();
  @Output() sent = new EventEmitter<void>();

  categories: KudosCategory[] = [
    { value: 'TEAMWORK', label: 'Teamwork', emoji: '🤝', color: '#2d5f3f' },
    { value: 'INNOVATION', label: 'Innovation', emoji: '💡', color: '#c2410c' },
    { value: 'LEADERSHIP', label: 'Leadership', emoji: '⭐', color: '#7c4f17' },
    { value: 'EXCELLENCE', label: 'Excellence', emoji: '🏆', color: '#9a3412' },
    { value: 'HELPFUL', label: 'Helpful', emoji: '🙌', color: '#5d4e75' },
    { value: 'INTEGRITY', label: 'Integrity', emoji: '🛡️', color: '#3d4f3e' },
  ];

  selectedCategory: string = '';
  message: string = '';
  isPublic: boolean = true;
  submitting = false;
  done = false;
  error = '';

  constructor(private api: ApiService, private cdr: ChangeDetectorRef) { }

  submit(): void {
    if (!this.selectedCategory) return;
    this.submitting = true;
    this.error = '';
    this.api.giveKudos({
      receiverId: this.receiverId,
      category: this.selectedCategory,
      message: this.message,
      isPublic: this.isPublic
    }).subscribe({
      next: () => {
        this.done = true;
        this.submitting = false;
        this.sent.emit();
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.error = err?.error?.message || 'Could not send kudos';
        this.submitting = false;
        this.cdr.detectChanges();
      }
    });
  }

  closeDialog(): void { this.close.emit(); }
}

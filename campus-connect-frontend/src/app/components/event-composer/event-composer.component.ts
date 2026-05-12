import { Component, EventEmitter, Output, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../services/api.service';
import { EventDTO, CreateEventRequest } from '../../models';

@Component({
  selector: 'app-event-composer',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './event-composer.component.html',
  styleUrls: ['./event-composer.component.scss']
})
export class EventComposerComponent {
  @Output() close = new EventEmitter<void>();
  @Output() created = new EventEmitter<EventDTO>();

  form: any = {
    title: '',
    description: '',
    eventDate: '',
    eventEndDate: '',
    eventType: 'PHYSICAL',
    category: 'OTHER',
    location: '',
    virtualLink: '',
    maxParticipants: null,
    showAttendees: true,
    coverUrl: null,
    imageUrl: null
  };

  categories = [
    { value: 'WORKSHOP', label: 'Workshop', emoji: '🛠️' },
    { value: 'WEBINAR', label: 'Webinar', emoji: '📺' },
    { value: 'HACKATHON', label: 'Hackathon', emoji: '💻' },
    { value: 'NETWORKING', label: 'Networking', emoji: '🤝' },
    { value: 'TRAINING', label: 'Training', emoji: '📚' },
    { value: 'SOCIAL', label: 'Social', emoji: '🎉' },
    { value: 'CONFERENCE', label: 'Conference', emoji: '🎤' },
    { value: 'OTHER', label: 'Other', emoji: '📌' }
  ];

  submitting = false;
  uploadingCover = false;
  error: string | null = null;

  constructor(private api: ApiService, private cdr: ChangeDetectorRef) { }

  onCoverSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.files?.[0]) return;
    const file = input.files[0];
    if (file.size > 5 * 1024 * 1024) { alert('Image must be under 5MB'); return; }
    this.uploadingCover = true;
    this.api.uploadImage(file).subscribe({
      next: (r) => { this.form.coverUrl = r.url; this.uploadingCover = false; this.cdr.detectChanges(); },
      error: () => { this.uploadingCover = false; alert('Upload failed'); this.cdr.detectChanges(); }
    });
  }

  removeCover(): void { this.form.coverUrl = null; }

  setType(t: 'PHYSICAL' | 'VIRTUAL' | 'HYBRID'): void {
    this.form.eventType = t;
  }

  isValid(): boolean {
    if (!this.form.title?.trim()) return false;
    if (!this.form.eventDate) return false;
    if (this.form.eventType === 'VIRTUAL' && !this.form.virtualLink?.trim()) return false;
    if (this.form.eventType === 'PHYSICAL' && !this.form.location?.trim()) return false;
    return true;
  }

  submit(): void {
    if (!this.isValid() || this.submitting) return;
    this.submitting = true;
    this.error = null;

    const payload: CreateEventRequest = {
      title: this.form.title.trim(),
      description: this.form.description?.trim() || undefined,
      eventDate: this.form.eventDate,
      eventEndDate: this.form.eventEndDate || undefined,
      location: this.form.location?.trim() || undefined,
      virtualLink: this.form.virtualLink?.trim() || undefined,
      eventType: this.form.eventType,
      category: this.form.category,
      coverUrl: this.form.coverUrl || undefined,
      imageUrl: this.form.coverUrl || undefined,
      maxParticipants: this.form.maxParticipants ? Number(this.form.maxParticipants) : undefined,
      showAttendees: this.form.showAttendees
    };

    this.api.createEvent(payload).subscribe({
      next: (e) => { this.submitting = false; this.created.emit(e); },
      error: (err) => {
        this.submitting = false;
        this.error = err?.error?.error || 'Could not create event';
        this.cdr.detectChanges();
      }
    });
  }
}

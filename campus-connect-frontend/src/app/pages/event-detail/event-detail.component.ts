import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule, Router } from '@angular/router';
import { ApiService } from '../../services/api.service';
import { AuthService } from '../../services/auth.service';
import { EventDTO, EventAttendeeDTO, RsvpStatus } from '../../models';
import { EventChatComponent } from '../../components/event-chat/event-chat.component';

@Component({
  selector: 'app-event-detail',
  standalone: true,
  imports: [CommonModule, RouterModule, EventChatComponent],
  templateUrl: './event-detail.component.html',
  styleUrls: ['./event-detail.component.scss']
})
export class EventDetailComponent implements OnInit {
  event: EventDTO | null = null;
  attendees: EventAttendeeDTO[] = [];
  attendeeFilter: RsvpStatus = 'GOING';
  loading = true;
  loadingAttendees = false;
  showChat = false;
  canAccessChat = false;
  attendeesHidden = false;
  myUserId: number;
  isOrganizer = false;
  copied = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private api: ApiService,
    private auth: AuthService,
    private cdr: ChangeDetectorRef
  ) {
    this.myUserId = this.auth.getCurrentUser()?.userId || 0;
  }

  ngOnInit(): void {
    this.route.paramMap.subscribe(p => {
      const id = +(p.get('id') || '0');
      if (id) this.load(id);
    });
  }

  load(id: number): void {
    this.loading = true;
    this.api.getEventById(id).subscribe({
      next: (e) => {
        this.event = e;
        this.isOrganizer = e.createdById === this.myUserId;
        this.loading = false;
        this.cdr.detectChanges();
        this.loadAttendees();
        this.checkChatAccess();
      },
      error: () => { this.loading = false; this.cdr.detectChanges(); }
    });
  }

  loadAttendees(): void {
    if (!this.event) return;
    this.loadingAttendees = true;
    this.api.getEventAttendees(this.event.id, this.attendeeFilter).subscribe({
      next: (list) => {
        this.attendees = list;
        this.attendeesHidden = false;
        this.loadingAttendees = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.attendeesHidden = err?.status === 401 || err?.status === 403;
        this.attendees = [];
        this.loadingAttendees = false;
        this.cdr.detectChanges();
      }
    });
  }

  setAttendeeFilter(s: RsvpStatus): void {
    if (this.attendeeFilter === s) return;
    this.attendeeFilter = s;
    this.loadAttendees();
  }

  checkChatAccess(): void {
    if (!this.event) return;
    this.api.getEventChatAccess(this.event.id).subscribe({
      next: (r) => { this.canAccessChat = r.canAccess; this.cdr.detectChanges(); },
      error: () => { this.canAccessChat = false; this.cdr.detectChanges(); }
    });
  }

  setRsvp(status: RsvpStatus): void {
    if (!this.event) return;
    if (this.event.myRsvpStatus === status) {
      this.api.removeRsvp(this.event.id).subscribe(() => {
        if (this.event) {
          if (status === 'GOING') this.event.goingCount = Math.max(0, this.event.goingCount - 1);
          if (status === 'INTERESTED') this.event.interestedCount = Math.max(0, this.event.interestedCount - 1);
          this.event.myRsvpStatus = null;
        }
        this.canAccessChat = false;
        this.cdr.detectChanges();
        this.loadAttendees();
      });
    } else {
      this.api.setRsvp(this.event.id, status).subscribe({
        next: (updated) => {
          this.event = updated;
          this.cdr.detectChanges();
          this.loadAttendees();
          this.checkChatAccess();
        },
        error: (err) => alert(err?.error?.error || 'Could not RSVP')
      });
    }
  }

  downloadIcs(): void {
    if (!this.event) return;
    const url = this.api.downloadEventIcs(this.event.id);
    const token = sessionStorage.getItem('token');
    fetch(url, { headers: { Authorization: `Bearer ${token}` } })
      .then(r => r.blob())
      .then(blob => {
        const link = document.createElement('a');
        link.href = URL.createObjectURL(blob);
        link.download = `event-${this.event!.id}.ics`;
        link.click();
        setTimeout(() => URL.revokeObjectURL(link.href), 5000);
      })
      .catch(() => alert('Could not download calendar file'));
  }

  copyLink(): void {
    const url = window.location.href;
    navigator.clipboard.writeText(url).then(() => {
      this.copied = true;
      this.cdr.detectChanges();
      setTimeout(() => { this.copied = false; this.cdr.detectChanges(); }, 1500);
    });
  }

  deleteEvent(): void {
    if (!this.event) return;
    if (!confirm('Delete this event? This cannot be undone.')) return;
    this.api.deleteEvent(this.event.id).subscribe(() => {
      this.router.navigate(['/events']);
    });
  }

  toggleChat(): void {
    if (!this.canAccessChat) {
      alert('You need to RSVP "Going" to join the event chat.');
      return;
    }
    this.showChat = !this.showChat;
  }

  avatarFor(name: string, url: string | null): string {
    return url || `https://ui-avatars.com/api/?name=${encodeURIComponent(name)}&background=2d5f3f&color=fff`;
  }

  fullDate(d: string): string {
    return new Date(d).toLocaleDateString('en-IN', {
      weekday: 'long', year: 'numeric', month: 'long', day: 'numeric',
      hour: '2-digit', minute: '2-digit'
    });
  }

  typeLabel(t: string): string {
    if (t === 'VIRTUAL') return '💻 Online event';
    if (t === 'HYBRID') return '🌐 Hybrid event';
    return '📍 In-person';
  }

  categoryEmoji(c: string): string {
    const map: any = {
      WORKSHOP: '🛠️', WEBINAR: '📺', HACKATHON: '💻', NETWORKING: '🤝',
      TRAINING: '📚', SOCIAL: '🎉', CONFERENCE: '🎤', OTHER: '📌'
    };
    return map[c] || '📌';
  }
}

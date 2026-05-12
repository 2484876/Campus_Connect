import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule, Router } from '@angular/router';
import { ApiService } from '../../services/api.service';
import { AuthService } from '../../services/auth.service';
import { EventDTO, EventCategory, RsvpStatus } from '../../models';
import { EventComposerComponent } from '../../components/event-composer/event-composer.component';

type Tab = 'upcoming' | 'this-week' | 'past' | 'mine';

@Component({
  selector: 'app-events',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, EventComposerComponent],
  templateUrl: './events.html',
  styleUrl: './events.scss'
})
export class EventsComponent implements OnInit {
  events: EventDTO[] = [];
  loading = false;
  activeTab: Tab = 'upcoming';
  activeCategory: 'ALL' | EventCategory = 'ALL';
  page = 0;
  hasMore = true;
  showComposer = false;
  myUserId: number;

  categories: { value: 'ALL' | EventCategory, label: string, emoji: string }[] = [
    { value: 'ALL', label: 'All', emoji: '✨' },
    { value: 'WORKSHOP', label: 'Workshop', emoji: '🛠️' },
    { value: 'WEBINAR', label: 'Webinar', emoji: '📺' },
    { value: 'HACKATHON', label: 'Hackathon', emoji: '💻' },
    { value: 'NETWORKING', label: 'Networking', emoji: '🤝' },
    { value: 'TRAINING', label: 'Training', emoji: '📚' },
    { value: 'SOCIAL', label: 'Social', emoji: '🎉' },
    { value: 'CONFERENCE', label: 'Conference', emoji: '🎤' },
    { value: 'OTHER', label: 'Other', emoji: '📌' }
  ];

  constructor(
    private api: ApiService,
    public auth: AuthService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {
    this.myUserId = this.auth.getCurrentUser()?.userId || 0;
  }

  ngOnInit(): void {
    this.load(true);
  }

  setTab(tab: Tab): void {
    if (this.activeTab === tab) return;
    this.activeTab = tab;
    this.activeCategory = 'ALL';
    this.load(true);
  }

  setCategory(cat: 'ALL' | EventCategory): void {
    if (this.activeCategory === cat) return;
    this.activeCategory = cat;
    this.load(true);
  }

  load(reset = false): void {
    if (this.loading) return;
    if (reset) { this.page = 0; this.events = []; this.hasMore = true; }
    if (!this.hasMore) return;
    this.loading = true;

    const stream =
      this.activeCategory !== 'ALL' ? this.api.getEventsByCategory(this.activeCategory, this.page)
        : this.activeTab === 'past' ? this.api.getPastEvents(this.page)
          : this.activeTab === 'this-week' ? this.api.getThisWeekEvents(this.page)
            : this.activeTab === 'mine' ? this.api.getMyEvents(this.page)
              : this.api.getUpcomingEvents(this.page);

    stream.subscribe({
      next: (res) => {
        this.events = reset ? res.content : [...this.events, ...res.content];
        this.hasMore = !res.last;
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: () => { this.loading = false; this.cdr.detectChanges(); }
    });
  }

  loadMore(): void {
    if (this.loading || !this.hasMore) return;
    this.page++;
    this.load();
  }

  openComposer(): void { this.showComposer = true; }
  closeComposer(): void { this.showComposer = false; }
  onEventCreated(e: EventDTO): void {
    this.showComposer = false;
    this.activeTab = 'upcoming';
    this.activeCategory = 'ALL';
    this.load(true);
  }

  setRsvp(event: EventDTO, status: RsvpStatus, e: Event): void {
    e.stopPropagation();
    if (event.myRsvpStatus === status) {
      this.api.removeRsvp(event.id).subscribe(() => {
        event.myRsvpStatus = null;
        if (status === 'GOING') event.goingCount = Math.max(0, event.goingCount - 1);
        if (status === 'INTERESTED') event.interestedCount = Math.max(0, event.interestedCount - 1);
        this.cdr.detectChanges();
      });
    } else {
      this.api.setRsvp(event.id, status).subscribe({
        next: (updated) => { Object.assign(event, updated); this.cdr.detectChanges(); },
        error: (err) => alert(err?.error?.error || 'Could not RSVP')
      });
    }
  }

  openEvent(eventId: number): void {
    this.router.navigate(['/events', eventId]);
  }

  categoryMeta(value: string): { label: string, emoji: string } {
    const c = this.categories.find(x => x.value === value);
    return c ? { label: c.label, emoji: c.emoji } : { label: value, emoji: '📌' };
  }

  formatDateRange(e: EventDTO): string {
    const start = new Date(e.eventDate);
    const opts: Intl.DateTimeFormatOptions = { weekday: 'short', month: 'short', day: 'numeric' };
    const dateStr = start.toLocaleDateString('en-IN', opts);
    const timeStr = start.toLocaleTimeString('en-IN', { hour: '2-digit', minute: '2-digit' });
    return `${dateStr} · ${timeStr}`;
  }

  formatDayMonth(date: string): { day: string, month: string } {
    const d = new Date(date);
    return {
      day: d.getDate().toString(),
      month: d.toLocaleDateString('en-IN', { month: 'short' }).toUpperCase()
    };
  }

  typeIcon(t: string): string {
    if (t === 'VIRTUAL') return '💻';
    if (t === 'HYBRID') return '🌐';
    return '📍';
  }
}

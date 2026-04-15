import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../services/api.service';
import { AuthService } from '../../services/auth.service';
import { EventDTO } from '../../models';

@Component({
  selector: 'app-events',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './events.html',
  styleUrl: './events.scss'
})
export class EventsComponent implements OnInit {
  events: EventDTO[] = [];
  showCreate = false;
  loading = true;
  creating = false;
  form = { title: '', description: '', eventDate: '', location: '', maxParticipants: null as number | null };

  constructor(
    private api: ApiService,
    public auth: AuthService,
    private cdr: ChangeDetectorRef
  ) { }

  ngOnInit(): void {
    this.api.getUpcomingEvents().subscribe({
      next: (res) => {
        this.events = res.content;
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: () => { this.loading = false; this.cdr.detectChanges(); }
    });
  }

  createEvent(): void {
    if (!this.form.title.trim() || !this.form.eventDate) return;
    this.creating = true;
    const payload = {
      ...this.form,
      eventDate: new Date(this.form.eventDate).toISOString()
    };
    this.api.createEvent(payload).subscribe({
      next: (event) => {
        this.events.unshift(event);
        this.showCreate = false;
        this.creating = false;
        this.form = { title: '', description: '', eventDate: '', location: '', maxParticipants: null };
        this.cdr.detectChanges();
      },
      error: () => { this.creating = false; this.cdr.detectChanges(); }
    });
  }

  formatDate(dateStr: string): string {
    const d = new Date(dateStr);
    return d.toLocaleDateString('en-IN', {
      weekday: 'short', year: 'numeric', month: 'short', day: 'numeric',
      hour: '2-digit', minute: '2-digit'
    });
  }

  isUpcoming(dateStr: string): boolean {
    return new Date(dateStr).getTime() > Date.now();
  }
}

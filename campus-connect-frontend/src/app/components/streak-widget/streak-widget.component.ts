import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ApiService } from '../../services/api.service';
import { StreakDTO } from '../../models';

@Component({
  selector: 'app-streak-widget',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './streak-widget.component.html',
  styleUrls: ['./streak-widget.component.scss']
})
export class StreakWidgetComponent implements OnInit {
  streak: StreakDTO | null = null;
  loading = true;
  checkingIn = false;

  constructor(private api: ApiService, private cdr: ChangeDetectorRef) { }

  ngOnInit(): void {
    this.api.getMyStreak().subscribe({
      next: (s) => {
        this.streak = s;
        this.loading = false;
        if (!s.checkedInToday) this.autoCheckIn();
        this.cdr.detectChanges();
      },
      error: () => { this.loading = false; this.cdr.detectChanges(); }
    });
  }

  autoCheckIn(): void {
    this.checkingIn = true;
    this.api.checkInStreak().subscribe({
      next: (s) => { this.streak = s; this.checkingIn = false; this.cdr.detectChanges(); },
      error: () => { this.checkingIn = false; this.cdr.detectChanges(); }
    });
  }
}

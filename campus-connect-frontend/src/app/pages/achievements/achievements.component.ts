import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { ApiService } from '../../services/api.service';
import { AchievementDTO, AchievementStats } from '../../models';

@Component({
  selector: 'app-achievements',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './achievements.component.html',
  styleUrls: ['./achievements.component.scss']
})
export class AchievementsComponent implements OnInit {
  achievements: AchievementDTO[] = [];
  stats: AchievementStats | null = null;
  loading = true;

  constructor(private api: ApiService, private cdr: ChangeDetectorRef) { }

  ngOnInit(): void {
    this.api.getMyAchievements().subscribe({
      next: (list) => { this.achievements = list; this.loading = false; this.cdr.detectChanges(); },
      error: () => { this.loading = false; this.cdr.detectChanges(); }
    });
  }

  earnedCount(): number {
    return this.achievements.filter(a => a.earned).length;
  }

  totalPoints(): number {
    return this.achievements.filter(a => a.earned).reduce((sum, a) => sum + (a.points || 0), 0);
  }

  formatDate(s: string | null): string {
    if (!s) return '';
    return new Date(s).toLocaleDateString();
  }
}

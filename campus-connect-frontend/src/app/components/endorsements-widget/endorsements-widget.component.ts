import { Component, Input, OnChanges, ChangeDetectorRef, EventEmitter, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { ApiService } from '../../services/api.service';
import { AuthService } from '../../services/auth.service';
import { EndorsementSummaryDTO, SkillEndorsementDTO } from '../../models';
import { EndorseDialogComponent } from '../endorse-dialog/endorse-dialog.component';

@Component({
  selector: 'app-endorsements-widget',
  standalone: true,
  imports: [CommonModule, RouterModule, EndorseDialogComponent],
  templateUrl: './endorsements-widget.component.html',
  styleUrls: ['./endorsements-widget.component.scss']
})
export class EndorsementsWidgetComponent implements OnChanges {
  @Input() userId!: number;
  @Input() userName: string = '';
  @Input() userSkills: string[] = [];
  @Input() canEndorse = false;
  @Input() isOwnProfile = false;
  @Output() endorsementAdded = new EventEmitter<void>();

  summary: EndorsementSummaryDTO | null = null;
  recent: SkillEndorsementDTO[] = [];
  loading = true;
  showEndorseDialog = false;
  selectedEndorsement: SkillEndorsementDTO | null = null;
  myId: number;

  categoryLabels: { [k: string]: string } = {
    TECHNICAL: 'Technical',
    LEADERSHIP: 'Leadership',
    TEAMWORK: 'Teamwork',
    COMMUNICATION: 'Communication',
    PROBLEM_SOLVING: 'Problem solving',
    MENTORSHIP: 'Mentorship'
  };

  constructor(
    private api: ApiService,
    private auth: AuthService,
    private cdr: ChangeDetectorRef
  ) {
    this.myId = this.auth.getCurrentUser()?.userId || 0;
  }

  ngOnChanges(): void {
    if (this.userId) this.load();
  }

  load(): void {
    this.loading = true;
    this.api.getEndorsementSummary(this.userId).subscribe({
      next: (s) => { this.summary = s; this.loading = false; this.cdr.detectChanges(); },
      error: () => { this.loading = false; this.cdr.detectChanges(); }
    });
    this.api.getEndorsementsForUser(this.userId, 0).subscribe({
      next: (page) => { this.recent = page.content || []; this.cdr.detectChanges(); },
      error: () => { }
    });
  }

  openDialog(): void {
    if (!this.canEndorse) return;
    this.showEndorseDialog = true;
  }

  closeDialog(): void {
    this.showEndorseDialog = false;
  }

  onEndorsed(): void {
    this.showEndorseDialog = false;
    this.endorsementAdded.emit();
    this.load();
  }

  selectEndorser(e: SkillEndorsementDTO): void {
    this.selectedEndorsement = this.selectedEndorsement?.id === e.id ? null : e;
    this.cdr.detectChanges();
  }

  closeSelected(): void {
    this.selectedEndorsement = null;
    this.cdr.detectChanges();
  }

  maxSkillCount(): number {
    if (!this.summary?.topSkills?.length) return 1;
    return Math.max(...this.summary.topSkills.map(s => s.count));
  }
  maxCatCount(): number {
    if (!this.summary?.categoryBreakdown?.length) return 1;
    return Math.max(...this.summary.categoryBreakdown.map(s => s.count));
  }

  categoryLabel(value: string): string {
    return this.categoryLabels[value] || value;
  }

  avatar(name: string, url: string | null): string {
    return url || `https://ui-avatars.com/api/?name=${encodeURIComponent(name)}&background=1a1a1a&color=fff`;
  }

  endorserTag(e: SkillEndorsementDTO): string {
    if (e.skill) return e.skill;
    if (e.category) return this.categoryLabel(e.category);
    return 'Endorsed';
  }

  timeAgo(dateStr: string): string {
    const s = Math.floor((Date.now() - new Date(dateStr).getTime()) / 1000);
    if (s < 60) return 'just now';
    if (s < 3600) return Math.floor(s / 60) + 'm';
    if (s < 86400) return Math.floor(s / 3600) + 'h';
    if (s < 2592000) return Math.floor(s / 86400) + 'd';
    return Math.floor(s / 2592000) + 'mo';
  }
}

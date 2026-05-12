import { Component, Input, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ApiService } from '../../services/api.service';
import { PollDTO } from '../../models';

@Component({
  selector: 'app-poll-widget',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './poll-widget.component.html',
  styleUrls: ['./poll-widget.component.scss']
})
export class PollWidgetComponent implements OnInit {
  @Input() postId!: number;
  poll: PollDTO | null = null;
  loading = true;
  voting = false;

  constructor(private api: ApiService, private cdr: ChangeDetectorRef) { }

  ngOnInit(): void {
    this.api.getPollForPost(this.postId).subscribe({
      next: (p) => { this.poll = p; this.loading = false; this.cdr.detectChanges(); },
      error: () => { this.loading = false; this.cdr.detectChanges(); }
    });
  }

  vote(optionId: number): void {
    if (!this.poll || this.poll.expired || this.voting) return;
    this.voting = true;
    this.api.votePoll(this.poll.id, optionId).subscribe({
      next: (p) => { this.poll = p; this.voting = false; this.cdr.detectChanges(); },
      error: () => { this.voting = false; this.cdr.detectChanges(); }
    });
  }

  isVoted(optionId: number): boolean {
    return this.poll?.myVotedOptionIds?.includes(optionId) || false;
  }

  expiryLabel(): string {
    if (!this.poll || !this.poll.expiresAt) return '';
    if (this.poll.expired) return 'Poll closed';
    const diff = new Date(this.poll.expiresAt).getTime() - Date.now();
    const hours = Math.floor(diff / 3600000);
    if (hours < 1) return 'Closes soon';
    if (hours < 24) return `${hours}h left`;
    return `${Math.floor(hours / 24)}d left`;
  }
}

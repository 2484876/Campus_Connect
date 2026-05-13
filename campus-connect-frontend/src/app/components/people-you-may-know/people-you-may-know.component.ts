import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { ApiService } from '../../services/api.service';
import { ConnectionSuggestionDTO } from '../../models';
import { ConnectRequestDialogComponent } from '../connect-request-dialog/connect-request-dialog.component';

@Component({
  selector: 'app-people-you-may-know',
  standalone: true,
  imports: [CommonModule, RouterModule, ConnectRequestDialogComponent],
  templateUrl: './people-you-may-know.component.html',
  styleUrls: ['./people-you-may-know.component.scss']
})
export class PeopleYouMayKnowComponent implements OnInit {
  suggestions: ConnectionSuggestionDTO[] = [];
  dismissed = new Set<number>();
  loading = true;
  dialogTarget: ConnectionSuggestionDTO | null = null;

  constructor(private api: ApiService, private cdr: ChangeDetectorRef) { }

  ngOnInit(): void {
    this.api.getConnectionSuggestions(5).subscribe({
      next: (list) => { this.suggestions = list || []; this.loading = false; this.cdr.detectChanges(); },
      error: () => { this.loading = false; this.cdr.detectChanges(); }
    });
  }

  visible(): ConnectionSuggestionDTO[] {
    return this.suggestions.filter(s => !this.dismissed.has(s.userId));
  }

  dismiss(s: ConnectionSuggestionDTO): void {
    this.dismissed.add(s.userId);
    this.cdr.detectChanges();
  }

  openConnect(s: ConnectionSuggestionDTO): void {
    this.dialogTarget = s;
  }

  closeDialog(): void {
    this.dialogTarget = null;
  }

  onSent(): void {
    if (this.dialogTarget) this.dismiss(this.dialogTarget);
    this.dialogTarget = null;
  }

  avatar(s: ConnectionSuggestionDTO): string {
    return s.profilePicUrl || `https://ui-avatars.com/api/?name=${encodeURIComponent(s.name)}&background=2d5f3f&color=fff`;
  }
}

import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { ApiService } from '../../services/api.service';
import { AuthService } from '../../services/auth.service';
import { ProfileCompletionDTO } from '../../models';

@Component({
  selector: 'app-profile-completion',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './profile-completion.component.html',
  styleUrls: ['./profile-completion.component.scss']
})
export class ProfileCompletionComponent implements OnInit {
  data: ProfileCompletionDTO | null = null;
  loading = true;
  myId: number;

  constructor(
    private api: ApiService,
    private auth: AuthService,
    private cdr: ChangeDetectorRef
  ) {
    this.myId = this.auth.getCurrentUser()?.userId || 0;
  }

  ngOnInit(): void {
    this.api.getMyProfileCompletion().subscribe({
      next: (d) => { this.data = d; this.loading = false; this.cdr.detectChanges(); },
      error: () => { this.loading = false; this.cdr.detectChanges(); }
    });
  }

  get visible(): boolean {
    return !!this.data && this.data.percent < 100;
  }
}

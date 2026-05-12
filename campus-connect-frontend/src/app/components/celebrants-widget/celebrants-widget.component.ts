import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { ApiService } from '../../services/api.service';
import { UserDTO } from '../../models';

interface CelebrantItem {
  user: UserDTO;
  isBirthday: boolean;
  isAnniversary: boolean;
  yearsLabel: string | null;
}

@Component({
  selector: 'app-celebrants-widget',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './celebrants-widget.component.html',
  styleUrls: ['./celebrants-widget.component.scss']
})
export class CelebrantsWidgetComponent implements OnInit {
  items: CelebrantItem[] = [];
  loading = true;

  constructor(private api: ApiService, private cdr: ChangeDetectorRef) { }

  ngOnInit(): void {
    this.api.getCelebrants().subscribe({
      next: (users) => {
        const today = new Date();
        this.items = users.map(u => {
          const birthday = u.birthday ? new Date(u.birthday) : null;
          const anniv = u.workAnniversary ? new Date(u.workAnniversary) : null;
          const isBirthday = !!birthday && birthday.getMonth() === today.getMonth() && birthday.getDate() === today.getDate();
          const isAnniversary = !!anniv && anniv.getMonth() === today.getMonth() && anniv.getDate() === today.getDate();
          let yearsLabel: string | null = null;
          if (isAnniversary && anniv) {
            const years = today.getFullYear() - anniv.getFullYear();
            if (years > 0) yearsLabel = years + (years === 1 ? ' year' : ' years');
          }
          return { user: u, isBirthday, isAnniversary, yearsLabel };
        });
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: () => { this.loading = false; this.cdr.detectChanges(); }
    });
  }

  avatar(user: UserDTO): string {
    return user.profilePicUrl || `https://ui-avatars.com/api/?name=${encodeURIComponent(user.name)}&background=2d5f3f&color=fff`;
  }
}

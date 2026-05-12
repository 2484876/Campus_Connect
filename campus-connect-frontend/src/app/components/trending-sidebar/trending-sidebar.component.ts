import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { ApiService } from '../../services/api.service';
import { HashtagDTO } from '../../models';

@Component({
  selector: 'app-trending-sidebar',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './trending-sidebar.component.html',
  styleUrls: ['./trending-sidebar.component.scss']
})
export class TrendingSidebarComponent implements OnInit {
  trending: HashtagDTO[] = [];
  loading = true;

  constructor(private api: ApiService) { }

  ngOnInit(): void {
    this.api.getTrendingHashtags(8).subscribe({
      next: (tags) => { this.trending = tags; this.loading = false; },
      error: () => { this.loading = false; }
    });
  }
}

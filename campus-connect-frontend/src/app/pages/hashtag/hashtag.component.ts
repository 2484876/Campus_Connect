import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule, Router } from '@angular/router';
import { ApiService } from '../../services/api.service';
import { PostDTO } from '../../models';
import { HashtagLinksPipe } from '../../pipes/hashtag-links.pipe';

@Component({
  selector: 'app-hashtag',
  standalone: true,
  imports: [CommonModule, RouterModule, HashtagLinksPipe],
  templateUrl: './hashtag.component.html',
  styleUrls: ['./hashtag.component.scss']
})
export class HashtagComponent implements OnInit {
  tag: string = '';
  posts: PostDTO[] = [];
  loading = true;

  constructor(private route: ActivatedRoute, private api: ApiService, private router: Router) { }

  ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      this.tag = params.get('tag') || '';
      this.load();
    });
  }

  load(): void {
    this.loading = true;
    this.api.getPostsByHashtag(this.tag).subscribe({
      next: (posts) => { this.posts = posts; this.loading = false; },
      error: () => { this.loading = false; }
    });
  }

  formatDate(d: string): string {
    const diffMin = Math.floor((Date.now() - new Date(d).getTime()) / 60000);
    if (diffMin < 1) return 'just now';
    if (diffMin < 60) return diffMin + 'm';
    const h = Math.floor(diffMin / 60);
    if (h < 24) return h + 'h';
    const days = Math.floor(h / 24);
    if (days < 7) return days + 'd';
    return new Date(d).toLocaleDateString();
  }
}

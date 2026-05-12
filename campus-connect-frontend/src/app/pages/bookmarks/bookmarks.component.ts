import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { ApiService } from '../../services/api.service';
import { AuthService } from '../../services/auth.service';
import { PostDTO } from '../../models';
import { HashtagLinksPipe } from '../../pipes/hashtag-links.pipe';

@Component({
  selector: 'app-bookmarks',
  standalone: true,
  imports: [CommonModule, RouterModule, HashtagLinksPipe],
  templateUrl: './bookmarks.component.html',
  styleUrls: ['./bookmarks.component.scss']
})
export class BookmarksComponent implements OnInit {
  posts: PostDTO[] = [];
  loading = true;
  page = 0;
  hasMore = true;
  currentUserId: number | null = null;

  constructor(private api: ApiService, private auth: AuthService, private router: Router) { }

  ngOnInit(): void {
    const u = this.auth.getCurrentUser();
    this.currentUserId = u?.userId || null;
    this.load();
  }

  load(): void {
    this.loading = true;
    this.api.getMyBookmarks(this.page).subscribe({
      next: (res) => {
        this.posts = [...this.posts, ...res.content];
        this.hasMore = !res.last;
        this.loading = false;
      },
      error: () => { this.loading = false; }
    });
  }

  loadMore(): void {
    if (this.hasMore && !this.loading) {
      this.page++;
      this.load();
    }
  }

  removeBookmark(post: PostDTO): void {
    this.api.toggleBookmark(post.id).subscribe(() => {
      this.posts = this.posts.filter(p => p.id !== post.id);
    });
  }

  goToPost(post: PostDTO): void {
    this.router.navigate(['/profile', post.authorId]);
  }

  formatDate(d: string): string {
    const now = new Date();
    const date = new Date(d);
    const diffMs = now.getTime() - date.getTime();
    const diffMin = Math.floor(diffMs / 60000);
    const diffHr = Math.floor(diffMin / 60);
    const diffDay = Math.floor(diffHr / 24);
    if (diffMin < 1) return 'just now';
    if (diffMin < 60) return diffMin + 'm';
    if (diffHr < 24) return diffHr + 'h';
    if (diffDay < 7) return diffDay + 'd';
    return date.toLocaleDateString();
  }
}

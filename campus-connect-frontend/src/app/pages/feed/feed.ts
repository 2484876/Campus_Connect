import { Component, OnInit, HostListener, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { ApiService } from '../../services/api.service';
import { AuthService } from '../../services/auth.service';
import { PostDTO, UserDTO } from '../../models';
import { TrendingSidebarComponent } from '../../components/trending-sidebar/trending-sidebar.component';
import { ReportDialogComponent } from '../../components/report-dialog/report-dialog.component';
import { StreakWidgetComponent } from '../../components/streak-widget/streak-widget.component';
import { CelebrantsWidgetComponent } from '../../components/celebrants-widget/celebrants-widget.component';
import { StoriesComponent } from '../../components/stories/stories.component';
import { PollCreatorComponent, PollDraft } from '../../components/poll-creator/poll-creator.component';
import { PollWidgetComponent } from '../../components/poll-widget/poll-widget.component';
import { HashtagLinksPipe } from '../../pipes/hashtag-links.pipe';

@Component({
  selector: 'app-feed',
  standalone: true,
  imports: [
    CommonModule, FormsModule, RouterModule,
    TrendingSidebarComponent, ReportDialogComponent,
    StreakWidgetComponent, CelebrantsWidgetComponent,
    StoriesComponent, PollCreatorComponent, PollWidgetComponent,
    HashtagLinksPipe
  ],
  templateUrl: './feed.html',
  styleUrl: './feed.scss'
})
export class FeedComponent implements OnInit {
  posts: PostDTO[] = [];
  newPost = '';
  page = 0;
  isLast = false;
  loading = false;
  posting = false;
  currentUserId: number;
  myProfile: UserDTO | null = null;

  selectedImageFile: File | null = null;
  selectedVideoFile: File | null = null;
  imagePreview: string | null = null;
  videoPreview: string | null = null;
  uploading = false;

  openMenuPostId: number | null = null;
  reportPostId: number | null = null;

  showPollCreator = false;
  pendingPoll: PollDraft | null = null;

  constructor(
    private api: ApiService,
    public auth: AuthService,
    private cdr: ChangeDetectorRef
  ) {
    this.currentUserId = this.auth.getCurrentUser()?.userId || 0;
  }

  ngOnInit(): void {
    this.loadProfile();
    this.loadFeed();
  }

  loadProfile(): void {
    this.api.getProfile().subscribe({
      next: (p) => { this.myProfile = p; this.cdr.detectChanges(); },
      error: () => { }
    });
  }

  loadFeed(): void {
    if (this.loading || this.isLast) return;
    this.loading = true;
    this.api.getFeed(this.page).subscribe({
      next: (res) => {
        this.posts = this.page === 0 ? res.content : [...this.posts, ...res.content];
        this.isLast = res.last;
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: () => { this.loading = false; this.cdr.detectChanges(); }
    });
  }

  @HostListener('window:scroll')
  onScroll(): void {
    if (window.innerHeight + window.scrollY >= document.body.offsetHeight - 400 && !this.loading && !this.isLast) {
      this.page++;
      this.loadFeed();
    }
  }

  @HostListener('document:click', ['$event'])
  onDocClick(event: Event): void {
    const target = event.target as HTMLElement;
    if (this.openMenuPostId !== null && !target.closest('.post-menu-wrap')) {
      this.openMenuPostId = null;
      this.cdr.detectChanges();
    }
  }

  togglePostMenu(postId: number, event: Event): void {
    event.stopPropagation();
    this.openMenuPostId = this.openMenuPostId === postId ? null : postId;
  }

  openReport(postId: number): void { this.reportPostId = postId; this.openMenuPostId = null; }
  closeReport(): void { this.reportPostId = null; }

  toggleBookmark(post: PostDTO): void {
    const previous = post.bookmarkedByMe;
    post.bookmarkedByMe = !previous;
    this.cdr.detectChanges();
    this.api.toggleBookmark(post.id).subscribe({
      next: (res) => { post.bookmarkedByMe = res.bookmarked; this.cdr.detectChanges(); },
      error: () => { post.bookmarkedByMe = previous; this.cdr.detectChanges(); }
    });
  }

  onImageSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.files || !input.files[0]) return;
    const file = input.files[0];
    if (!file.type.startsWith('image/')) { alert('Please select an image file'); return; }
    if (file.size > 5 * 1024 * 1024) { alert('Image must be under 5MB'); return; }
    this.selectedImageFile = file;
    this.selectedVideoFile = null;
    this.videoPreview = null;
    const reader = new FileReader();
    reader.onload = () => { this.imagePreview = reader.result as string; this.cdr.detectChanges(); };
    reader.readAsDataURL(file);
  }

  onVideoSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.files || !input.files[0]) return;
    const file = input.files[0];
    if (!file.type.startsWith('video/')) { alert('Please select a video file'); return; }
    if (file.size > 50 * 1024 * 1024) { alert('Video must be under 50MB'); return; }
    this.selectedVideoFile = file;
    this.selectedImageFile = null;
    this.imagePreview = null;
    this.videoPreview = URL.createObjectURL(file);
    this.cdr.detectChanges();
  }

  removeMedia(): void {
    this.selectedImageFile = null;
    this.selectedVideoFile = null;
    this.imagePreview = null;
    this.videoPreview = null;
    this.cdr.detectChanges();
  }

  togglePollCreator(): void {
    this.showPollCreator = !this.showPollCreator;
    if (!this.showPollCreator) this.pendingPoll = null;
  }

  onPollDraft(draft: PollDraft): void {
    this.pendingPoll = draft;
    this.showPollCreator = false;
    this.cdr.detectChanges();
  }

  removePoll(): void {
    this.pendingPoll = null;
  }

  createPost(): void {
    if (!this.newPost.trim() && !this.selectedImageFile && !this.selectedVideoFile && !this.pendingPoll) return;
    if (this.posting) return;
    this.posting = true;
    this.uploading = !!this.selectedImageFile || !!this.selectedVideoFile;
    if (this.selectedImageFile) {
      this.api.uploadImage(this.selectedImageFile).subscribe({
        next: (res) => this.submitPost(res.url, null),
        error: () => { alert('Image upload failed'); this.posting = false; this.uploading = false; this.cdr.detectChanges(); }
      });
    } else if (this.selectedVideoFile) {
      this.api.uploadVideo(this.selectedVideoFile).subscribe({
        next: (res) => this.submitPost(null, res.url),
        error: () => { alert('Video upload failed'); this.posting = false; this.uploading = false; this.cdr.detectChanges(); }
      });
    } else {
      this.submitPost(null, null);
    }
  }

  private submitPost(imageUrl: string | null, videoUrl: string | null): void {
    const payload: any = { content: this.newPost, postType: 'GENERAL' };
    if (imageUrl) payload.imageUrl = imageUrl;
    if (videoUrl) payload.videoUrl = videoUrl;

    this.api.createPost(payload).subscribe({
      next: (post) => {
        if (this.pendingPoll) {
          const expiresAt = new Date(Date.now() + this.pendingPoll.durationHours * 3600000).toISOString();
          this.api.createPoll(post.id, {
            question: this.pendingPoll.question,
            options: this.pendingPoll.options,
            multiChoice: this.pendingPoll.multiChoice,
            expiresAt
          }).subscribe(() => {
            this.posts.unshift(post);
            this.afterPost();
          }, () => this.afterPost());
        } else {
          this.posts.unshift(post);
          this.afterPost();
        }
      },
      error: () => { this.posting = false; this.uploading = false; this.cdr.detectChanges(); }
    });
  }

  private afterPost(): void {
    this.newPost = '';
    this.removeMedia();
    this.pendingPoll = null;
    this.posting = false;
    this.uploading = false;
    this.cdr.detectChanges();
  }

  toggleLike(post: PostDTO): void {
    post.likedByMe = !post.likedByMe;
    post.likeCount += post.likedByMe ? 1 : -1;
    this.cdr.detectChanges();
    this.api.toggleLike(post.id).subscribe({
      next: (res) => { post.likedByMe = res.liked; post.likeCount = res.likeCount; this.cdr.detectChanges(); },
      error: () => { post.likedByMe = !post.likedByMe; post.likeCount += post.likedByMe ? 1 : -1; this.cdr.detectChanges(); }
    });
  }

  toggleComments(post: PostDTO): void {
    post.showComments = !post.showComments;
    if (post.showComments && (!post.recentComments || post.recentComments.length === 0)) {
      this.api.getComments(post.id).subscribe(res => { post.recentComments = res.content; this.cdr.detectChanges(); });
    }
    this.cdr.detectChanges();
  }

  addComment(post: PostDTO): void {
    if (!post.newComment?.trim()) return;
    this.api.addComment(post.id, post.newComment).subscribe(comment => {
      if (!post.recentComments) post.recentComments = [];
      post.recentComments.push(comment);
      post.commentCount++;
      post.newComment = '';
      this.cdr.detectChanges();
    });
  }

  deletePost(post: PostDTO, index: number): void {
    if (confirm('Delete this post?')) {
      this.api.deletePost(post.id).subscribe(() => {
        this.posts.splice(index, 1);
        this.cdr.detectChanges();
      });
    }
  }

  getUserAvatar(name: string): string {
    return `https://ui-avatars.com/api/?name=${encodeURIComponent(name)}&background=2d5f3f&color=fff`;
  }

  getMyAvatar(): string {
    if (this.myProfile?.profilePicUrl) return this.myProfile.profilePicUrl;
    return this.getUserAvatar(this.auth.getCurrentUser()?.name || 'U');
  }

  getRoleBadgeClass(role: string): string {
    return 'badge-role badge-' + role.toLowerCase();
  }

  timeAgo(dateStr: string): string {
    const seconds = Math.floor((Date.now() - new Date(dateStr).getTime()) / 1000);
    if (seconds < 60) return 'just now';
    if (seconds < 3600) return Math.floor(seconds / 60) + 'm';
    if (seconds < 86400) return Math.floor(seconds / 3600) + 'h';
    if (seconds < 604800) return Math.floor(seconds / 86400) + 'd';
    return Math.floor(seconds / 2592000) + 'mo';
  }
}

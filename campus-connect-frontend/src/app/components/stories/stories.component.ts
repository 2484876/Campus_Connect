import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../services/api.service';
import { AuthService } from '../../services/auth.service';
import { UserStoriesGroupDTO, StoryDTO } from '../../models';

@Component({
  selector: 'app-stories',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './stories.component.html',
  styleUrls: ['./stories.component.scss']
})
export class StoriesComponent implements OnInit, OnDestroy {
  groups: UserStoriesGroupDTO[] = [];
  loading = true;

  viewerOpen = false;
  activeGroupIdx = 0;
  activeStoryIdx = 0;
  progress = 0;
  private progressTimer: any = null;
  private storyDurationMs = 5000;

  composerOpen = false;
  composerCaption = '';
  composerMediaUrl: string | null = null;
  composerBg = '#2d5f3f';
  composerFile: File | null = null;
  uploading = false;
  posting = false;

  bgColors = ['#2d5f3f', '#1f4530', '#c2410c', '#92400e', '#4c1d95', '#1e3a8a', '#0f172a', '#7f1d1d'];

  currentUserId: number;
  myAvatar: string = '';

  constructor(private api: ApiService, private auth: AuthService, private cdr: ChangeDetectorRef) {
    this.currentUserId = this.auth.getCurrentUser()?.userId || 0;
  }

  ngOnInit(): void {
    this.load();
    this.api.getProfile().subscribe(p => {
      this.myAvatar = p.profilePicUrl || `https://ui-avatars.com/api/?name=${encodeURIComponent(p.name)}&background=2d5f3f&color=fff`;
      this.cdr.detectChanges();
    });
  }

  ngOnDestroy(): void {
    this.clearTimer();
  }

  load(): void {
    this.loading = true;
    this.api.getStoriesFeed().subscribe({
      next: (g) => { this.groups = g; this.loading = false; this.cdr.detectChanges(); },
      error: () => { this.loading = false; this.cdr.detectChanges(); }
    });
  }

  avatarFor(name: string, url?: string): string {
    return url || `https://ui-avatars.com/api/?name=${encodeURIComponent(name)}&background=2d5f3f&color=fff`;
  }

  openViewer(groupIdx: number): void {
    this.activeGroupIdx = groupIdx;
    this.activeStoryIdx = 0;
    this.viewerOpen = true;
    this.startProgress();
  }

  closeViewer(): void {
    this.viewerOpen = false;
    this.clearTimer();
    this.cdr.detectChanges();
  }

  currentStory(): StoryDTO | null {
    const g = this.groups[this.activeGroupIdx];
    if (!g) return null;
    return g.stories[this.activeStoryIdx] || null;
  }

  startProgress(): void {
    this.clearTimer();
    this.progress = 0;
    const story = this.currentStory();
    if (!story) return;
    if (!story.viewedByMe) {
      this.api.markStoryViewed(story.id).subscribe();
      story.viewedByMe = true;
    }
    const step = 50;
    this.progressTimer = setInterval(() => {
      this.progress += (step / this.storyDurationMs) * 100;
      if (this.progress >= 100) {
        this.next();
      }
      this.cdr.detectChanges();
    }, step);
  }

  clearTimer(): void {
    if (this.progressTimer) {
      clearInterval(this.progressTimer);
      this.progressTimer = null;
    }
  }

  next(): void {
    const g = this.groups[this.activeGroupIdx];
    if (!g) { this.closeViewer(); return; }
    if (this.activeStoryIdx < g.stories.length - 1) {
      this.activeStoryIdx++;
    } else if (this.activeGroupIdx < this.groups.length - 1) {
      this.activeGroupIdx++;
      this.activeStoryIdx = 0;
    } else {
      this.closeViewer();
      return;
    }
    this.startProgress();
  }

  prev(): void {
    if (this.activeStoryIdx > 0) {
      this.activeStoryIdx--;
    } else if (this.activeGroupIdx > 0) {
      this.activeGroupIdx--;
      this.activeStoryIdx = this.groups[this.activeGroupIdx].stories.length - 1;
    }
    this.startProgress();
  }

  deleteStory(): void {
    const s = this.currentStory();
    if (!s || s.userId !== this.currentUserId) return;
    if (!confirm('Delete this story?')) return;
    this.api.deleteStory(s.id).subscribe(() => {
      this.closeViewer();
      this.load();
    });
  }

  openComposer(): void {
    this.composerOpen = true;
    this.composerCaption = '';
    this.composerMediaUrl = null;
    this.composerFile = null;
    this.composerBg = this.bgColors[0];
  }

  closeComposer(): void {
    this.composerOpen = false;
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.files?.[0]) return;
    const file = input.files[0];
    if (file.size > 5 * 1024 * 1024) { alert('Image must be under 5MB'); return; }
    this.composerFile = file;
    this.uploading = true;
    this.api.uploadImage(file).subscribe({
      next: (res) => { this.composerMediaUrl = res.url; this.uploading = false; this.cdr.detectChanges(); },
      error: () => { this.uploading = false; alert('Upload failed'); this.cdr.detectChanges(); }
    });
  }

  removeMedia(): void {
    this.composerMediaUrl = null;
    this.composerFile = null;
  }

  postStory(): void {
    if (!this.composerCaption.trim() && !this.composerMediaUrl) return;
    this.posting = true;
    this.api.createStory({
      caption: this.composerCaption || undefined,
      mediaUrl: this.composerMediaUrl || undefined,
      backgroundColor: this.composerMediaUrl ? undefined : this.composerBg
    }).subscribe({
      next: () => { this.posting = false; this.closeComposer(); this.load(); },
      error: () => { this.posting = false; this.cdr.detectChanges(); }
    });
  }

  formatTime(d: string): string {
    const diffMin = Math.floor((Date.now() - new Date(d).getTime()) / 60000);
    if (diffMin < 1) return 'now';
    if (diffMin < 60) return diffMin + 'm';
    const h = Math.floor(diffMin / 60);
    return h + 'h';
  }
}

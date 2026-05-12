import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterModule, Router } from '@angular/router';
import { ApiService } from '../../services/api.service';
import { AuthService } from '../../services/auth.service';
import { CommunityDTO, CommunityPostDTO, ConnectionDTO, UserDTO, CommunityResourceDTO } from '../../models';
import { ResourceAddDialogComponent } from '../../components/resource-add-dialog/resource-add-dialog.component';

@Component({
  selector: 'app-community-detail',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, ResourceAddDialogComponent],
  templateUrl: './community-detail.component.html',
  styleUrls: ['./community-detail.component.scss']
})
export class CommunityDetailComponent implements OnInit {
  community: CommunityDTO | null = null;
  posts: CommunityPostDTO[] = [];
  members: UserDTO[] = [];
  connections: ConnectionDTO[] = [];
  resources: CommunityResourceDTO[] = [];
  loading = true;
  newPostContent = '';
  postType: 'DISCUSSION' | 'QUESTION' = 'DISCUSSION';
  anonymous = false;
  posting = false;
  uploading = false;
  selectedImage: File | null = null;
  selectedVideo: File | null = null;
  imagePreview: string | null = null;
  videoPreview: string | null = null;
  page = 0;
  isLast = false;
  activeTab: 'posts' | 'questions' | 'resources' | 'about' = 'posts';
  showInviteModal = false;
  showSettingsMenu = false;
  showSettings = false;
  showMembers = false;
  showResourceDialog = false;
  inviting = false;
  resourceQuery = '';
  editForm = { name: '', description: '', isPrivate: false };
  currentUserId: number;

  constructor(
    private api: ApiService,
    public auth: AuthService,
    private route: ActivatedRoute,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {
    this.currentUserId = this.auth.getCurrentUser()?.userId || 0;
  }

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      const id = +params['id'];
      this.loadCommunity(id);
      this.loadPosts(id);
    });
  }

  loadCommunity(id: number): void {
    this.loading = true;
    this.api.getCommunity(id).subscribe({
      next: (c) => {
        this.community = c;
        this.editForm = { name: c.name, description: c.description || '', isPrivate: c.isPrivate };
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: () => { this.loading = false; this.cdr.detectChanges(); }
    });
  }

  loadPosts(communityId: number): void {
    this.api.getCommunityPosts(communityId, this.page).subscribe(res => {
      this.posts = this.page === 0 ? res.content : [...this.posts, ...res.content];
      this.isLast = res.last;
      this.cdr.detectChanges();
    });
  }

  loadMembers(): void {
    if (!this.community) return;
    this.showMembers = true;
    this.api.getCommunityMembers(this.community.id).subscribe(res => {
      this.members = res.content;
      this.cdr.detectChanges();
    });
  }

  loadResources(): void {
    if (!this.community) return;
    this.api.getCommunityResources(this.community.id, this.resourceQuery || undefined).subscribe(res => {
      this.resources = res.content;
      this.cdr.detectChanges();
    });
  }

  setTab(t: 'posts' | 'questions' | 'resources' | 'about'): void {
    this.activeTab = t;
    if (t === 'resources') this.loadResources();
  }

  searchResources(): void {
    this.loadResources();
  }

  filteredPosts(): CommunityPostDTO[] {
    if (this.activeTab === 'questions') return this.posts.filter(p => p.postType === 'QUESTION');
    if (this.activeTab === 'posts') return this.posts.filter(p => p.postType !== 'QUESTION');
    return this.posts;
  }

  onImageSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.files?.[0]) return;
    this.selectedImage = input.files[0];
    this.selectedVideo = null; this.videoPreview = null;
    const reader = new FileReader();
    reader.onload = () => { this.imagePreview = reader.result as string; this.cdr.detectChanges(); };
    reader.readAsDataURL(this.selectedImage);
  }

  onVideoSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.files?.[0]) return;
    this.selectedVideo = input.files[0];
    this.selectedImage = null; this.imagePreview = null;
    this.videoPreview = URL.createObjectURL(this.selectedVideo);
    this.cdr.detectChanges();
  }

  removeMedia(): void {
    this.selectedImage = null; this.selectedVideo = null;
    this.imagePreview = null; this.videoPreview = null;
    this.cdr.detectChanges();
  }

  createPost(): void {
    if (!this.newPostContent.trim() && !this.selectedImage && !this.selectedVideo) return;
    if (!this.community || this.posting) return;
    this.posting = true;
    this.uploading = !!this.selectedImage || !!this.selectedVideo;

    if (this.selectedImage) {
      this.api.uploadImage(this.selectedImage).subscribe({
        next: (res) => this.submitPost(res.url, null),
        error: () => { this.posting = false; this.uploading = false; this.cdr.detectChanges(); }
      });
    } else if (this.selectedVideo) {
      this.api.uploadVideo(this.selectedVideo).subscribe({
        next: (res) => this.submitPost(null, res.url),
        error: () => { this.posting = false; this.uploading = false; this.cdr.detectChanges(); }
      });
    } else {
      this.submitPost(null, null);
    }
  }

  private submitPost(imageUrl: string | null, videoUrl: string | null): void {
    const payload: any = {
      content: this.newPostContent,
      postType: this.postType,
      anonymous: this.anonymous
    };
    if (imageUrl) payload.imageUrl = imageUrl;
    if (videoUrl) payload.videoUrl = videoUrl;
    this.api.createCommunityPost(this.community!.id, payload).subscribe({
      next: (post) => {
        this.posts.unshift(post);
        this.newPostContent = ''; this.removeMedia();
        this.postType = 'DISCUSSION';
        this.anonymous = false;
        this.posting = false; this.uploading = false;
        this.cdr.detectChanges();
      },
      error: () => { this.posting = false; this.uploading = false; this.cdr.detectChanges(); }
    });
  }

  vote(post: CommunityPostDTO, value: number): void {
    this.api.voteCommunityPost(post.id, value).subscribe(res => {
      post.upvotes = res.upvotes; post.downvotes = res.downvotes;
      post.userVote = res.userVote; post.score = post.upvotes - post.downvotes;
      this.cdr.detectChanges();
    });
  }

  deletePost(post: CommunityPostDTO, index: number): void {
    if (!confirm('Delete this post?')) return;
    this.api.deleteCommunityPost(post.id).subscribe(() => {
      this.posts.splice(index, 1); this.cdr.detectChanges();
    });
  }

  unmaskPost(post: CommunityPostDTO): void {
    if (!confirm('Reveal yourself as the author of this anonymous post?')) return;
    this.api.unmaskAnonymous(post.id).subscribe(updated => {
      Object.assign(post, updated);
      this.cdr.detectChanges();
    });
  }

  deleteResource(resource: CommunityResourceDTO): void {
    if (!this.community || !confirm('Delete this resource?')) return;
    this.api.deleteCommunityResource(this.community.id, resource.id).subscribe(() => {
      this.resources = this.resources.filter(r => r.id !== resource.id);
      this.cdr.detectChanges();
    });
  }

  openResource(r: CommunityResourceDTO): void {
    if (!this.community) return;
    this.api.trackResourceClick(this.community.id, r.id).subscribe(() => {
      r.clickCount = (r.clickCount || 0) + 1;
      this.cdr.detectChanges();
    });
    window.open(r.url, '_blank');
  }

  onResourceAdded(r: CommunityResourceDTO): void {
    this.resources.unshift(r);
    this.showResourceDialog = false;
    this.cdr.detectChanges();
  }

  join(): void {
    if (!this.community) return;
    this.api.joinCommunity(this.community.id).subscribe(c => { this.community = c; this.cdr.detectChanges(); });
  }

  leave(): void {
    if (!this.community || !confirm('Leave this community?')) return;
    this.api.leaveCommunity(this.community.id).subscribe(() => {
      this.router.navigate(['/communities']);
    });
  }

  deleteCommunity(): void {
    if (!this.community || !confirm('Permanently delete "' + this.community.name + '"? This cannot be undone.')) return;
    this.api.deleteCommunity(this.community.id).subscribe(() => {
      this.router.navigate(['/communities']);
    });
  }

  onIconUpload(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.files?.[0] || !this.community) return;
    this.api.uploadCommunityIcon(this.community.id, input.files[0]).subscribe(c => {
      this.community = c; this.cdr.detectChanges();
    });
  }

  onBannerUpload(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.files?.[0] || !this.community) return;
    this.api.uploadCommunityBanner(this.community.id, input.files[0]).subscribe(c => {
      this.community = c; this.cdr.detectChanges();
    });
  }

  saveSettings(): void {
    if (!this.community) return;
    this.api.updateCommunity(this.community.id, this.editForm).subscribe(c => {
      this.community = c; this.showSettings = false; this.cdr.detectChanges();
    });
  }

  openInvite(): void {
    this.showInviteModal = true;
    this.api.getConnections().subscribe(res => { this.connections = res.content; this.cdr.detectChanges(); });
  }

  invite(conn: ConnectionDTO): void {
    if (!this.community) return;
    this.inviting = true;
    this.api.inviteToCommunity(this.community.id, conn.userId).subscribe({
      next: () => { alert('Invite sent to ' + conn.userName); this.inviting = false; this.cdr.detectChanges(); },
      error: (err: any) => { alert(err.error?.error || 'Failed'); this.inviting = false; this.cdr.detectChanges(); }
    });
  }

  promoteMember(member: UserDTO): void {
    if (!this.community) return;
    const role = member.role === 'ADMIN' ? 'MEMBER' : 'ADMIN';
    this.api.updateMemberRole(this.community.id, member.id, role).subscribe(() => {
      member.role = role; this.cdr.detectChanges();
    });
  }

  removeMember(member: UserDTO): void {
    if (!this.community || !confirm('Remove ' + member.name + '?')) return;
    this.api.removeMember(this.community.id, member.id).subscribe(() => {
      this.members = this.members.filter(m => m.id !== member.id);
      if (this.community) this.community.memberCount--;
      this.cdr.detectChanges();
    });
  }

  isOwnerOrAdmin(): boolean {
    return this.community?.memberRole === 'OWNER' || this.community?.memberRole === 'ADMIN';
  }

  getIcon(name: string): string {
    return `https://ui-avatars.com/api/?name=${encodeURIComponent(name)}&background=2d5f3f&color=fff&bold=true&size=120`;
  }

  getAvatar(name: string): string {
    return `https://ui-avatars.com/api/?name=${encodeURIComponent(name)}&background=2d5f3f&color=fff`;
  }

  getAnonAvatar(): string {
    return `https://ui-avatars.com/api/?name=A&background=64748b&color=fff`;
  }

  formatFileSize(bytes: number | null): string {
    if (!bytes) return '';
    if (bytes < 1024) return bytes + ' B';
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(0) + ' KB';
    return (bytes / (1024 * 1024)).toFixed(1) + ' MB';
  }

  timeAgo(dateStr: string): string {
    const s = Math.floor((Date.now() - new Date(dateStr).getTime()) / 1000);
    if (s < 60) return 'just now'; if (s < 3600) return Math.floor(s / 60) + 'm';
    if (s < 86400) return Math.floor(s / 3600) + 'h'; return Math.floor(s / 86400) + 'd';
  }
}

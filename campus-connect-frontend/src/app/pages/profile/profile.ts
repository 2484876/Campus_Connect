import { Component, OnInit, ChangeDetectorRef, HostListener, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterModule, Router } from '@angular/router';
import { ApiService } from '../../services/api.service';
import { AuthService } from '../../services/auth.service';
import { UserDTO, PostDTO, ExperienceDTO, KudosDTO, AchievementDTO } from '../../models';
import { KudosDialogComponent } from '../../components/kudos-dialog/kudos-dialog.component';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, KudosDialogComponent],
  templateUrl: './profile.html',
  styleUrls: ['./profile.scss']
})
export class ProfileComponent implements OnInit {
  user: UserDTO | null = null;
  posts: PostDTO[] = [];
  receivedKudos: KudosDTO[] = [];
  achievements: AchievementDTO[] = [];
  isOwnProfile = false;
  editing = false;
  showAddExperience = false;
  editForm: any = {};
  expForm: any = { title: '', employmentType: 'Full-time', company: 'Cognizant', location: '', startDate: '', endDate: '', isCurrent: false, description: '' };
  loading = true;
  uploadingAvatar = false;
  uploadingBanner = false;
  currentUserName = '';
  profileMenuOpen = false;
  openExpMenuIndex: number | null = null;
  showKudosDialog = false;
  selectedKudos: KudosDTO | null = null;

  @ViewChild('kudosStrip') kudosStripRef?: ElementRef<HTMLDivElement>;

  roleLabels: any = {
    PROGRAMMER_ANALYST_TRAINEE: 'Programmer Analyst Trainee',
    PROGRAMMER_ANALYST: 'Programmer Analyst',
    ASSOCIATE: 'Associate',
    SENIOR_ASSOCIATE: 'Senior Associate',
    MANAGER: 'Manager',
    SENIOR_MANAGER: 'Senior Manager',
    ASSOCIATE_DIRECTOR: 'Associate Director',
    DIRECTOR: 'Director',
    SENIOR_DIRECTOR: 'Senior Director',
    AVP: 'Associate Vice President',
    VP: 'Vice President',
    SVP: 'Senior Vice President',
    ADMIN: 'Admin'
  };

  categoryEmojis: any = {
    TEAMWORK: '🤝', INNOVATION: '💡', LEADERSHIP: '⭐',
    EXCELLENCE: '🏆', HELPFUL: '🙌', INTEGRITY: '🛡️'
  };

  constructor(
    private api: ApiService,
    private auth: AuthService,
    private route: ActivatedRoute,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {
    this.currentUserName = this.auth.getCurrentUser()?.name || 'U';
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent): void {
    const target = event.target as HTMLElement;
    if (!target.closest('.profile-kebab')) this.profileMenuOpen = false;
    if (!target.closest('.exp-kebab')) this.openExpMenuIndex = null;
    this.cdr.detectChanges();
  }

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      const id = +params['id'];
      this.isOwnProfile = id === this.auth.getCurrentUser()?.userId;
      this.loading = true;
      this.api.getUser(id).subscribe({
        next: (user) => {
          this.user = user;
          this.editForm = {
            name: user.name, department: user.department, position: user.position,
            bio: user.bio, phone: user.phone, skills: user.skills || [],
            birthday: user.birthday ? user.birthday.substring(0, 10) : '',
            workAnniversary: user.workAnniversary ? user.workAnniversary.substring(0, 10) : ''
          };
          this.loading = false;
          this.cdr.detectChanges();
        },
        error: () => { this.loading = false; this.cdr.detectChanges(); }
      });
      this.loadUserPosts(id);
      this.loadKudos(id);
      this.loadAchievements(id);
    });
  }

  loadUserPosts(userId: number): void {
    this.api.getFeed(0, 50).subscribe(res => {
      this.posts = res.content.filter(p => p.authorId === userId);
      this.cdr.detectChanges();
    });
  }

  loadKudos(userId: number): void {
    this.api.getKudosReceived(userId, 0).subscribe(res => {
      this.receivedKudos = res.content;
      this.cdr.detectChanges();
    });
  }

  loadAchievements(userId: number): void {
    this.api.getAchievementsForUser(userId).subscribe(list => {
      this.achievements = list;
      this.cdr.detectChanges();
    });
  }

  onAvatarSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.files?.[0]) return;
    this.uploadingAvatar = true; this.cdr.detectChanges();
    this.api.updateAvatar(input.files[0]).subscribe({
      next: (u) => { this.user = u; this.uploadingAvatar = false; this.cdr.detectChanges(); },
      error: () => { this.uploadingAvatar = false; this.cdr.detectChanges(); }
    });
  }

  onBannerSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.files?.[0]) return;
    this.uploadingBanner = true; this.cdr.detectChanges();
    this.api.uploadUserBanner(input.files[0]).subscribe({
      next: (u) => { this.user = u; this.uploadingBanner = false; this.cdr.detectChanges(); },
      error: () => { this.uploadingBanner = false; this.cdr.detectChanges(); }
    });
  }

  toggleProfileMenu(): void {
    this.profileMenuOpen = !this.profileMenuOpen;
    this.openExpMenuIndex = null;
    this.cdr.detectChanges();
  }

  openEditProfile(): void {
    this.editing = !this.editing;
    this.profileMenuOpen = false;
    this.cdr.detectChanges();
  }

  saveProfile(): void {
    const payload = { ...this.editForm };
    if (!payload.birthday) payload.birthday = null;
    if (!payload.workAnniversary) payload.workAnniversary = null;
    this.api.updateProfile(payload).subscribe(u => {
      this.user = u; this.editing = false; this.cdr.detectChanges();
    });
  }

  toggleExpMenu(i: number): void {
    this.openExpMenuIndex = this.openExpMenuIndex === i ? null : i;
    this.profileMenuOpen = false;
    this.cdr.detectChanges();
  }

  addExperience(): void {
    if (!this.expForm.title.trim()) return;
    this.api.addExperience(this.expForm).subscribe(exp => {
      if (this.user) {
        if (!this.user.experiences) this.user.experiences = [];
        this.user.experiences.unshift(exp);
      }
      this.showAddExperience = false;
      this.expForm = { title: '', employmentType: 'Full-time', company: 'Cognizant', location: '', startDate: '', endDate: '', isCurrent: false, description: '' };
      this.cdr.detectChanges();
    });
  }

  deleteExperience(exp: ExperienceDTO, index: number): void {
    if (!confirm('Remove this experience?')) return;
    this.openExpMenuIndex = null;
    this.api.deleteExperience(exp.id).subscribe(() => {
      this.user?.experiences?.splice(index, 1);
      this.cdr.detectChanges();
    });
  }

  viewRole(role: string): void {
    this.router.navigate(['/connections'], { queryParams: { q: role } });
  }

  connect(): void {
    if (this.user) this.api.sendConnectionRequest(this.user.id).subscribe(() => { if (this.user) this.user.connectionStatus = 'PENDING'; this.cdr.detectChanges(); });
  }

  message(): void { if (this.user) this.router.navigate(['/chat', this.user.id]); }

  openKudos(): void { this.showKudosDialog = true; }
  closeKudos(): void { this.showKudosDialog = false; }
  onKudosSent(): void {
    if (this.user) this.loadKudos(this.user.id);
  }

  openKudosDetail(k: KudosDTO): void {
    this.selectedKudos = k;
    this.cdr.detectChanges();
  }

  closeKudosDetail(): void {
    this.selectedKudos = null;
    this.cdr.detectChanges();
  }

  scrollKudos(direction: number): void {
    const el = this.kudosStripRef?.nativeElement;
    if (!el) return;
    el.scrollBy({ left: direction * 240, behavior: 'smooth' });
  }

  getFirstName(name: string): string {
    if (!name) return '';
    return name.split(' ')[0];
  }

  getRoleLabel(role: string): string { return this.roleLabels[role] || role; }
  getAvatar(name: string): string { return `https://ui-avatars.com/api/?name=${encodeURIComponent(name)}&background=2d5f3f&color=fff&size=120`; }
  getRoleBadgeClass(role: string): string { return 'badge-role badge-' + role.toLowerCase(); }
  getCategoryEmoji(cat: string): string { return this.categoryEmojis[cat] || '⭐'; }

  earnedAchievements(): AchievementDTO[] {
    return this.achievements.filter(a => a.earned);
  }

  toggleLike(post: PostDTO): void {
    post.likedByMe = !post.likedByMe; post.likeCount += post.likedByMe ? 1 : -1; this.cdr.detectChanges();
    this.api.toggleLike(post.id).subscribe({
      next: (r) => { post.likedByMe = r.liked; post.likeCount = r.likeCount; this.cdr.detectChanges(); },
      error: () => { post.likedByMe = !post.likedByMe; post.likeCount += post.likedByMe ? 1 : -1; this.cdr.detectChanges(); }
    });
  }

  toggleComments(post: PostDTO): void {
    post.showComments = !post.showComments;
    if (post.showComments && (!post.recentComments || !post.recentComments.length))
      this.api.getComments(post.id).subscribe(r => { post.recentComments = r.content; this.cdr.detectChanges(); });
    this.cdr.detectChanges();
  }

  addComment(post: PostDTO): void {
    if (!post.newComment?.trim()) return;
    this.api.addComment(post.id, post.newComment).subscribe(c => {
      if (!post.recentComments) post.recentComments = [];
      post.recentComments.push(c);
      post.commentCount++;
      post.newComment = '';
      this.cdr.detectChanges();
    });
  }

  timeAgo(dateStr: string): string {
    const s = Math.floor((Date.now() - new Date(dateStr).getTime()) / 1000);
    if (s < 60) return 'just now'; if (s < 3600) return Math.floor(s / 60) + 'm ago';
    if (s < 86400) return Math.floor(s / 3600) + 'h ago'; return Math.floor(s / 86400) + 'd ago';
  }

  formatDate(d: string): string { return d ? new Date(d).toLocaleDateString('en-IN', { month: 'short', year: 'numeric' }) : ''; }

  formatBirthdayDisplay(d: string | null): string {
    if (!d) return '';
    return new Date(d).toLocaleDateString('en-IN', { month: 'long', day: 'numeric' });
  }
}

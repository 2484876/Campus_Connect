import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { ApiService } from '../../services/api.service';
import { AuthService } from '../../services/auth.service';
import { CommunityPostDTO, CommunityCommentDTO } from '../../models';

@Component({
  selector: 'app-community-post',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './community-post.component.html',
  styleUrls: ['./community-post.component.scss']
})
export class CommunityPostComponent implements OnInit {
  post: CommunityPostDTO | null = null;
  comments: CommunityCommentDTO[] = [];
  loading = true;
  newComment = '';
  commenting = false;
  communityId = 0;

  constructor(
    private api: ApiService,
    public auth: AuthService,
    private route: ActivatedRoute,
    private cdr: ChangeDetectorRef
  ) { }

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      this.communityId = +params['id'];
      const postId = +params['postId'];
      this.api.getCommunityPost(postId).subscribe({
        next: (p) => { this.post = p; this.loading = false; this.cdr.detectChanges(); },
        error: () => { this.loading = false; this.cdr.detectChanges(); }
      });
      this.api.getCommunityComments(postId).subscribe(res => {
        this.comments = res.content;
        this.cdr.detectChanges();
      });
    });
  }

  votePost(value: number): void {
    if (!this.post) return;
    this.api.voteCommunityPost(this.post.id, value).subscribe(res => {
      if (this.post) {
        this.post.upvotes = res.upvotes;
        this.post.downvotes = res.downvotes;
        this.post.userVote = res.userVote;
        this.post.score = this.post.upvotes - this.post.downvotes;
      }
      this.cdr.detectChanges();
    });
  }

  addComment(): void {
    if (!this.newComment.trim() || !this.post || this.commenting) return;
    this.commenting = true;
    this.api.addCommunityComment(this.post.id, { content: this.newComment }).subscribe({
      next: (c) => {
        this.comments.unshift(c);
        this.newComment = '';
        this.commenting = false;
        if (this.post) this.post.commentCount++;
        this.cdr.detectChanges();
      },
      error: () => { this.commenting = false; this.cdr.detectChanges(); }
    });
  }

  addReply(comment: CommunityCommentDTO): void {
    if (!comment.replyContent?.trim() || !this.post) return;
    this.api.addCommunityComment(this.post.id, {
      content: comment.replyContent,
      parentCommentId: comment.id
    }).subscribe(reply => {
      if (!comment.replies) comment.replies = [];
      comment.replies.push(reply);
      comment.replyCount++;
      comment.replyContent = '';
      comment.showReplyInput = false;
      this.cdr.detectChanges();
    });
  }

  loadReplies(comment: CommunityCommentDTO): void {
    comment.showReplies = !comment.showReplies;
    this.cdr.detectChanges();
  }

  voteComment(comment: CommunityCommentDTO, value: number): void {
    this.api.voteCommunityComment(comment.id, value).subscribe(res => {
      comment.upvotes = res.upvotes;
      comment.downvotes = res.downvotes;
      comment.userVote = res.userVote;
      comment.score = comment.upvotes - comment.downvotes;
      this.cdr.detectChanges();
    });
  }

  getAvatar(name: string): string {
    return `https://ui-avatars.com/api/?name=${encodeURIComponent(name)}&background=0a66c2&color=fff`;
  }

  timeAgo(dateStr: string): string {
    const s = Math.floor((Date.now() - new Date(dateStr).getTime()) / 1000);
    if (s < 60) return 'just now';
    if (s < 3600) return Math.floor(s / 60) + 'm';
    if (s < 86400) return Math.floor(s / 3600) + 'h';
    return Math.floor(s / 86400) + 'd';
  }
}

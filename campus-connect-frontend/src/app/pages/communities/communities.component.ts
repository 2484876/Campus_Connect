import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { ApiService } from '../../services/api.service';
import { CommunityDTO } from '../../models';

@Component({
  selector: 'app-communities',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './communities.component.html',
  styleUrls: ['./communities.component.scss']
})
export class CommunitiesComponent implements OnInit {
  myCommunities: CommunityDTO[] = [];
  allCommunities: CommunityDTO[] = [];
  searchResults: CommunityDTO[] = [];
  searchQuery = '';
  activeTab = 'discover';
  showCreate = false;
  creating = false;
  loading = true;
  form = { name: '', description: '', isPrivate: false };

  constructor(private api: ApiService, private cdr: ChangeDetectorRef) { }

  ngOnInit(): void {
    this.loadAll();
  }

  loadAll(): void {
    this.loading = true;
    this.api.getMyCommunities().subscribe(res => {
      this.myCommunities = res.content;
      this.cdr.detectChanges();
    });
    this.api.getAllCommunities().subscribe(res => {
      this.allCommunities = res.content;
      this.loading = false;
      this.cdr.detectChanges();
    });
  }

  onSearch(): void {
    if (!this.searchQuery.trim()) { this.searchResults = []; this.cdr.detectChanges(); return; }
    this.api.searchCommunities(this.searchQuery).subscribe(res => {
      this.searchResults = res.content;
      this.cdr.detectChanges();
    });
  }

  createCommunity(): void {
    if (!this.form.name.trim()) return;
    this.creating = true;
    this.api.createCommunity(this.form).subscribe({
      next: (c) => {
        this.myCommunities.unshift(c);
        this.allCommunities.unshift(c);
        this.showCreate = false;
        this.creating = false;
        this.form = { name: '', description: '', isPrivate: false };
        this.cdr.detectChanges();
      },
      error: () => { this.creating = false; this.cdr.detectChanges(); }
    });
  }

  join(c: CommunityDTO): void {
    this.api.joinCommunity(c.id).subscribe(updated => {
      c.isMember = true;
      c.memberCount = updated.memberCount;
      this.myCommunities.push(c);
      this.cdr.detectChanges();
    });
  }

  leave(c: CommunityDTO, index: number): void {
    if (!confirm('Leave ' + c.name + '?')) return;
    this.api.leaveCommunity(c.id).subscribe(() => {
      c.isMember = false;
      c.memberCount--;
      this.myCommunities = this.myCommunities.filter(m => m.id !== c.id);
      this.cdr.detectChanges();
    });
  }

  getIcon(name: string): string {
    return `https://ui-avatars.com/api/?name=${encodeURIComponent(name)}&background=7b2cbf&color=fff&bold=true`;
  }
}

import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule, ActivatedRoute, Router } from '@angular/router';
import { ApiService } from '../../services/api.service';
import { UserDTO, SkillSuggestion } from '../../models';

@Component({
  selector: 'app-skill-search',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './skill-search.component.html',
  styleUrls: ['./skill-search.component.scss']
})
export class SkillSearchComponent implements OnInit {
  query = '';
  results: UserDTO[] = [];
  trending: SkillSuggestion[] = [];
  autocomplete: SkillSuggestion[] = [];
  showAutocomplete = false;
  loading = false;
  hasMore = false;
  page = 0;

  constructor(
    private api: ApiService,
    private route: ActivatedRoute,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) { }

  ngOnInit(): void {
    this.api.getTrendingSkills(12).subscribe({
      next: (list) => { this.trending = list || []; this.cdr.detectChanges(); },
      error: () => { }
    });
    this.route.queryParams.subscribe(p => {
      if (p['skill']) {
        this.query = p['skill'];
        this.search();
      }
    });
  }

  onQueryChange(): void {
    if (this.query.length < 2) {
      this.autocomplete = [];
      this.showAutocomplete = false;
      this.cdr.detectChanges();
      return;
    }
    this.api.autocompleteSkills(this.query, 8).subscribe({
      next: (list) => {
        this.autocomplete = list || [];
        this.showAutocomplete = list.length > 0;
        this.cdr.detectChanges();
      }
    });
  }

  pickSuggestion(s: SkillSuggestion): void {
    this.query = s.skill;
    this.showAutocomplete = false;
    this.search();
  }

  pickTrending(s: SkillSuggestion): void {
    this.query = s.skill;
    this.search();
  }

  search(): void {
    if (!this.query.trim()) return;
    this.showAutocomplete = false;
    this.page = 0;
    this.results = [];
    this.loading = true;
    this.router.navigate([], { queryParams: { skill: this.query }, replaceUrl: true });
    this.fetch();
  }

  fetch(): void {
    this.api.searchUsersBySkill(this.query.trim(), this.page).subscribe({
      next: (res) => {
        this.results = this.page === 0 ? res.content : [...this.results, ...res.content];
        this.hasMore = !res.last;
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: () => { this.loading = false; this.cdr.detectChanges(); }
    });
  }

  loadMore(): void {
    if (this.loading || !this.hasMore) return;
    this.page++;
    this.loading = true;
    this.fetch();
  }

  avatar(name: string, url: string | null | undefined): string {
    return url || `https://ui-avatars.com/api/?name=${encodeURIComponent(name)}&background=2d5f3f&color=fff`;
  }
}

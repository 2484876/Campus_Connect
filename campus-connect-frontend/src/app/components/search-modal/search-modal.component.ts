import { Component, EventEmitter, Output, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule, Router } from '@angular/router';
import { ApiService } from '../../services/api.service';
import { SearchResultDTO } from '../../models';
import { Subject, debounceTime, distinctUntilChanged, switchMap, of, catchError } from 'rxjs';

@Component({
  selector: 'app-search-modal',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './search-modal.component.html',
  styleUrls: ['./search-modal.component.scss']
})
export class SearchModalComponent implements OnDestroy {
  @Output() close = new EventEmitter<void>();
  query: string = '';
  results: SearchResultDTO | null = null;
  loading = false;
  private query$ = new Subject<string>();

  constructor(private api: ApiService, private router: Router) {
    this.query$.pipe(
      debounceTime(250),
      distinctUntilChanged(),
      switchMap(q => {
        if (!q || q.trim().length < 2) {
          this.results = null;
          return of(null);
        }
        this.loading = true;
        return this.api.universalSearch(q.trim()).pipe(catchError(() => of(null)));
      })
    ).subscribe(res => {
      this.results = res;
      this.loading = false;
    });
  }

  onInput(): void { this.query$.next(this.query); }

  closeModal(): void { this.close.emit(); }

  go(path: any[]): void {
    this.closeModal();
    this.router.navigate(path);
  }

  hasResults(): boolean {
    if (!this.results) return false;
    return (this.results.users?.length || 0) > 0
      || (this.results.communities?.length || 0) > 0
      || (this.results.hashtags?.length || 0) > 0;
  }

  ngOnDestroy(): void { this.query$.complete(); }
}

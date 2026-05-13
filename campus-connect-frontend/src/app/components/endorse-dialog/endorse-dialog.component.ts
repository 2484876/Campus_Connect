import { Component, EventEmitter, Input, Output, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../services/api.service';
import { SkillEndorsementDTO, EndorsementCategory } from '../../models';

@Component({
  selector: 'app-endorse-dialog',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './endorse-dialog.component.html',
  styleUrls: ['./endorse-dialog.component.scss']
})
export class EndorseDialogComponent implements OnInit {
  @Input() endorseeId!: number;
  @Input() endorseeName!: string;
  @Input() endorseeSkills: string[] = [];
  @Output() close = new EventEmitter<void>();
  @Output() endorsed = new EventEmitter<SkillEndorsementDTO>();

  mode: 'SKILL' | 'CATEGORY' = 'SKILL';
  selectedSkill: string | null = null;
  customSkill = '';
  selectedCategory: EndorsementCategory | null = null;
  message = '';
  submitting = false;
  error: string | null = null;

  categories: { value: EndorsementCategory, label: string }[] = [
    { value: 'TECHNICAL', label: 'Technical' },
    { value: 'LEADERSHIP', label: 'Leadership' },
    { value: 'TEAMWORK', label: 'Teamwork' },
    { value: 'COMMUNICATION', label: 'Communication' },
    { value: 'PROBLEM_SOLVING', label: 'Problem solving' },
    { value: 'MENTORSHIP', label: 'Mentorship' }
  ];

  constructor(private api: ApiService, private cdr: ChangeDetectorRef) { }

  ngOnInit(): void {
    if (this.endorseeSkills.length === 0) this.mode = 'CATEGORY';
  }

  setMode(m: 'SKILL' | 'CATEGORY'): void {
    this.mode = m;
    this.selectedSkill = null;
    this.customSkill = '';
    this.selectedCategory = null;
    this.error = null;
  }

  pickSkill(s: string): void {
    this.selectedSkill = s;
    this.customSkill = '';
  }

  pickCategory(c: EndorsementCategory): void {
    this.selectedCategory = c;
  }

  isValid(): boolean {
    if (this.mode === 'SKILL') {
      return !!(this.selectedSkill || this.customSkill.trim());
    }
    return !!this.selectedCategory;
  }

  submit(): void {
    if (!this.isValid() || this.submitting) return;
    this.submitting = true;
    this.error = null;

    const payload: any = { endorseeId: this.endorseeId };
    if (this.mode === 'SKILL') {
      payload.skill = (this.selectedSkill || this.customSkill).trim();
    } else {
      payload.category = this.selectedCategory;
    }
    if (this.message.trim()) payload.message = this.message.trim();

    this.api.createEndorsement(payload).subscribe({
      next: (e) => { this.submitting = false; this.endorsed.emit(e); },
      error: (err) => {
        this.submitting = false;
        this.error = err?.error?.error || 'Could not endorse';
        this.cdr.detectChanges();
      }
    });
  }
}

import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

export interface PollDraft {
  question: string;
  options: string[];
  multiChoice: boolean;
  durationHours: number;
}

@Component({
  selector: 'app-poll-creator',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './poll-creator.component.html',
  styleUrls: ['./poll-creator.component.scss']
})
export class PollCreatorComponent {
  @Output() close = new EventEmitter<void>();
  @Output() draft = new EventEmitter<PollDraft>();

  question = '';
  options: string[] = ['', ''];
  multiChoice = false;
  durationHours = 24;

  addOption(): void {
    if (this.options.length < 6) this.options.push('');
  }

  removeOption(i: number): void {
    if (this.options.length > 2) this.options.splice(i, 1);
  }

  isValid(): boolean {
    if (!this.question.trim()) return false;
    const filled = this.options.filter(o => o.trim().length > 0);
    return filled.length >= 2;
  }

  emit(): void {
    if (!this.isValid()) return;
    this.draft.emit({
      question: this.question.trim(),
      options: this.options.filter(o => o.trim().length > 0).map(o => o.trim()),
      multiChoice: this.multiChoice,
      durationHours: this.durationHours
    });
  }
}

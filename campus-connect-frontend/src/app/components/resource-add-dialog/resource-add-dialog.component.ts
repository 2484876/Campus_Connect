import { Component, EventEmitter, Input, Output, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../services/api.service';
import { CommunityResourceDTO, CreateResourceRequest } from '../../models';

@Component({
  selector: 'app-resource-add-dialog',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './resource-add-dialog.component.html',
  styleUrls: ['./resource-add-dialog.component.scss']
})
export class ResourceAddDialogComponent {
  @Input() communityId!: number;
  @Output() close = new EventEmitter<void>();
  @Output() added = new EventEmitter<CommunityResourceDTO>();

  resourceType: 'LINK' | 'FILE' = 'LINK';
  title = '';
  description = '';
  url = '';
  tags = '';
  selectedFile: File | null = null;
  fileSizeBytes: number | null = null;
  mimeType: string | null = null;
  uploading = false;
  submitting = false;
  error: string | null = null;

  constructor(private api: ApiService, private cdr: ChangeDetectorRef) { }

  setType(t: 'LINK' | 'FILE'): void {
    this.resourceType = t;
    this.url = '';
    this.selectedFile = null;
    this.fileSizeBytes = null;
    this.mimeType = null;
    this.error = null;
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.files?.[0]) return;
    const file = input.files[0];
    if (file.size > 20 * 1024 * 1024) {
      this.error = 'File must be under 20 MB';
      this.cdr.detectChanges();
      return;
    }
    this.error = null;
    this.selectedFile = file;
    this.fileSizeBytes = file.size;
    this.mimeType = file.type || 'application/octet-stream';
    if (!this.title) this.title = file.name.replace(/\.[^.]+$/, '');
    this.cdr.detectChanges();

    this.uploading = true;
    this.api.uploadImage(file).subscribe({
      next: (r) => {
        this.url = r.url;
        this.uploading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.uploading = false;
        this.selectedFile = null;
        this.error = 'Upload failed. Try a smaller file or different format.';
        this.cdr.detectChanges();
      }
    });
  }

  removeFile(): void {
    this.selectedFile = null;
    this.url = '';
    this.fileSizeBytes = null;
    this.mimeType = null;
    this.cdr.detectChanges();
  }

  isValid(): boolean {
    if (!this.title?.trim()) return false;
    if (!this.url?.trim()) return false;
    if (this.uploading) return false;
    return true;
  }

  submit(): void {
    if (!this.isValid() || this.submitting) return;
    this.submitting = true;
    this.error = null;

    const payload: CreateResourceRequest = {
      title: this.title.trim(),
      description: this.description?.trim() || undefined,
      resourceType: this.resourceType,
      url: this.url.trim(),
      tags: this.tags?.trim() || undefined,
      fileSizeBytes: this.fileSizeBytes ?? undefined,
      mimeType: this.mimeType ?? undefined
    };

    this.api.addCommunityResource(this.communityId, payload).subscribe({
      next: (r) => { this.submitting = false; this.added.emit(r); },
      error: (err) => {
        this.submitting = false;
        this.error = err?.error?.error || 'Could not add resource';
        this.cdr.detectChanges();
      }
    });
  }
}

import { Pipe, PipeTransform } from '@angular/core';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';

@Pipe({ name: 'hashtagLinks', standalone: true })
export class HashtagLinksPipe implements PipeTransform {
  constructor(private sanitizer: DomSanitizer) { }

  transform(value: string | null | undefined): SafeHtml {
    if (!value) return '';
    const escaped = value
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/\n/g, '<br>');
    const linked = escaped.replace(/#([A-Za-z0-9_]{2,50})/g,
      '<a class="hashtag" href="/hashtag/$1">#$1</a>');
    return this.sanitizer.bypassSecurityTrustHtml(linked);
  }
}

import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-presence-dot',
  standalone: true,
  imports: [CommonModule],
  template: `
    <span class="presence-dot" [class.online]="status === 'ONLINE'" [class.away]="status === 'AWAY'" [class.offline]="status === 'OFFLINE' || !status" [attr.title]="title()"></span>
  `,
  styles: [`
    .presence-dot {
      display: inline-block;
      width: 9px;
      height: 9px;
      border-radius: 50%;
      border: 2px solid #fff;
      background: #999;
      box-shadow: 0 0 0 1px rgba(0,0,0,0.1);
    }
    .presence-dot.online { background: #2ecc71; }
    .presence-dot.away { background: #f39c12; }
    .presence-dot.offline { background: #bbb; }
  `]
})
export class PresenceDotComponent {
  @Input() status: string | undefined | null = 'OFFLINE';

  title(): string {
    if (this.status === 'ONLINE') return 'Online';
    if (this.status === 'AWAY') return 'Away';
    return 'Offline';
  }
}

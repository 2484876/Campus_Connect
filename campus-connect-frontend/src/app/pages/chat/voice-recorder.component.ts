import { Component, EventEmitter, Output } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-voice-recorder',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="voice-recorder" *ngIf="recording">
      <div class="rec-indicator"></div>
      <span class="rec-time">{{ formatTime(seconds) }}</span>
      <button class="rec-btn rec-cancel" (click)="cancel()" title="Cancel">
        <svg width="14" height="14" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
      </button>
      <button class="rec-btn rec-send" (click)="stop()" title="Send">
        <svg width="14" height="14" fill="currentColor" viewBox="0 0 16 16"><path d="M15.854.146a.5.5 0 01.11.54l-5.819 14.547a.75.75 0 01-1.329.124l-3.178-4.995L.643 7.184a.75.75 0 01.124-1.33L15.314.037a.5.5 0 01.54.11z"/></svg>
      </button>
    </div>
    <button *ngIf="!recording" class="mic-btn" (click)="start()" title="Record voice note">
      <svg width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
        <path d="M12 1a3 3 0 0 0-3 3v8a3 3 0 0 0 6 0V4a3 3 0 0 0-3-3z"/>
        <path d="M19 10v2a7 7 0 0 1-14 0v-2"/>
        <line x1="12" y1="19" x2="12" y2="23"/>
        <line x1="8" y1="23" x2="16" y2="23"/>
      </svg>
    </button>
  `,
  styles: [`
    .voice-recorder {
      display: inline-flex;
      align-items: center;
      gap: 8px;
      padding: 5px 10px;
      background: #1a1a1a;
      border-radius: 20px;
    }
    .rec-indicator {
      width: 8px;
      height: 8px;
      border-radius: 50%;
      background: #e74c3c;
      animation: pulse 1s infinite;
    }
    @keyframes pulse {
      0%, 100% { opacity: 1; }
      50% { opacity: 0.3; }
    }
    .rec-time {
      color: #fff;
      font-size: 12px;
      font-weight: 600;
      min-width: 36px;
    }
    .rec-btn {
      width: 24px;
      height: 24px;
      border-radius: 50%;
      border: none;
      cursor: pointer;
      display: inline-flex;
      align-items: center;
      justify-content: center;
      color: #fff;
    }
    .rec-cancel { background: #555; }
    .rec-cancel:hover { background: #777; }
    .rec-send { background: #2ecc71; }
    .rec-send:hover { background: #27ae60; }
    .mic-btn {
      background: transparent;
      border: none;
      width: 32px;
      height: 32px;
      border-radius: 50%;
      cursor: pointer;
      color: #555;
      display: inline-flex;
      align-items: center;
      justify-content: center;
    }
    .mic-btn:hover { background: #f3f3f3; color: #000; }
  `]
})
export class VoiceRecorderComponent {
  @Output() recorded = new EventEmitter<{ blob: Blob, duration: number }>();

  recording = false;
  seconds = 0;
  private mediaRecorder: MediaRecorder | null = null;
  private chunks: Blob[] = [];
  private timer: any = null;
  private stream: MediaStream | null = null;
  private cancelled = false;

  async start(): Promise<void> {
    try {
      this.stream = await navigator.mediaDevices.getUserMedia({ audio: true });
      this.chunks = [];
      this.cancelled = false;
      this.seconds = 0;
      this.mediaRecorder = new MediaRecorder(this.stream);
      this.mediaRecorder.ondataavailable = (e) => {
        if (e.data && e.data.size > 0) this.chunks.push(e.data);
      };
      this.mediaRecorder.onstop = () => {
        this.cleanup();
        if (this.cancelled) return;
        const blob = new Blob(this.chunks, { type: 'audio/webm' });
        this.recorded.emit({ blob, duration: this.seconds });
      };
      this.mediaRecorder.start();
      this.recording = true;
      this.timer = setInterval(() => {
        this.seconds++;
        if (this.seconds >= 120) this.stop();
      }, 1000);
    } catch (e) {
      alert('Microphone permission denied or unavailable');
    }
  }

  stop(): void {
    if (this.mediaRecorder && this.mediaRecorder.state !== 'inactive') {
      this.mediaRecorder.stop();
    }
    this.recording = false;
  }

  cancel(): void {
    this.cancelled = true;
    this.stop();
  }

  private cleanup(): void {
    if (this.timer) { clearInterval(this.timer); this.timer = null; }
    if (this.stream) {
      this.stream.getTracks().forEach(t => t.stop());
      this.stream = null;
    }
    this.recording = false;
  }

  formatTime(s: number): string {
    const m = Math.floor(s / 60);
    const sec = s % 60;
    return `${m}:${sec < 10 ? '0' : ''}${sec}`;
  }
}

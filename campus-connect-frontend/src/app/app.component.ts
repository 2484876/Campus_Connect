import { Component, OnInit, OnDestroy } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { CommonModule } from '@angular/common';
import { NavbarComponent } from './components/navbar/navbar.component';
import { AuthService } from './services/auth.service';
import { WebSocketService } from './services/websocket.service';
import { MessageStateService } from './services/message-state.service';
import { ThemeService } from './services/theme.service';
import { Subscription } from 'rxjs';
import { ToastContainerComponent } from './components/toast/toast-container.component';
import { GlobalAlertOverride } from './services/global-alert-override.service';
@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, CommonModule, NavbarComponent, ToastContainerComponent],
  template: `
    <app-navbar *ngIf="auth.isLoggedIn()"></app-navbar>
    <router-outlet></router-outlet>
    <app-toast-container></app-toast-container>
  `
})
export class AppComponent implements OnInit, OnDestroy {
  private loginSub?: Subscription;

  constructor(
    public auth: AuthService,
    private ws: WebSocketService,
    private msgState: MessageStateService,
    private theme: ThemeService,
    private alertOverride: GlobalAlertOverride   
  ) {
    this.alertOverride.install();  
  }

  ngOnInit(): void {
    this.theme.init();

    if (this.auth.isLoggedIn()) {
      this.ws.connect();
      this.msgState.init();
    }

    this.loginSub = this.auth.loginSuccess$.subscribe(() => {
      this.ws.connect();
      this.msgState.init();
    });
  }

  ngOnDestroy(): void {
    this.loginSub?.unsubscribe();
    this.ws.disconnect();
  }
}

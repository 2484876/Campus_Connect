import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './login.html',
  styleUrl: './login.scss'
})
export class LoginComponent {
  email = '';
  password = '';
  error = '';
  loading = false;

  constructor(private authService: AuthService, private router: Router) {
    if (this.authService.isLoggedIn()) {
      this.router.navigate(['/feed']);
    }
  }

  onLogin(): void {
    if (!this.email || !this.password) {
      this.error = 'Please fill in all fields';
      return;
    }
    this.loading = true;
    this.error = '';
    this.authService.login({ email: this.email, password: this.password }).subscribe({
      next: () => this.router.navigate(['/feed']),
      error: () => {
        this.error = 'Invalid email or password';
        this.loading = false;
      }
    });
  }
}

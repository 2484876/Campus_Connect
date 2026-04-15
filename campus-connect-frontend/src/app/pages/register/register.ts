import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './register.html',
  styleUrl: './register.scss'
})
export class RegisterComponent {
  form = { name: '', email: '', password: '', role: 'PROGRAMMER_ANALYST_TRAINEE', department: '', position: '' };
  error = '';
  loading = false;

  constructor(private authService: AuthService, private router: Router) { }

  onRegister(): void {
    if (!this.form.name || !this.form.email || !this.form.password) {
      this.error = 'Name, email and password are required';
      return;
    }
    this.loading = true;
    this.error = '';
    this.authService.register(this.form).subscribe({
      next: () => this.router.navigate(['/feed']),
      error: (err) => {
        this.error = err.error?.error || 'Registration failed';
        this.loading = false;
      }
    });
  }
}

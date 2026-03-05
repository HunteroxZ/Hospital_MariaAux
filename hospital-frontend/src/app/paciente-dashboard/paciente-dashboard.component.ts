import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, Router } from '@angular/router'; 
import { AuthService } from '../auth.service'; 

@Component({
  selector: 'app-paciente-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './paciente-dashboard.component.html',
  styleUrl: './paciente-dashboard.component.css'
})
export class PacienteDashboardComponent {

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  onLogout() {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
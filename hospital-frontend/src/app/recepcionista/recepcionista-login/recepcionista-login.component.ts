
import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { RecepcionistaService } from '../../services/recepcionista.service';

@Component({
  selector: 'app-recepcionista-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './recepcionista-login.component.html',
  styleUrl: './recepcionista-login.component.css'
})
export class RecepcionistaLoginComponent {
  dni: string = '';
  clave: string = '';
  errorMessage: string = '';
  isLoading: boolean = false;

  constructor(
    private recepcionistaService: RecepcionistaService,
    private router: Router
  ) {}

  onSubmit(): void {
    if (!this.dni || !this.clave) {
      this.errorMessage = 'Por favor complete todos los campos';
      return;
    }

    if (this.dni.length !== 8 || !/^\d+$/.test(this.dni)) {
      this.errorMessage = 'El DNI debe tener 8 dígitos numéricos';
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';

    this.recepcionistaService.login({ dni: this.dni, clave: this.clave }).subscribe({
      next: (response) => {
        this.recepcionistaService.guardarSesion(response);
        this.router.navigate(['/recepcionista/dashboard']);
      },
      error: (error) => {
        this.isLoading = false;
        this.errorMessage = error.error?.error || 'Error al iniciar sesión';
      },
      complete: () => {
        this.isLoading = false;
      }
    });
  }
}
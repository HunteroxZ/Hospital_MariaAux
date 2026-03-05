import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { AdministradorService } from '../../services/administrador.service';
import { Administrador } from '../../models/administrador.model';

@Component({
  selector: 'app-admin-login',
  imports: [FormsModule, CommonModule, RouterLink],
  templateUrl: './admin-login.html',
  styleUrl: './admin-login.css'
})
export class AdminLogin {
  credenciales = {
    dni: '',
    contrasena: ''
  };

  errorMessage = '';
  dniError = '';

  constructor(
    private adminService: AdministradorService,
    private router: Router
  ) {}


  onDniInput(event: Event): void {
    const input = event.target as HTMLInputElement;
    const value = input.value;

    if (/[a-zA-Z]/.test(value)) {
      this.dniError = 'Solo se permiten números';
    } else if (value.length > 0 && value.length < 8) {
      this.dniError = 'El DNI debe tener 8 dígitos';
    } else {
      this.dniError = '';
    }
  }

  onLoginAdmin(): void {

    if (this.credenciales.dni.length !== 8) {
      this.dniError = 'El DNI debe tener exactamente 8 dígitos';
      return;
    }

    if (!/^\d+$/.test(this.credenciales.dni)) {
      this.dniError = 'El DNI solo puede contener números';
      return;
    }

    this.adminService.login(this.credenciales).subscribe({
      next: (admin: Administrador) => {
        console.log('Login exitoso:', admin);
        localStorage.setItem('admin', JSON.stringify(admin));
        this.router.navigate(['/admin/dashboard/inicio']);
      },
      error: (err) => {
        console.error('Error al iniciar sesión:', err);
        this.errorMessage = 'DNI o contraseña incorrectos';
      }
    });
  }
}





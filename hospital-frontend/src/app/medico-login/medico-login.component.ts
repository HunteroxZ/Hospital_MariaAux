import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { MedicoAuthService } from '../auth/medico-auth.service';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { environment } from '../../environments/environment';

@Component({
  selector: 'app-medico-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './medico-login.component.html',
  styleUrl: './medico-login.component.css'
})
export class MedicoLoginComponent {

  private apiUrl = environment.apiUrl + '/medicos/login';

  formMedico: FormGroup;
  errorMessage = '';

  constructor(
    private fb: FormBuilder,
    private http: HttpClient,
    private router: Router,
    private medicoAuthService: MedicoAuthService
  ) {
    this.formMedico = this.fb.group({
      dni: [
        '',
        [
          Validators.required,
          Validators.pattern(/^[0-9]{8}$/)
        ]
      ],
      clave: ['', Validators.required]
    });
  }

  onLoginMedico(): void {
    if (this.formMedico.invalid) {
      this.errorMessage = 'Verifica los datos ingresados.';
      this.formMedico.markAllAsTouched();
      return;
    }

    const credenciales = this.formMedico.value;

    this.http.post(this.apiUrl, credenciales).subscribe({
      next: (respuesta: any) => {
        alert(`¡Bienvenido Dr(a). ${respuesta.medico.nombres}!`);

        this.medicoAuthService.login(respuesta.medico);
        this.router.navigateByUrl('/medico/dashboard');
      },
      error: (error) => {
        this.errorMessage = error.error?.error || 'DNI o contraseña incorrectos.';
      }
    });
  }

  limitarCampo(event: any, campo: string, maxLength: number) {
    let valor = event.target.value;

    // Limitar cantidad de caracteres
    if (valor.length > maxLength) {
      valor = valor.slice(0, maxLength);
    }

    // Actualizar input
    event.target.value = valor;

    // Actualizar FormControl sin modificar lo ingresado (incluye letras)
    this.formMedico.get(campo)?.setValue(valor, { emitEvent: false });
  }
}

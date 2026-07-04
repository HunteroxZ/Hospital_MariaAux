import { Component } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../auth.service';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { environment } from '../../environments/environment';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {

  private apiUrl = environment.apiUrl + '/login';

  formLogin: FormGroup;
  errorMessage = '';

  constructor(
    private fb: FormBuilder,
    private http: HttpClient,
    private router: Router,
    private authService: AuthService
  ) {

    this.formLogin = this.fb.group({
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

  limitarDNI(event: Event): void {
    const input = event.target as HTMLInputElement;
    let valor = input.value;


    valor = valor.slice(0, 8);

    input.value = valor;
    this.formLogin.get('dni')?.setValue(valor, { emitEvent: false });
  }

  onLogin() {
    if (this.formLogin.invalid) {
      this.errorMessage = 'Verifica los datos ingresados.';
      this.formLogin.markAllAsTouched();
      return;
    }

    const credenciales = this.formLogin.value;

    this.http.post(this.apiUrl, credenciales).subscribe({
      next: (respuesta: any) => {
        alert('¡Bienvenido ' + respuesta.paciente.nombres + '!');
        this.authService.login(respuesta.paciente);
        this.router.navigate(['/dashboard']);
      },
      error: (error) => {
        this.errorMessage = error.error?.error || 'DNI o contraseña incorrectos.';
      }
    });
  }
}


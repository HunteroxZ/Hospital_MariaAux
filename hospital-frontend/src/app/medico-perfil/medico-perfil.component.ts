import { Component, OnInit } from '@angular/core';
import { CommonModule, NgIf } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Router, RouterLink } from '@angular/router';
import { MedicoAuthService } from '../auth/medico-auth.service';
import { FormsModule } from '@angular/forms'; 

@Component({
  selector: 'app-medico-perfil',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, NgIf],
  templateUrl: './medico-perfil.component.html',
  styleUrl: './medico-perfil.component.css'
})
export class MedicoPerfilComponent implements OnInit {

  private apiUrlBase = 'http://localhost:8080/medicos/';
  idMedicoLogueado: number | null = null;


  medicoInfo: any = {};


  updateData: any = {
    telefono: '',
    direccion: '',
    correo: '',
    clave: ''

  };


  modoEdicion = false; 
  isLoading = true;
  isSubmitting = false;
  errorMessage = '';

  constructor(
    private http: HttpClient,
    private router: Router,
    private medicoAuthService: MedicoAuthService
  ) {}

  ngOnInit(): void {
    this.idMedicoLogueado = this.medicoAuthService.getMedicoId();
    if (this.idMedicoLogueado) {
      this.cargarDatosMedico();
    } else {
      alert("Error de sesión. Redirigiendo a login.");
      this.router.navigate(['/medico/login']);
    }
  }


  cargarDatosMedico(): void {
    if (!this.idMedicoLogueado) return;
    this.isLoading = true;
    this.errorMessage = '';


    this.http.get<any>(`${this.apiUrlBase}${this.idMedicoLogueado}`).subscribe({
      next: (data) => {

        this.medicoInfo = {
          nombres: data.nombres,
          apellidos: data.apellidos,
          dni: data.dni,
          codigoColegiatura: data.codigoColegiatura
        };

        this.updateData = {
          telefono: data.telefono || '',
          direccion: data.direccion || '',
          correo: data.correo || '',
          clave: '' 
        };
        this.isLoading = false;
      },
      error: (err) => {
        this.errorMessage = "Error al cargar el perfil del médico.";
        this.isLoading = false;
      }
    });
  }


  activarModoEdicion(): void {
    this.modoEdicion = true;
  }


  cancelarEdicion(): void {
    this.modoEdicion = false;

    this.cargarDatosMedico(); 
  }


  guardarCambios(): void {
    if (!this.idMedicoLogueado) return;


    const datosAEnviar: any = {
      telefono: this.updateData.telefono,
      direccion: this.updateData.direccion,
      correo: this.updateData.correo

    };


    if (this.updateData.clave && this.updateData.clave.length > 0) {

      datosAEnviar.clave = this.updateData.clave;
    }

    this.isSubmitting = true;

    this.http.put(`${this.apiUrlBase}${this.idMedicoLogueado}`, datosAEnviar).subscribe({
      next: (data: any) => {
        alert("Perfil actualizado exitosamente.");
        this.isSubmitting = false;
        this.modoEdicion = false; 
        this.cargarDatosMedico(); 
      },
      error: (err) => {
        alert(`Error al actualizar: ${err.error.error || 'Error desconocido'}`);
        this.isSubmitting = false;
      }
    });
  }
}
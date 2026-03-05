import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../auth.service';

@Component({
  selector: 'app-editar-perfil',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './editar-perfil.component.html',
  styleUrl: './editar-perfil.component.css'
})
export class EditarPerfilComponent implements OnInit {

  private apiUrl = 'http://localhost:8080/'; 
  
  pacienteId: number | null = null;
  

  pacienteActual: any = {}; 
  

  updateData: any = {
    telefono: '',
    direccion: '',
    talla: null,
    peso: null,
    clave: '' 
  };

  constructor(
    private authService: AuthService,
    private http: HttpClient,
    private router: Router
  ) {}

  ngOnInit(): void {

    this.pacienteId = this.authService.getPacienteId();

    if (!this.pacienteId) {
      alert('Error: No se encontró paciente. Volviendo al login.');
      this.router.navigate(['/login']);
      return;
    }

    this.http.get(this.apiUrl + this.pacienteId).subscribe({
      next: (data: any) => {
        this.pacienteActual = data;
        this.updateData.telefono = data.telefono;
        this.updateData.direccion = data.direccion;
        this.updateData.talla = data.talla;
        this.updateData.peso = data.peso;
      },
      error: (err) => {
        alert('Error al cargar los datos del paciente.');
        console.error(err);
      }
    });
  }

  onUpdate() {
    if (!this.pacienteId) return;


    const datosParaApi = {
      telefono: this.updateData.telefono,
      direccion: this.updateData.direccion,
      talla: this.updateData.talla,
      peso: this.updateData.peso,
      clave: this.updateData.clave && this.updateData.clave.length > 0 ? this.updateData.clave : null
    };


    this.http.put(this.apiUrl + this.pacienteId, datosParaApi).subscribe({
      next: (respuesta: any) => {
        alert('¡Perfil actualizado exitosamente!');
        this.router.navigate(['/dashboard']);
      },
      error: (err) => {
        alert('Error al actualizar el perfil: ' + err.error.error);
        console.error(err);
      }
    });
  }
}
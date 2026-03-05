import { Component, OnInit } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { Router, RouterLink } from '@angular/router'; 
import { MedicoAuthService } from '../auth/medico-auth.service';

@Component({
  selector: 'app-medico-agenda',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink], 
  templateUrl: './medico-agenda.component.html',
  styleUrl: './medico-agenda.component.css'
})
export class MedicoAgendaComponent implements OnInit {

  private apiUrlBase = 'http://localhost:8080/medicos/';
  private apiUrlCitas = 'http://localhost:8080/citas/'; 
  
  idMedicoLogueado: number | null = null;
  nombreMedicoLogueado: string = "Doctor(a) Desconocido";

  fechaSeleccionada: string = this.getTodayDate(); 
  citasDelDia: any[] = []; 
  isLoading = false;
  errorMessage = '';

  constructor(
    private http: HttpClient,
    private router: Router,
    private medicoAuthService: MedicoAuthService
  ) {}

  ngOnInit(): void {
    const medicoData = this.medicoAuthService.getMedicoData();
    
    if (medicoData && medicoData.idMedico) { 
       this.idMedicoLogueado = medicoData.idMedico;
       this.nombreMedicoLogueado = `Dr(a). ${medicoData.nombres} ${medicoData.apellidos}`;
       this.cargarAgenda(); 
    } else {
       console.error("Error: ID de médico no encontrado en la sesión. Redirigiendo a login.");
       this.medicoAuthService.logout();
       this.router.navigate(['/medico/login']);
    }
  }

  cargarAgenda(): void {
    if (!this.idMedicoLogueado || !this.fechaSeleccionada) {
      this.errorMessage = "Seleccione una fecha válida.";
      this.citasDelDia = [];
      return;
    }
    this.isLoading = true;
    this.errorMessage = '';
    this.citasDelDia = [];

    const url = `${this.apiUrlBase}${this.idMedicoLogueado}/citas?fecha=${this.fechaSeleccionada}`;

    this.http.get<any[]>(url).subscribe({
      next: (data) => {
        this.citasDelDia = data; 
        if (this.citasDelDia.length === 0) {
          this.errorMessage = "No hay citas programadas para esta fecha.";
        }
        this.isLoading = false;
      },
      error: (err) => {
        this.errorMessage = 'Error al cargar la agenda.';
        this.isLoading = false;
      }
    });
  }

  marcarComoAtendida(idCita: number): void {
      const confirmar = confirm("¿Está seguro de marcar esta cita como ATENDIDA?");
      if (!confirmar) return;
      this.http.put(`${this.apiUrlCitas}${idCita}/atendida`, {}).subscribe({
          next: (data: any) => { alert(data.mensaje); this.cargarAgenda(); },
          error: (err) => { alert(`Error al atender cita: ${err.error.error}`); this.cargarAgenda(); }
      });
  }

  marcarComoNoPresentado(idCita: number): void {
      const confirmar = confirm("¿Está seguro de marcar esta cita como NO ASISTIO?");
      if (!confirmar) return;
      this.http.put(`${this.apiUrlCitas}${idCita}/no-presentado`, {}).subscribe({
          next: (data: any) => { alert(data.mensaje); this.cargarAgenda(); },
          error: (err) => { alert(`Error al marcar como no presentado: ${err.error.error}`); this.cargarAgenda(); }
      });
  }


  verDetalles(idCita: number, idPaciente: number, idEspecialidad: number): void { 

      this.router.navigate(['/medico/historial/cita', idCita, idPaciente, idEspecialidad]);
  }

  getTodayDate(): string {
    return new Date().toISOString().split('T')[0];
  }

  formatHora(hora: string): string {
    return hora ? hora.substring(0, 5) : '';
  }
}
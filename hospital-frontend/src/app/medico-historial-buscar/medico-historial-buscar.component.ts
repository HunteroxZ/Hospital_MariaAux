
import { Component, OnInit } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common'; 
import { FormsModule } from '@angular/forms'; 
import { Router, RouterLink } from '@angular/router';
import { MedicoAuthService } from '../auth/medico-auth.service';
import { HistoriaService } from '../services/historia.service';
import { HistoriaMedicaDTO } from '../models/historia-medica';

@Component({
  selector: 'app-medico-historial-buscar',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, DatePipe], 
  templateUrl: './medico-historial-buscar.component.html',
  styleUrl: './medico-historial-buscar.component.css'
})
export class MedicoHistorialBuscarComponent implements OnInit {

  idMedicoLogueado: number | null = null;
  dniBusqueda: string = '';
  historialPaciente: HistoriaMedicaDTO[] = [];
  pacienteEncontradoNombre: string | null = null;

  isLoading = false;
  errorMessage = '';
  infoMessage = '';

  constructor(
    private router: Router,
    private medicoAuthService: MedicoAuthService,
    private historiaService: HistoriaService
  ) {}

  ngOnInit(): void {
    this.idMedicoLogueado = this.medicoAuthService.getMedicoId();
    if (!this.idMedicoLogueado) {
      alert("Error de sesión de médico. Redirigiendo.");
      this.router.navigate(['/medico/login']);
    }
  }

  onBuscarPaciente(): void {
    if (!this.dniBusqueda || this.dniBusqueda.length !== 8 || !this.idMedicoLogueado) {
      this.errorMessage = "Por favor, ingrese un DNI válido de 8 dígitos.";
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';
    this.infoMessage = '';
    this.historialPaciente = [];
    this.pacienteEncontradoNombre = null;

    this.historiaService.buscarHistorialPorDniYMedico(this.idMedicoLogueado, this.dniBusqueda)
      .subscribe({
        next: (historial) => {
          if (historial.length === 0) {
            this.infoMessage = `No se encontró historial para el DNI ${this.dniBusqueda} (atendido por usted).`;
          } else {
            this.historialPaciente = historial;
            this.pacienteEncontradoNombre = historial[0].nombrePaciente || 'Paciente';
            this.infoMessage = `Historial encontrado para ${this.pacienteEncontradoNombre}.`;
          }
          this.isLoading = false;
        },
        error: (err) => {
          console.error('Error al buscar historial:', err);
          this.errorMessage = err.error?.error || "Error al buscar el historial del paciente.";
          this.isLoading = false;
        }
      });
  }
}
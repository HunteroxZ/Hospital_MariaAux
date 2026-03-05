import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink, Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../auth.service';
import { forkJoin } from 'rxjs';


interface TimeSlot {
  hora: string; 
  disponible: boolean; 
}

@Component({
  selector: 'app-generar-cita',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './generar-cita.component.html',
  styleUrl: './generar-cita.component.css'
})
export class GenerarCitaComponent implements OnInit {

  private apiUrlBase = 'http://localhost:8080';
  private apiUrlCitas = `${this.apiUrlBase}/citas`;
  private apiUrlEspecialidades = `${this.apiUrlBase}/especialidades`;
  private apiUrlMedicos = `${this.apiUrlBase}/medicos`;
  private apiUrlCitasReservadas = `${this.apiUrlBase}/citas/reservadas`; // <-- NUEVA URL

  pacienteId: number | null = null;
  especialidades: any[] = [];
  medicos: any[] = [];
  timeSlots: TimeSlot[] = [];

  public citaData: any = {
    idPaciente: null, idEspecialidad: null, idMedico: null,
    fecha: '', hora: '', motivoConsulta: '', sintomas: ''
  };

  isLoadingEspecialidades = false;
  isLoadingMedicos = false;
  isLoadingHorarios = false;
  errorMessage = '';

  constructor(
    private http: HttpClient,
    private router: Router,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.pacienteId = this.authService.getPacienteId();
    if (this.pacienteId) {
      this.citaData.idPaciente = this.pacienteId;
      this.cargarEspecialidades();
    } else {
      alert('Error: Debes iniciar sesión.');
      this.router.navigate(['/login']);
    }
  }

  cargarEspecialidades(): void {
    this.isLoadingEspecialidades = true;
    this.errorMessage = '';
    this.http.get<any[]>(this.apiUrlEspecialidades).subscribe({
      next: (data) => {
        this.especialidades = data;
        this.isLoadingEspecialidades = false;
      },
      error: (err) => {
        this.errorMessage = 'Error al cargar especialidades.'; console.error(err);
        this.isLoadingEspecialidades = false;
      }
    });
  }

  onEspecialidadChange(): void {
     this.medicos = []; this.timeSlots = []; this.citaData.idMedico = null;
     this.citaData.fecha = ''; this.citaData.hora = ''; this.errorMessage = '';
     const idEspecialidadSeleccionada = this.citaData.idEspecialidad;
     if (!idEspecialidadSeleccionada) return;
     this.isLoadingMedicos = true;
     this.http.get<any[]>(`${this.apiUrlEspecialidades}/${idEspecialidadSeleccionada}/medicos`).subscribe({
       next: (data) => { this.medicos = data; this.isLoadingMedicos = false; },
       error: (err) => {
         this.errorMessage = 'Error al cargar médicos.'; console.error(err);
         this.isLoadingMedicos = false;
       }
     });
  }

  onMedicoSelect(idMedicoSeleccionado: number): void {
      this.citaData.idMedico = idMedicoSeleccionado;
      if (this.citaData.fecha) { this.cargarHorariosDisponibles(); }
  }

  onFechaChange(): void {
      this.cargarHorariosDisponibles();
  }


  cargarHorariosDisponibles(): void {
    this.timeSlots = [];
    this.citaData.hora = '';
    this.errorMessage = '';

    const idMedicoSeleccionado = this.citaData.idMedico;
    const fechaSeleccionada = this.citaData.fecha;

    if (!idMedicoSeleccionado || !fechaSeleccionada) return;

    const hoy = this.getTodayDate();
    if (fechaSeleccionada < hoy) {
        this.errorMessage = 'No puedes seleccionar una fecha pasada.';
        return;
    }

    const fechaObj = new Date(fechaSeleccionada + 'T00:00:00');
    const diaSemanaIndex = fechaObj.getDay();
    const dias = ['domingo', 'lunes', 'martes', 'miercoles', 'jueves', 'viernes', 'sabado'];
    const diaSemanaNombre = dias[diaSemanaIndex];

    if (diaSemanaNombre === 'domingo') {
        this.errorMessage = 'No hay citas disponibles los domingos.';
        return;
    }

    this.isLoadingHorarios = true;

    const getDisponibilidad = this.http.get<any[]>(`${this.apiUrlMedicos}/${idMedicoSeleccionado}/disponibilidad`);
    const getHorasReservadas = this.http.get<string[]>(`${this.apiUrlCitasReservadas}/${idMedicoSeleccionado}/${fechaSeleccionada}`); 

    forkJoin([getDisponibilidad, getHorasReservadas]).subscribe({
      next: ([disponibilidades, horasReservadas]) => {
        const horariosDelDia = disponibilidades.filter(d => d.diaSemana === diaSemanaNombre);

        if (horariosDelDia.length === 0) {
            this.errorMessage = `El médico no tiene horarios registrados para el ${diaSemanaNombre} ${fechaSeleccionada}.`;
            this.isLoadingHorarios = false;
            return;
        }

        const slotsGenerados = this.generateTimeSlots(horariosDelDia, 20);

        this.timeSlots = slotsGenerados.map(slot => ({
          ...slot, 
          disponible: !horasReservadas.includes(slot.hora)
        }));

        const hayDisponibles = this.timeSlots.some(slot => slot.disponible);
        if (!hayDisponibles && this.timeSlots.length > 0) {
           this.errorMessage = `Todos los cupos para el ${diaSemanaNombre} ${fechaSeleccionada} ya están reservados. Por favor, selecciona otra fecha.`;
        } else if (this.timeSlots.length === 0){
             this.errorMessage = `No hay cupos de 20 minutos disponibles en los horarios registrados para esta fecha.`;
        }


        this.isLoadingHorarios = false;
      },
      error: (err) => {
        if (err.status === 404 && err.url?.includes('/reservadas/')) {
             this.errorMessage = 'Error: Médico no encontrado.';
        } else {
             this.errorMessage = 'Error al cargar la disponibilidad del médico.';
        }
        console.error(err);
        this.isLoadingHorarios = false;
      }
    });
  }


  generateTimeSlots(disponibilidades: any[], intervalMinutes: number): TimeSlot[] {
    const slots: TimeSlot[] = [];
    disponibilidades.forEach(bloque => {
      let slotStartTime = this.parseTime(bloque.horaInicio);
      const blockEndTime = this.parseTime(bloque.horaFin);
      let slotEndTime = new Date(slotStartTime.getTime() + intervalMinutes * 60000);

      while (slotEndTime.getTime() <= blockEndTime.getTime()) {
        slots.push({ hora: this.formatTime(slotStartTime), disponible: true }); // Inicialmente true
        slotStartTime = new Date(slotEndTime.getTime());
        slotEndTime = new Date(slotStartTime.getTime() + intervalMinutes * 60000);
      }
    });
    slots.sort((a, b) => a.hora.localeCompare(b.hora));
    return slots;
  }

  parseTime(timeString: string): Date {
    const [hours, minutes, seconds] = timeString.split(':').map(Number);
    const date = new Date();
    date.setHours(hours, minutes, seconds, 0);
    return date;
  }
  formatTime(date: Date): string {
    const hours = date.getHours().toString().padStart(2, '0');
    const minutes = date.getMinutes().toString().padStart(2, '0');
    const seconds = date.getSeconds().toString().padStart(2, '0');
    return `${hours}:${minutes}:${seconds}`;
  }

  selectTimeSlot(slot: TimeSlot): void {
      if(slot.disponible) {
          this.citaData.hora = slot.hora;
      }
  }

  onSubmit() {
     if (!this.citaData.idPaciente) {
       alert('Error: No se pudo identificar al paciente.'); return;
     }
     if (!this.citaData.idEspecialidad || !this.citaData.idMedico || !this.citaData.fecha || !this.citaData.hora) {
       alert('Por favor, seleccione especialidad, médico, fecha y hora.'); return;
     }
     console.log('Datos de la cita a enviar:', this.citaData);
     this.http.post(this.apiUrlCitas, this.citaData).subscribe({
       next: (respuesta: any) => {
         alert('¡Cita registrada con éxito! ID Cita: ' + respuesta.idCita);
         this.router.navigate(['/dashboard']);
       },
       error: (error) => {
         alert('Error al registrar la cita: ' + error.error.error); console.error('Error en API:', error);
       }
     });
  }

  getTodayDate(): string {
    return new Date().toISOString().split('T')[0];
  }

} 
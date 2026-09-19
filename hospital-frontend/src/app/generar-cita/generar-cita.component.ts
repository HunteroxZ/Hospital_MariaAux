import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink, Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../auth.service';
import { forkJoin } from 'rxjs';
import { environment } from '../../environments/environment';


interface TimeSlot {
  hora: string; 
  disponible: boolean; 
  esAdicional: boolean;
  pasada: boolean;
}

@Component({
  selector: 'app-generar-cita',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './generar-cita.component.html',
  styleUrl: './generar-cita.component.css'
})
export class GenerarCitaComponent implements OnInit {

  private apiUrlBase = environment.apiUrl;
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

  semanaInicio: string = '';
  diasDisponibles: any[] = [];
  private disponibilidadMedico: any[] = [];

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
     this.diasDisponibles = []; this.disponibilidadMedico = [];
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
      this.citaData.fecha = ''; this.citaData.hora = ''; this.timeSlots = [];
      this.semanaInicio = this.obtenerLunes(this.getTodayDate());
      this.cargarDisponibilidadSemana();
  }

  onFechaChange(): void {
      this.cargarHorariosDisponibles();
  }

  obtenerLunes(fechaStr: string): string {
    const f = new Date(fechaStr + 'T00:00:00');
    const diff = (f.getDay() + 6) % 7;
    f.setDate(f.getDate() - diff);
    return this.toFechaStr(f);
  }

  toFechaStr(f: Date): string {
    const y = f.getFullYear();
    const m = (f.getMonth() + 1).toString().padStart(2, '0');
    const d = f.getDate().toString().padStart(2, '0');
    return `${y}-${m}-${d}`;
  }

  nombreDiaCorto(fechaStr: string): string {
    const nombres = ['dom', 'lun', 'mar', 'mié', 'jue', 'vie', 'sáb'];
    return nombres[new Date(fechaStr + 'T00:00:00').getDay()];
  }

  cargarDisponibilidadSemana(): void {
    const idMedicoSeleccionado = this.citaData.idMedico;
    if (!idMedicoSeleccionado || !this.semanaInicio) return;
    this.http.get<any[]>(`${this.apiUrlMedicos}/${idMedicoSeleccionado}/disponibilidad`).subscribe({
      next: (data) => {
        this.disponibilidadMedico = data || [];
        this.construirDiasDisponibles();
      },
      error: (err) => {
        console.error('Error al cargar disponibilidad:', err);
        this.disponibilidadMedico = [];
        this.construirDiasDisponibles();
      }
    });
  }

  construirDiasDisponibles(): void {
    const nombresDias = ['lunes', 'martes', 'miercoles', 'jueves', 'viernes', 'sabado', 'domingo'];
    const idEsp = Number(this.citaData.idEspecialidad);
    const diasAtiende = new Set(
      this.disponibilidadMedico
        .filter(d => Number(d.idEspecialidad) === idEsp)
        .map(d => String(d.diaSemana).toLowerCase())
    );
    const hoy = this.getTodayDate();
    const base = new Date(this.semanaInicio + 'T00:00:00');
    this.diasDisponibles = [];
    for (let i = 0; i < 7; i++) {
      const f = new Date(base);
      f.setDate(base.getDate() + i);
      const fecha = this.toFechaStr(f);
      const esDomingo = f.getDay() === 0;
      const idx = (f.getDay() + 6) % 7;
      this.diasDisponibles.push({
        fecha,
        nombre: this.nombreDiaCorto(fecha),
        num: f.getDate(),
        esDomingo,
        esPasado: fecha < hoy,
        disponible: !esDomingo && fecha >= hoy && diasAtiende.has(nombresDias[idx])
      });
    }
  }

  seleccionarDia(dia: any): void {
    if (!dia.disponible) return;
    this.citaData.fecha = dia.fecha;
    this.cargarHorariosDisponibles();
  }

  semanaAnterior(): void {
    const f = new Date(this.semanaInicio + 'T00:00:00');
    f.setDate(f.getDate() - 7);
    const lunesActual = this.obtenerLunes(this.getTodayDate());
    if (this.toFechaStr(f) < lunesActual) return;
    this.semanaInicio = this.toFechaStr(f);
    this.construirDiasDisponibles();
  }

  semanaSiguiente(): void {
    const f = new Date(this.semanaInicio + 'T00:00:00');
    f.setDate(f.getDate() + 7);
    this.semanaInicio = this.toFechaStr(f);
    this.construirDiasDisponibles();
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
    const getAdicionales = this.http.get<any[]>(`${this.apiUrlMedicos}/${idMedicoSeleccionado}/cupos-adicionales/fecha?fecha=${fechaSeleccionada}`);

    forkJoin([getDisponibilidad, getHorasReservadas, getAdicionales]).subscribe({
      next: ([disponibilidades, horasReservadas, cuposAdicionales]) => {
        const horariosDelDia = disponibilidades.filter(d => d.diaSemana === diaSemanaNombre);

        if (horariosDelDia.length === 0) {
            this.errorMessage = `El médico no tiene horarios registrados para el ${diaSemanaNombre} ${fechaSeleccionada}.`;
            this.isLoadingHorarios = false;
            return;
        }

        const slotsGenerados = this.generateTimeSlots(horariosDelDia, 20);

        const idEsp = Number(this.citaData.idEspecialidad);
        const cuposValidos = (cuposAdicionales || [])
          .filter((c: any) => Number(c.idEspecialidad) === idEsp);
        const horasAdicionales = new Set(cuposValidos.map((c: any) => c.horaInicio.substring(0, 8)));

        this.timeSlots = slotsGenerados
          .filter(slot => !horasAdicionales.has(slot.hora))
          .map(slot => ({
            ...slot,
            disponible: !horasReservadas.includes(slot.hora) && !this.esHoraPasada(slot.hora, fechaSeleccionada),
            pasada: this.esHoraPasada(slot.hora, fechaSeleccionada)
          }));

        cuposValidos.forEach((cupo: any) => {
          const hora = cupo.horaInicio.substring(0, 8);
          if (!this.timeSlots.some(s => s.hora === hora)) {
            const pasada = this.esHoraPasada(hora, fechaSeleccionada);
            const libre = cupo.disponible && !horasReservadas.includes(hora) && !pasada;
            this.timeSlots.push({ hora, disponible: libre, esAdicional: true, pasada });
          }
        });
        this.timeSlots.sort((a, b) => a.hora.localeCompare(b.hora));

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
        slots.push({ hora: this.formatTime(slotStartTime), disponible: true, esAdicional: false, pasada: false }); // Inicialmente true
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

  esHoraPasada(hora: string, fechaSeleccionada: string): boolean {
    if (fechaSeleccionada !== this.getTodayDate()) return false;
    const ahora = new Date();
    const [h, m] = hora.split(':').map(Number);
    const slotDate = new Date();
    slotDate.setHours(h, m, 0, 0);
    return slotDate.getTime() <= ahora.getTime();
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
          alert('¡Cita registrada con éxito!');
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
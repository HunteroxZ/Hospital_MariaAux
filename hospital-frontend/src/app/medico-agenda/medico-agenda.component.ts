import { Component, OnInit } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { Router, RouterLink } from '@angular/router'; 
import { MedicoAuthService } from '../auth/medico-auth.service';
import { environment } from '../../environments/environment';
import { forkJoin } from 'rxjs';

@Component({
  selector: 'app-medico-agenda',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink], 
  templateUrl: './medico-agenda.component.html',
  styleUrl: './medico-agenda.component.css'
})
export class MedicoAgendaComponent implements OnInit {

  private apiUrlBase = environment.apiUrl + '/medicos/';
  private apiUrlCitas = environment.apiUrl + '/citas/';
  
  idMedicoLogueado: number | null = null;
  nombreMedicoLogueado: string = "Doctor(a) Desconocido";

  fechaSeleccionada: string = this.getTodayDate(); 
  citasDelDia: any[] = []; 
  isLoading = false;
  errorMessage = '';

  semanaInicio: string = '';
  diasSemana: any[] = [];
  diasAtiende: string[] = [];
  etiquetaSemana: string = '';

  mostrarModalAdicionales = false;
  cantidadCuposAdicionales = 1;
  idEspecialidadSeleccionada: number | null = null;
  especialidades: any[] = [];
  cuposAdicionales: any[] = [];
  cuposAdicionalesFecha: any[] = [];
  isLoadingAdicionales = false;
  mensajeAdicionales = '';
  jornadaLlena = false;

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
       this.semanaInicio = this.obtenerLunes(this.fechaSeleccionada);
       this.cargarAgenda();
       this.cargarEspecialidades();
       this.cargarSemana();
    } else {
       console.error("Error: ID de médico no encontrado en la sesión. Redirigiendo a login.");
       this.medicoAuthService.logout();
       this.router.navigate(['/medico/login']);
    }
  }

  cargarEspecialidades(): void {
    if (!this.idMedicoLogueado) return;
    this.http.get<any[]>(`${environment.apiUrl}/medicos/${this.idMedicoLogueado}/especialidades`).subscribe({
      next: (data) => {
        this.especialidades = data;
        if (this.especialidades.length === 1) {
          this.idEspecialidadSeleccionada = this.especialidades[0].idEspecialidad;
        }
      },
      error: (err) => {
        console.error('Error al cargar especialidades:', err);
      }
    });
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

    this.cargarCuposAdicionalesFecha();
    this.verificarJornadaLlena();
  }

  verificarJornadaLlena(): void {
    this.jornadaLlena = false;
    if (!this.idMedicoLogueado || !this.fechaSeleccionada) return;

    const fechaObj = new Date(this.fechaSeleccionada + 'T00:00:00');
    const dias = ['domingo', 'lunes', 'martes', 'miercoles', 'jueves', 'viernes', 'sabado'];
    const diaSemanaNombre = dias[fechaObj.getDay()];
    if (diaSemanaNombre === 'domingo') return;

    forkJoin([
      this.http.get<any[]>(`${this.apiUrlBase}${this.idMedicoLogueado}/disponibilidad`),
      this.http.get<string[]>(`${environment.apiUrl}/citas/reservadas/${this.idMedicoLogueado}/${this.fechaSeleccionada}`)
    ]).subscribe({
      next: ([disponibilidades, horasReservadas]) => {
        const bloques = (disponibilidades || []).filter(d => d.diaSemana === diaSemanaNombre);
        if (bloques.length === 0) return;
        const reservadas = new Set((horasReservadas || []).map(h => h.substring(0, 5)));
        let llena = true;
        for (const b of bloques) {
          let act = this.parseHoraStr(b.horaInicio);
          const fin = this.parseHoraStr(b.horaFin);
          while (this.addMin(act, 20) <= fin) {
            if (!reservadas.has(this.toHoraStr(act))) {
              llena = false;
              break;
            }
            act = this.addMin(act, 20);
          }
          if (!llena) break;
        }
        this.jornadaLlena = llena;
      },
      error: (err) => console.error('Error al verificar jornada:', err)
    });
  }

  private parseHoraStr(hora: string): number {
    const [h, m] = hora.split(':').map(Number);
    return h * 60 + m;
  }

  private addMin(mins: number, add: number): number {
    return mins + add;
  }

  private toHoraStr(mins: number): string {
    const h = Math.floor(mins / 60).toString().padStart(2, '0');
    const m = (mins % 60).toString().padStart(2, '0');
    return `${h}:${m}`;
  }

  get puedeHabilitarAdicionales(): boolean {
    if (this.citasDelDia.length === 0) return false;
    const algunaAtendida = this.citasDelDia.some(c => c.estado === 'atendida' || c.estado === 'no_presentado');
    const pendientes = this.citasDelDia.some(c => c.estado === 'pendiente' || c.estado === 'confirmada');
    return algunaAtendida && !pendientes && this.jornadaLlena && this.cuposAdicionalesFecha.length < 2;
  }

  cargarCuposAdicionalesFecha(): void {
    if (!this.idMedicoLogueado || !this.fechaSeleccionada) {
      this.cuposAdicionalesFecha = [];
      return;
    }
    const url = `${this.apiUrlBase}${this.idMedicoLogueado}/cupos-adicionales/fecha?fecha=${this.fechaSeleccionada}`;
    this.http.get<any[]>(url).subscribe({
      next: (data) => {
        this.cuposAdicionalesFecha = data;
      },
      error: (err) => {
        console.error('Error al cargar cupos adicionales:', err);
        this.cuposAdicionalesFecha = [];
      }
    });
  }

  nombreEspecialidad(idEspecialidad: number): string {
    const esp = this.especialidades.find(e => e.idEspecialidad === idEspecialidad);
    return esp ? esp.nombre : `Esp. ${idEspecialidad}`;
  }

  esCupoReservado(cupo: any): boolean {
    if (!cupo.disponible) return true;
    const horaCupo = this.formatHora(cupo.horaInicio);
    return this.citasDelDia.some(cita => cita.estado !== 'cancelada' && cita.esAdicional && this.formatHora(cita.hora) === horaCupo);
  }

  eliminarCupoAdicional(cupo: any): void {
    if (!this.idMedicoLogueado) return;
    const confirmar = confirm(`¿Eliminar el cupo adicional de ${this.formatHora(cupo.horaInicio)} - ${this.formatHora(cupo.horaFin)}?`);
    if (!confirmar) return;
    this.http.delete<any>(`${this.apiUrlBase}${this.idMedicoLogueado}/cupos-adicionales/${cupo.idCupoAdicional}`).subscribe({
      next: (response) => {
        this.cargarCuposAdicionalesFecha();
      },
      error: (err) => {
        alert(err.error?.error || 'Error al eliminar el cupo.');
      }
    });
  }

  abrirModalAdicionales(): void {
    this.mostrarModalAdicionales = true;
    this.cantidadCuposAdicionales = 1;
    this.mensajeAdicionales = '';
    this.cargarCuposExistentes();
  }

  cerrarModalAdicionales(): void {
    this.mostrarModalAdicionales = false;
    this.cantidadCuposAdicionales = 1;
    this.mensajeAdicionales = '';
  }

  cargarCuposExistentes(): void {
    if (!this.idMedicoLogueado || !this.idEspecialidadSeleccionada) return;

    const url = `${this.apiUrlBase}${this.idMedicoLogueado}/cupos-adicionales?idEspecialidad=${this.idEspecialidadSeleccionada}&fecha=${this.fechaSeleccionada}`;
    this.http.get<any[]>(url).subscribe({
      next: (data) => {
        this.cuposAdicionales = data;
      },
      error: (err) => {
        console.error('Error al cargar cupos adicionales:', err);
      }
    });
  }

  habilitarCuposAdicionales(): void {
    if (!this.idMedicoLogueado || !this.idEspecialidadSeleccionada) {
      this.mensajeAdicionales = 'Seleccione una especialidad.';
      return;
    }

    if (this.cantidadCuposAdicionales < 1 || this.cantidadCuposAdicionales > 2) {
      this.mensajeAdicionales = 'Ingrese una cantidad válida (1-2).';
      return;
    }

    this.isLoadingAdicionales = true;
    this.mensajeAdicionales = '';

    const body = {
      idEspecialidad: this.idEspecialidadSeleccionada,
      fecha: this.fechaSeleccionada,
      cantidadCupos: this.cantidadCuposAdicionales
    };

    this.http.post<any>(`${this.apiUrlBase}${this.idMedicoLogueado}/cupos-adicionales`, body).subscribe({
      next: (response) => {
        this.mensajeAdicionales = response.mensaje;
        this.cargarCuposExistentes();
        this.cargarCuposAdicionalesFecha();
        this.isLoadingAdicionales = false;
        alert(response.mensaje);
        this.cerrarModalAdicionales();
      },
      error: (err) => {
        this.mensajeAdicionales = err.error?.error || 'Error al habilitar cupos.';
        this.isLoadingAdicionales = false;
      }
    });
  }

  marcarComoAtendida(idCita: number): void {
      const confirmar = confirm("¿Está seguro de marcar esta cita como ATENDIDA?");
      if (!confirmar) return;
      this.http.put(`${this.apiUrlCitas}${idCita}/atendida`, {}).subscribe({
          next: (data: any) => { alert(data.mensaje); this.cargarAgenda(); this.cargarSemana(); },
          error: (err) => { alert(`Error al atender cita: ${err.error.error}`); this.cargarAgenda(); this.cargarSemana(); }
      });
  }

  marcarComoNoPresentado(idCita: number): void {
      const confirmar = confirm("¿Está seguro de marcar esta cita como NO ASISTIO?");
      if (!confirmar) return;
      this.http.put(`${this.apiUrlCitas}${idCita}/no-presentado`, {}).subscribe({
          next: (data: any) => { alert(data.mensaje); this.cargarAgenda(); this.cargarSemana(); },
          error: (err) => { alert(`Error al marcar como no presentado: ${err.error.error}`); this.cargarAgenda(); this.cargarSemana(); }
      });
  }


  verDetalles(idCita: number, idPaciente: number, idEspecialidad: number): void { 

      this.router.navigate(['/medico/historial/cita', idCita, idPaciente, idEspecialidad]);
  }

  getTodayDate(): string {
    return new Date().toISOString().split('T')[0];
  }

  obtenerLunes(fechaStr: string): string {
    const f = new Date(fechaStr + 'T00:00:00');
    const dia = f.getDay();
    const diff = (dia + 6) % 7;
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

  construirEtiquetaSemana(lunes: Date): string {
    const meses = ['Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio',
      'Julio', 'Agosto', 'Septiembre', 'Octubre', 'Noviembre', 'Diciembre'];
    const inicio = new Date(lunes);
    const fin = new Date(lunes);
    fin.setDate(inicio.getDate() + 6);
    const rango = `${inicio.getDate()} – ${fin.getDate()}`;
    if (inicio.getMonth() === fin.getMonth()) {
      return `${rango} de ${meses[inicio.getMonth()]} ${fin.getFullYear()}`;
    }
    if (inicio.getFullYear() === fin.getFullYear()) {
      return `${inicio.getDate()} ${meses[inicio.getMonth()].substring(0, 3)}. – ${fin.getDate()} ${meses[fin.getMonth()].substring(0, 3)}. ${fin.getFullYear()}`;
    }
    return `${inicio.getDate()} ${meses[inicio.getMonth()].substring(0, 3)}. ${inicio.getFullYear()} – ${fin.getDate()} ${meses[fin.getMonth()].substring(0, 3)}. ${fin.getFullYear()}`;
  }

  cargarSemana(): void {
    if (!this.idMedicoLogueado || !this.semanaInicio) return;

    const nombresDias = ['lunes', 'martes', 'miercoles', 'jueves', 'viernes', 'sabado', 'domingo'];
    const base = new Date(this.semanaInicio + 'T00:00:00');
    this.diasSemana = [];
    for (let i = 0; i < 7; i++) {
      const f = new Date(base);
      f.setDate(base.getDate() + i);
      const fecha = this.toFechaStr(f);
      this.diasSemana.push({
        fecha,
        nombre: this.nombreDiaCorto(fecha),
        num: f.getDate(),
        esDomingo: f.getDay() === 0,
        totalCitas: 0,
        atiende: false
      });
    }
    this.etiquetaSemana = this.construirEtiquetaSemana(base);

    this.http.get<any[]>(`${this.apiUrlBase}${this.idMedicoLogueado}/disponibilidad`).subscribe({
      next: (data) => {
        const dias = new Set((data || []).map((d: any) => String(d.diaSemana).toLowerCase()));
        this.diasSemana.forEach(dia => {
          const idx = (new Date(dia.fecha + 'T00:00:00').getDay() + 6) % 7;
          dia.atiende = dias.has(nombresDias[idx]);
        });
      },
      error: (err) => console.error('Error al cargar disponibilidad:', err)
    });

    this.http.get<any[]>(`${environment.apiUrl}/citas/semanales/${this.idMedicoLogueado}?inicio=${this.semanaInicio}`).subscribe({
      next: (data) => {
        const mapa = new Map((data || []).map((d: any) => [d.fecha, d.totalCitas]));
        this.diasSemana.forEach(dia => {
          dia.totalCitas = mapa.get(dia.fecha) || 0;
        });
      },
      error: (err) => console.error('Error al cargar conteo semanal:', err)
    });
  }

  seleccionarDia(dia: any): void {
    if (dia.esDomingo) return;
    this.fechaSeleccionada = dia.fecha;
    this.cargarAgenda();
  }

  semanaAnterior(): void {
    const f = new Date(this.semanaInicio + 'T00:00:00');
    f.setDate(f.getDate() - 7);
    this.semanaInicio = this.toFechaStr(f);
    this.cargarSemana();
  }

  semanaSiguiente(): void {
    const f = new Date(this.semanaInicio + 'T00:00:00');
    f.setDate(f.getDate() + 7);
    this.semanaInicio = this.toFechaStr(f);
    this.cargarSemana();
  }

  irAHoy(): void {
    this.fechaSeleccionada = this.getTodayDate();
    this.semanaInicio = this.obtenerLunes(this.fechaSeleccionada);
    this.cargarSemana();
    this.cargarAgenda();
  }

  formatHora(hora: string): string {
    return hora ? hora.substring(0, 5) : '';
  }

  formatoEstado(estado: string): string {
    return estado ? estado.replace(/_/g, ' ') : '';
  }

  formatoFecha(fecha: string): string {
    if (!fecha || !fecha.includes('-')) return fecha || '';
    const [y, m, d] = fecha.substring(0, 10).split('-');
    return `${d}/${m}/${y}`;
  }
}
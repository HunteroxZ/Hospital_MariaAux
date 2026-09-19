
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { forkJoin } from 'rxjs';
import { RecepcionistaService } from '../../services/recepcionista.service';
import { CitaPacienteRecepcionista, PagoRequest } from '../../models/recepcionista.model';
import { MenuPagoComponent } from "../menu-pago/menu-pago.component";
import { environment } from '../../../environments/environment';

interface SlotHorario {
  hora: string;
  disponible: boolean;
  esAdicional: boolean;
  pasada: boolean;
}

@Component({
  selector: 'app-recepcionista-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule, MenuPagoComponent],
  templateUrl: './recepcionista-dashboard.component.html',
  styleUrl: './recepcionista-dashboard.component.css'
})
export class RecepcionistaDashboardComponent implements OnInit {
  nombreRecepcionista: string = '';
  dniBusqueda: string = '';
  citasEncontradas: CitaPacienteRecepcionista[] = [];
  citaSeleccionada: CitaPacienteRecepcionista | null = null;
  errorMessage: string = '';
  isLoading: boolean = false;
  mostrarMenuPago: boolean = false;
  mostrarModalTicketSIS: boolean = false;
  mostrarRegistroRapido: boolean = false;
  pacienteNoEncontrado: boolean = false;
  isGuardandoPaciente: boolean = false;
  errorRegistro: string = '';
  registroRapido: any = {
    dni: '', nombres: '', apellidos: '', sexo: 'M', fechaNacimiento: ''
  };

  private apiUrlBase = environment.apiUrl;
  especialidadesHorario: any[] = [];
  medicosHorario: any[] = [];
  idEspHorario: number | null = null;
  idMedicoHorario: number | null = null;
  fechaHorario: string = '';
  slotsHorario: SlotHorario[] = [];
  isLoadingHorarios: boolean = false;
  errorHorarios: string = '';

  semanaInicio: string = '';
  diasDisponibles: any[] = [];
  private disponibilidadMedico: any[] = [];

  reservaPaciente: any = null;
  slotSeleccionado: string | null = null;
  motivoReserva: string = '';
  sintomasReserva: string = '';
  isGuardandoCita: boolean = false;
  mensajeReserva: string = '';

  constructor(
    private recepcionistaService: RecepcionistaService,
    private http: HttpClient,
    private router: Router
  ) {}

  ngOnInit(): void {
    const sesion = this.recepcionistaService.obtenerSesion();
    if (sesion) {
      this.nombreRecepcionista = `${sesion.nombres} ${sesion.apellidos}`;
    }
    this.cargarEspecialidadesHorario();
  }

  buscarCita(): void {
    if (!this.dniBusqueda || this.dniBusqueda.length !== 8) {
      this.errorMessage = 'Por favor ingrese un DNI válido de 8 dígitos';
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';
    this.citasEncontradas = [];
    this.citaSeleccionada = null;
    this.resolverPacienteReserva(this.dniBusqueda);

    this.recepcionistaService.buscarCitasPorDni(this.dniBusqueda).subscribe({
      next: (citas) => {
        this.citasEncontradas = citas;
        if (citas.length === 0) {
          this.errorMessage = 'No se encontraron citas pendientes para este DNI';
        }
      },
      error: (error) => {
        this.errorMessage = error.error?.error || 'Error al buscar citas';
        if ((this.errorMessage || '').includes('No se encontró ningún paciente')) {
          this.pacienteNoEncontrado = true;
        }
        this.isLoading = false;
      },
      complete: () => {
        this.isLoading = false;
      }
    });
  }

  seleccionarCita(cita: CitaPacienteRecepcionista): void {
    this.citaSeleccionada = cita;
  }

  actualizarEstadoSIS(cita: CitaPacienteRecepcionista): void {
    if (this.citaSeleccionada?.idCita === cita.idCita) {
      this.citaSeleccionada = {...cita};
    }
  }

  verificarSIS(): void {
    window.open('https://cel.sis.gob.pe/SisConsultaEnLinea', '_blank');
  }

  abrirMenuPago(): void {
    if (!this.citaSeleccionada) {
      this.errorMessage = 'Por favor seleccione una cita primero';
      return;
    }
    this.mostrarMenuPago = true;
  }

  generarTicketSIS(): void {
    if (!this.citaSeleccionada) {
      this.errorMessage = 'Por favor seleccione una cita primero';
      return;
    }
    this.mostrarModalTicketSIS = true;
  }

  imprimirTicketSIS(): void {
      if (!this.citaSeleccionada) return;

      const contenido = this.generarHTMLTicketSIS();
      try {
        const blob = new Blob([contenido], { type: 'text/html;charset=utf-8' });
        const url = URL.createObjectURL(blob);
        const ventana = window.open(url, '_blank');
        if (!ventana) {
          alert('Permita las ventanas emergentes para ver el ticket.');
        }
      } catch (e) {
        console.error('No se pudo abrir el ticket:', e);
      }
      this.mostrarModalTicketSIS = false;

      // Procesar el pago SIS en el backend
      const pagoRequest: PagoRequest = {
        idCita: this.citaSeleccionada.idCita,
        metodoPago: 'SIS',
        montoPagado: 0,
        tieneSIS: true
      };

      this.recepcionistaService.procesarPago(pagoRequest).subscribe({
        next: (response) => {
          console.log('Ticket SIS procesado:', response);
          // Actualizar la lista de citas
          this.cerrarMenuPago();
        },
        error: (error) => {
          console.error('Error al procesar ticket SIS:', error);
          this.errorMessage = error.error?.error || 'Error al procesar ticket SIS.';
        }
      });
  }

  generarHTMLTicketSIS(): string {
    if (!this.citaSeleccionada) return '';

    const fecha = new Date();
    const fechaStr = fecha.toLocaleDateString('es-PE');
    const horaStr = fecha.toLocaleTimeString('es-PE');

    return `
      <!DOCTYPE html>
      <html>
      <head>
        <title>Ticket de Cita SIS</title>
        <style>
          @media print {
            .no-print { display: none !important; }
          }
          body { 
            font-family: Arial, sans-serif; 
            padding: 20px; 
            max-width: 600px; 
            margin: 0 auto;
          }
          .header { 
            text-align: center; 
            margin-bottom: 20px; 
            border-bottom: 2px solid #000; 
            padding-bottom: 15px; 
          }
          .sis-badge {
            background: #27ae60;
            color: white;
            padding: 10px;
            border-radius: 5px;
            font-weight: bold;
            margin: 15px 0;
          }
          .info p { margin: 8px 0; }
          .costo-sis {
            background: #d4edda;
            border: 2px solid #27ae60;
            padding: 15px;
            margin: 20px 0;
            text-align: center;
            font-size: 18px;
            font-weight: bold;
          }
        </style>
      </head>
      <body>
        <div class="header">
          <h2>SISTEMA DE GESTIÓN HOSPITALARIA</h2>
          <h3>TICKET DE CITA</h3>
          <div class="sis-badge">PACIENTE SIS - COBERTURA TOTAL</div>
        </div>
        
        <div class="info">
          <p><strong>Fecha de Emisión:</strong> ${fechaStr} - ${horaStr}</p>
          <p><strong>Paciente:</strong> ${this.citaSeleccionada.nombrePaciente}</p>
          <p><strong>DNI:</strong> ${this.citaSeleccionada.dniPaciente}</p>
          <p><strong>Médico:</strong> ${this.citaSeleccionada.nombreMedico}</p>
          <p><strong>Especialidad:</strong> ${this.citaSeleccionada.especialidad}</p>
          <p><strong>Fecha de Cita:</strong> ${this.citaSeleccionada.fecha}</p>
          <p><strong>Hora:</strong> ${this.citaSeleccionada.hora}</p>
        </div>
        
        <div class="costo-sis">
          Costo: S/ 0.00 (Cobertura SIS)
        </div>
        
        <p style="text-align: center; color: #666; margin-top: 30px;">
          Por favor, presente este ticket en el consultorio médico a la hora indicada
        </p>

        <div class="no-print" style="text-align: center; margin-top: 20px; padding: 10px;">
          <button onclick="window.print()" style="padding: 10px 20px; background: #27ae60; color: white; border: none; border-radius: 5px; cursor: pointer;">
            Imprimir
          </button>
          <button onclick="window.close()" style="padding: 10px 20px; background: #e74c3c; color: white; border: none; border-radius: 5px; margin-left: 10px; cursor: pointer;">
            Cerrar
          </button>
        </div>
      </body>
      </html>
    `;
  }

  cerrarMenuPago(): void {
    this.mostrarMenuPago = false;
    this.citaSeleccionada = null;
    
    if (this.dniBusqueda && this.dniBusqueda.length === 8) {
      setTimeout(() => {
        this.buscarCita();
      }, 500);
    }
  }

  resolverPacienteReserva(dni: string): void {
    if (!dni || dni.length !== 8) {
      this.reservaPaciente = null;
      return;
    }
    this.http.get<any[]>(`${this.apiUrlBase}/pacientes/info?dni=${dni}`).subscribe({
      next: (data) => {
        const p = (data || []).find((x: any) => x.dni === dni);
        this.reservaPaciente = p
          ? { idPaciente: p.idPaciente, nombre: p.nombre, dni: p.dni }
          : null;
      },
      error: () => { this.reservaPaciente = null; }
    });
  }

  seleccionarSlotReserva(slot: SlotHorario): void {
    if (!slot.disponible || slot.pasada) return;
    this.slotSeleccionado = slot.hora;
  }

  datosReservaListos(): boolean {
    return !!this.reservaPaciente && !!this.idEspHorario && !!this.idMedicoHorario
      && !!this.fechaHorario && !!this.slotSeleccionado;
  }

  confirmarReserva(): void {
    if (!this.reservaPaciente) {
      this.mensajeReserva = 'Ingrese y busque el DNI del paciente arriba antes de confirmar.';
      return;
    }
    if (!this.datosReservaListos()) {
      this.mensajeReserva = 'Seleccione paciente, especialidad, médico, fecha y hora.';
      return;
    }
    if (!this.motivoReserva || !this.sintomasReserva) {
      this.mensajeReserva = 'Ingrese motivo y síntomas.';
      return;
    }
    this.isGuardandoCita = true;
    this.mensajeReserva = '';

    const body = {
      idPaciente: this.reservaPaciente.idPaciente,
      idEspecialidad: this.idEspHorario,
      idMedico: this.idMedicoHorario,
      fecha: this.fechaHorario,
      hora: this.slotSeleccionado,
      motivoConsulta: this.motivoReserva,
      sintomas: this.sintomasReserva
    };

    this.http.post<any>(`${this.apiUrlBase}/citas`, body).subscribe({
      next: (resp) => {
        const medico = this.medicosHorario.find(m => m.idMedico === this.idMedicoHorario);
        const esp = this.especialidadesHorario.find(e => e.idEspecialidad === this.idEspHorario);
        this.citaSeleccionada = {
          idCita: resp.idCita,
          nombrePaciente: this.reservaPaciente.nombre,
          dniPaciente: this.reservaPaciente.dni,
          nombreMedico: medico ? `${medico.nombres} ${medico.apellidos}` : '',
          especialidad: esp ? esp.nombre : '',
          fecha: this.fechaHorario,
          hora: this.slotSeleccionado!.substring(0, 5),
          motivoConsulta: this.motivoReserva,
          estado: 'pendiente',
          precio: esp && esp.precio ? Number(esp.precio) : 0,
          tieneSIS: false
        };
        this.isGuardandoCita = false;
        this.mensajeReserva = 'Cita registrada. Proceda con el pago abajo.';
        this.slotSeleccionado = null;
        this.motivoReserva = '';
        this.sintomasReserva = '';
        this.cargarHorariosMedico();
        if (this.citaSeleccionada && !this.citasEncontradas.some(c => c.idCita === this.citaSeleccionada!.idCita)) {
          this.citasEncontradas = [this.citaSeleccionada, ...this.citasEncontradas];
        }
      },
      error: (err) => {
        this.mensajeReserva = err.error?.error || 'Error al registrar la cita.';
        this.isGuardandoCita = false;
      }
    });
  }

  cerrarSesion(): void {
    this.recepcionistaService.cerrarSesion();
    this.router.navigate(['/recepcionista/login']);
  }

  confirmarRegistroRapido(desea: boolean): void {
    this.pacienteNoEncontrado = false;
    if (desea) {
      this.registroRapido = {
        dni: this.dniBusqueda, nombres: '', apellidos: '',
        sexo: 'M', fechaNacimiento: ''
      };
      this.errorRegistro = '';
      this.errorMessage = '';
      this.mostrarRegistroRapido = true;
    } else {
      this.errorMessage = '';
    }
  }

  cerrarRegistroRapido(): void {
    this.mostrarRegistroRapido = false;
    this.errorRegistro = '';
  }

  guardarRegistroRapido(): void {
    const r = this.registroRapido;
    if (!r.nombres || !r.apellidos || !r.sexo || !r.fechaNacimiento) {
      this.errorRegistro = 'Complete nombres, apellidos, sexo y fecha de nacimiento.';
      return;
    }
    this.isGuardandoPaciente = true;
    this.errorRegistro = '';

    const body = {
      dni: r.dni,
      nombres: r.nombres,
      apellidos: r.apellidos,
      sexo: r.sexo,
      correo: `${r.dni}@paciente.hospital`,
      telefono: null,
      fechaNacimiento: r.fechaNacimiento,
      clave: r.dni
    };

    this.http.post<any>(`${this.apiUrlBase}/registro`, body).subscribe({
      next: (resp) => {
        this.isGuardandoPaciente = false;
        this.mostrarRegistroRapido = false;
        this.errorMessage = '';
        this.reservaPaciente = {
          idPaciente: resp.idPaciente,
          nombre: `${r.nombres} ${r.apellidos}`,
          dni: r.dni
        };
        alert('Cuenta creada con éxito. Usuario y contraseña: ' + r.dni);
      },
      error: (err) => {
        this.errorRegistro = err.error?.error || 'Error al registrar paciente.';
        this.isGuardandoPaciente = false;
      }
    });
  }

  cargarEspecialidadesHorario(): void {
    this.http.get<any[]>(`${this.apiUrlBase}/especialidades`).subscribe({
      next: (data) => { this.especialidadesHorario = data; },
      error: (err) => { console.error('Error al cargar especialidades:', err); }
    });
  }

  onEspHorarioChange(): void {
    this.medicosHorario = [];
    this.idMedicoHorario = null;
    this.slotsHorario = [];
    this.errorHorarios = '';
    this.diasDisponibles = [];
    this.disponibilidadMedico = [];
    this.fechaHorario = '';
    if (!this.idEspHorario) return;
    this.http.get<any[]>(`${this.apiUrlBase}/especialidades/${this.idEspHorario}/medicos`).subscribe({
      next: (data) => { this.medicosHorario = data; },
      error: (err) => {
        console.error('Error al cargar médicos:', err);
        this.errorHorarios = 'Error al cargar médicos.';
      }
    });
  }

  onHorarioFiltroChange(): void {
    this.cargarHorariosMedico();
  }

  onMedicoHorarioChange(): void {
    this.fechaHorario = '';
    this.slotsHorario = [];
    this.errorHorarios = '';
    if (!this.idMedicoHorario) {
      this.diasDisponibles = [];
      return;
    }
    this.semanaInicio = this.obtenerLunes(this.getTodayDate());
    this.http.get<any[]>(`${this.apiUrlBase}/medicos/${this.idMedicoHorario}/disponibilidad`).subscribe({
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

  construirDiasDisponibles(): void {
    const nombresDias = ['lunes', 'martes', 'miercoles', 'jueves', 'viernes', 'sabado', 'domingo'];
    const diasAtiende = new Set(
      this.disponibilidadMedico
        .filter(d => Number(d.idEspecialidad) === Number(this.idEspHorario))
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
        disponible: !esDomingo && fecha >= hoy && diasAtiende.has(nombresDias[idx])
      });
    }
  }

  seleccionarDia(dia: any): void {
    if (!dia.disponible) return;
    this.fechaHorario = dia.fecha;
    this.cargarHorariosMedico();
  }

  semanaAnterior(): void {
    const f = new Date(this.semanaInicio + 'T00:00:00');
    f.setDate(f.getDate() - 7);
    if (this.toFechaStr(f) < this.obtenerLunes(this.getTodayDate())) return;
    this.semanaInicio = this.toFechaStr(f);
    this.construirDiasDisponibles();
  }

  semanaSiguiente(): void {
    const f = new Date(this.semanaInicio + 'T00:00:00');
    f.setDate(f.getDate() + 7);
    this.semanaInicio = this.toFechaStr(f);
    this.construirDiasDisponibles();
  }

  cargarHorariosMedico(): void {
    this.slotsHorario = [];
    this.errorHorarios = '';
    if (!this.idMedicoHorario || !this.fechaHorario) return;

    const fechaObj = new Date(this.fechaHorario + 'T00:00:00');
    const dias = ['domingo', 'lunes', 'martes', 'miercoles', 'jueves', 'viernes', 'sabado'];
    const diaSemanaNombre = dias[fechaObj.getDay()];

    if (diaSemanaNombre === 'domingo') {
      this.errorHorarios = 'No hay citas disponibles los domingos.';
      return;
    }

    this.isLoadingHorarios = true;

    const getDisponibilidad = this.http.get<any[]>(`${this.apiUrlBase}/medicos/${this.idMedicoHorario}/disponibilidad`);
    const getReservadas = this.http.get<string[]>(`${this.apiUrlBase}/citas/reservadas/${this.idMedicoHorario}/${this.fechaHorario}`);
    const getAdicionales = this.http.get<any[]>(`${this.apiUrlBase}/medicos/${this.idMedicoHorario}/cupos-adicionales/fecha?fecha=${this.fechaHorario}`);

    forkJoin([getDisponibilidad, getReservadas, getAdicionales]).subscribe({
      next: ([disponibilidades, horasReservadas, cuposAdicionales]) => {
        const horariosDelDia = disponibilidades.filter(d => d.diaSemana === diaSemanaNombre);
        const slots: SlotHorario[] = [];

        const cuposValidos = (cuposAdicionales || []);
        const horasAdicionales = new Set(cuposValidos.map((c: any) => c.horaInicio.substring(0, 8)));

        horariosDelDia.forEach(bloque => {
          let inicio = this.parseHora(bloque.horaInicio);
          const fin = this.parseHora(bloque.horaFin);
          let finSlot = new Date(inicio.getTime() + 20 * 60000);
          while (finSlot.getTime() <= fin.getTime()) {
            const hora = this.formatHora(inicio);
            if (!horasAdicionales.has(hora)) {
              slots.push({ hora, disponible: !horasReservadas.includes(hora), esAdicional: false, pasada: this.esHoraPasada(hora, this.fechaHorario) });
            }
            inicio = new Date(finSlot.getTime());
            finSlot = new Date(inicio.getTime() + 20 * 60000);
          }
        });

        cuposValidos
          .forEach(cupo => {
            const hora = cupo.horaInicio.substring(0, 8);
            if (!slots.some(s => s.hora === hora)) {
              const pasada = this.esHoraPasada(hora, this.fechaHorario);
              const libre = cupo.disponible && !horasReservadas.includes(hora) && !pasada;
              slots.push({ hora, disponible: libre, esAdicional: true, pasada });
            }
          });

        slots.sort((a, b) => a.hora.localeCompare(b.hora));
        this.slotsHorario = slots;

        if (slots.length === 0) {
          this.errorHorarios = `El médico no tiene horarios registrados para el ${diaSemanaNombre} ${this.fechaHorario}.`;
        } else if (!slots.some(s => s.disponible)) {
          this.errorHorarios = 'Todos los cupos para esta fecha ya están reservados.';
        }
        this.isLoadingHorarios = false;
      },
      error: (err) => {
        console.error('Error al cargar horarios:', err);
        this.errorHorarios = 'Error al cargar los horarios del médico.';
        this.isLoadingHorarios = false;
      }
    });
  }

  getTodayDate(): string {
    return new Date().toISOString().split('T')[0];
  }

  private parseHora(timeString: string): Date {
    const [hours, minutes, seconds] = timeString.split(':').map(Number);
    const date = new Date();
    date.setHours(hours, minutes, seconds || 0, 0);
    return date;
  }

  private formatHora(date: Date): string {
    const h = date.getHours().toString().padStart(2, '0');
    const m = date.getMinutes().toString().padStart(2, '0');
    return `${h}:${m}:00`;
  }

  formatHoraCorta(hora: string): string {
    return hora ? hora.substring(0, 5) : '';
  }

  esHoraPasada(hora: string, fecha: string): boolean {
    if (!fecha || fecha !== this.getTodayDate()) return false;
    const ahora = new Date();
    const [h, m] = hora.split(':').map(Number);
    const slotDate = new Date();
    slotDate.setHours(h, m, 0, 0);
    return slotDate.getTime() <= ahora.getTime();
  }

  get citaParaPago(): CitaPacienteRecepcionista | undefined {
    return this.citaSeleccionada ?? undefined;
  }

  esCitaSeleccionada(idCita: number): boolean {
    return this.citaSeleccionada?.idCita === idCita;
  }

  get tieneCitas(): boolean {
    return this.citasEncontradas.length > 0;
  }
}
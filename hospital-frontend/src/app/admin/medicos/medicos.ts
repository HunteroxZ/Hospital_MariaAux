// app/admin/medicos/medicos.component.ts

import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { MedicoService } from '../../services/medico.service';
import { EspecialidadService } from '../../services/especialidad.service';
import { DisponibilidadService, DisponibilidadMedico, RegistrarDisponibilidadRequest } from '../../services/disponibilidad.service';
import { Medico, RegistrarMedicoRequest, ActualizarMedicoRequest, Especialidad } from '../../models/medico.model';

@Component({
  selector: 'app-medicos',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './medicos.html',
  styleUrls: ['./medicos.css']
})
export class Medicos implements OnInit {
  medicos: Medico[] = [];
  medicosFiltrados: Medico[] = [];
  especialidades: Especialidad[] = [];
  disponibilidades: DisponibilidadMedico[] = [];
  

  mostrarFormulario = false;
  modoEdicion = false;
  mostrarDetalle = false;
  mostrarModalEspecialidades = false;

  busqueda = '';
  filtroEstado: 'TODOS' | 'ACTIVO' | 'INACTIVO' = 'TODOS';

  medicoSeleccionado: Medico | null = null;
  especialidadesMedico: Especialidad[] = [];
  

  medicoForm: RegistrarMedicoRequest = {
    nombres: '',
    apellidos: '',
    dni: '',
    codigoColegiatura: '',
    correo: '',
    clave: '',
    direccion: '',
    telefono: '',
    fechaNacimiento: ''
  };


  horarioForm: RegistrarDisponibilidadRequest = {
    idEspecialidad: 0,
    diaSemana: 'lunes',
    horaInicio: '',
    horaFin: ''
  };

  diasSemana = ['lunes', 'martes', 'miercoles', 'jueves', 'viernes', 'sabado'];
  

  errorMessage = '';
  successMessage = '';
  

  totalMedicos = 0;
  medicosActivos = 0;
  medicosInactivos = 0;

  fechaMaxima: string = '';

  constructor(
    private router: Router,
    private medicoService: MedicoService,
    private especialidadService: EspecialidadService,
    private disponibilidadService: DisponibilidadService
  ) {
      const hoy = new Date();
      this.fechaMaxima = hoy.toISOString().split('T')[0];
  }

  ngOnInit(): void {
    this.cargarMedicos();
    this.cargarEspecialidades();
  }

  cargarMedicos(): void {
    this.medicoService.obtenerTodos().subscribe({
      next: (data) => {
        this.medicos = data;
        this.aplicarFiltros();
        this.calcularEstadisticas();
      },
      error: (err) => {
        console.error('Error al cargar médicos:', err);
        this.errorMessage = 'Error al cargar la lista de médicos';
      }
    });
  }

  cargarEspecialidades(): void {
    this.especialidadService.obtenerTodas().subscribe({
      next: (data) => {
        this.especialidades = data;
      },
      error: (err) => {
        console.error('Error al cargar especialidades:', err);
      }
    });
  }

  aplicarFiltros(): void {
    this.medicosFiltrados = this.medicos.filter(medico => {
      const cumpleBusqueda = 
        medico.nombres.toLowerCase().includes(this.busqueda.toLowerCase()) ||
        medico.apellidos.toLowerCase().includes(this.busqueda.toLowerCase()) ||
        medico.dni.includes(this.busqueda) ||
        medico.codigoColegiatura.toLowerCase().includes(this.busqueda.toLowerCase());
      
      const cumpleEstado = 
        this.filtroEstado === 'TODOS' || medico.estado === this.filtroEstado;
      
      return cumpleBusqueda && cumpleEstado;
    });
  }

  calcularEstadisticas(): void {
    this.totalMedicos = this.medicos.length;
    this.medicosActivos = this.medicos.filter(m => m.estado === 'ACTIVO').length;
    this.medicosInactivos = this.medicos.filter(m => m.estado === 'INACTIVO').length;
  }

  abrirFormularioNuevo(): void {
    this.modoEdicion = false;
    this.mostrarFormulario = true;
    this.limpiarFormulario();
  }

  abrirFormularioEditar(medico: Medico): void {
    this.modoEdicion = true;
    this.mostrarFormulario = true;
    this.medicoSeleccionado = medico;
    this.medicoForm = {
      nombres: medico.nombres,
      apellidos: medico.apellidos,
      dni: medico.dni,
      codigoColegiatura: medico.codigoColegiatura,
      correo: medico.correo,
      clave: '',
      direccion: medico.direccion || '',
      telefono: medico.telefono || '',
      fechaNacimiento: medico.fechaNacimiento || ''
    };
  }

  cerrarFormulario(): void {
    this.mostrarFormulario = false;
    this.limpiarFormulario();
  }

  limpiarFormulario(): void {
    this.medicoForm = {
      nombres: '',
      apellidos: '',
      dni: '',
      codigoColegiatura: '',
      correo: '',
      clave: '',
      direccion: '',
      telefono: '',
      fechaNacimiento: ''
    };
    this.medicoSeleccionado = null;
    this.errorMessage = '';
  }

  guardarMedico(): void {

    if (!this.validarFechaNoFutura(this.medicoForm.fechaNacimiento || '')) {
      this.errorMessage = 'La fecha de nacimiento no puede ser mayor a hoy';
      return;
    }

    if (this.medicoForm.fechaNacimiento && !this.validarEdadMinima(this.medicoForm.fechaNacimiento)) {
      this.errorMessage = 'El médico debe ser mayor de 18 años';
      return;
    }

    if (this.modoEdicion && this.medicoSeleccionado) {
      const request: ActualizarMedicoRequest = {
        nombres: this.medicoForm.nombres,
        apellidos: this.medicoForm.apellidos,
        correo: this.medicoForm.correo,
        direccion: this.medicoForm.direccion,
        telefono: this.medicoForm.telefono,
        fechaNacimiento: this.medicoForm.fechaNacimiento
      };
      
      if (this.medicoForm.clave) {
        request.clave = this.medicoForm.clave;
      }

      this.medicoService.actualizar(this.medicoSeleccionado.idMedico!, request).subscribe({
        next: () => {
          this.successMessage = 'Médico actualizado correctamente';
          this.cargarMedicos();
          this.cerrarFormulario();
          setTimeout(() => this.successMessage = '', 3000);
        },
        error: (err) => {
          console.error('Error al actualizar médico:', err);
          this.errorMessage = err.error?.error || 'Error al actualizar el médico';
        }
      });
    } else {
      this.medicoService.registrar(this.medicoForm).subscribe({
        next: () => {
          this.successMessage = 'Médico registrado correctamente';
          this.cargarMedicos();
          this.cerrarFormulario();
          setTimeout(() => this.successMessage = '', 3000);
        },
        error: (err) => {
          console.error('Error al registrar médico:', err);
          this.errorMessage = err.error?.error || 'Error al registrar el médico';
        }
      });
    }
  }

  verDetalle(medico: Medico): void {
    this.medicoSeleccionado = medico;
    this.mostrarDetalle = true;
    this.cargarEspecialidadesMedico(medico.idMedico!);
  }

  cerrarDetalle(): void {
    this.mostrarDetalle = false;
    this.medicoSeleccionado = null;
    this.especialidadesMedico = [];
  }

  cargarEspecialidadesMedico(idMedico: number): void {
    this.medicoService.obtenerEspecialidades(idMedico).subscribe({
      next: (data) => {
        this.especialidadesMedico = data;
      },
      error: (err) => {
        console.error('Error al cargar especialidades del médico:', err);
      }
    });
  }

  cambiarEstado(medico: Medico): void {
    const nuevoEstado = medico.estado === 'ACTIVO' ? 'INACTIVO' : 'ACTIVO';
    
    if (confirm(`¿Está seguro de cambiar el estado del médico a ${nuevoEstado}?`)) {
      const request: ActualizarMedicoRequest = { estado: nuevoEstado };
      
      this.medicoService.actualizar(medico.idMedico!, request).subscribe({
        next: () => {
          this.successMessage = `Estado cambiado a ${nuevoEstado}`;
          this.cargarMedicos();
          setTimeout(() => this.successMessage = '', 3000);
        },
        error: (err) => {
          console.error('Error al cambiar estado:', err);
          this.errorMessage = 'Error al cambiar el estado del médico';
        }
      });
    }
  }


  abrirModalEspecialidades(medico: Medico): void {
    this.medicoSeleccionado = medico;
    this.mostrarModalEspecialidades = true;
    this.cargarEspecialidadesMedico(medico.idMedico!);
    this.cargarDisponibilidades(medico.idMedico!);
    this.limpiarFormularioHorario();
  }

  cerrarModalEspecialidades(): void {
    this.mostrarModalEspecialidades = false;
    this.medicoSeleccionado = null;
    this.especialidadesMedico = [];
    this.disponibilidades = [];
    this.limpiarFormularioHorario();
  }

  asignarEspecialidad(idEspecialidad: number): void {
    if (!this.medicoSeleccionado) return;
    
    this.medicoService.asignarEspecialidad(this.medicoSeleccionado.idMedico!, idEspecialidad).subscribe({
      next: () => {
        this.successMessage = 'Especialidad asignada correctamente';
        this.cargarEspecialidadesMedico(this.medicoSeleccionado!.idMedico!);
        setTimeout(() => this.successMessage = '', 3000);
      },
      error: (err) => {
        console.error('Error al asignar especialidad:', err);
        this.errorMessage = err.error?.error || 'Error al asignar la especialidad';
      }
    });
  }

  quitarEspecialidad(idEspecialidad: number): void {
    if (!this.medicoSeleccionado) return;
    
    if (confirm('¿Está seguro de quitar esta especialidad? Se eliminarán también todos los horarios asociados.')) {
      this.medicoService.quitarEspecialidad(this.medicoSeleccionado.idMedico!, idEspecialidad).subscribe({
        next: () => {
          this.successMessage = 'Especialidad quitada correctamente';
          this.cargarEspecialidadesMedico(this.medicoSeleccionado!.idMedico!);
          this.cargarDisponibilidades(this.medicoSeleccionado!.idMedico!); // Recargar horarios
          setTimeout(() => this.successMessage = '', 3000);
        },
        error: (err) => {
          console.error('Error al quitar especialidad:', err);
          this.errorMessage = err.error?.error || 'Error al quitar la especialidad';
        }
      });
    }
  }

  tieneEspecialidad(idEspecialidad: number): boolean {
    return this.especialidadesMedico.some(e => e.idEspecialidad === idEspecialidad);
  }



  cargarDisponibilidades(idMedico: number): void {
    this.disponibilidadService.obtenerHorarios(idMedico).subscribe({
      next: (data) => {
        this.disponibilidades = data;
      },
      error: (err) => {
        console.error('Error al cargar horarios:', err);
      }
    });
  }

  limpiarFormularioHorario(): void {
    this.horarioForm = {
      idEspecialidad: 0,
      diaSemana: 'lunes',
      horaInicio: '',
      horaFin: ''
    };
    this.errorMessage = '';
  }

  agregarHorario(): void {
    if (!this.medicoSeleccionado) return;

    if (!this.horarioForm.idEspecialidad || this.horarioForm.idEspecialidad === 0) {
      this.errorMessage = 'Debe seleccionar una especialidad';
      return;
    }

    if (!this.horarioForm.horaInicio || !this.horarioForm.horaFin) {
      this.errorMessage = 'Debe completar las horas de inicio y fin';
      return;
    }

    this.disponibilidadService.anadirHorario(this.medicoSeleccionado.idMedico!, this.horarioForm).subscribe({
      next: () => {
        this.successMessage = 'Horario agregado correctamente';
        this.cargarDisponibilidades(this.medicoSeleccionado!.idMedico!);
        this.limpiarFormularioHorario();
        setTimeout(() => this.successMessage = '', 3000);
      },
      error: (err) => {
        console.error('Error al agregar horario:', err);
        this.errorMessage = err.error?.error || 'Error al agregar el horario';
      }
    });
  }

  eliminarHorario(idDisponibilidad: number): void {
    if (!this.medicoSeleccionado) return;

    if (confirm('¿Está seguro de eliminar este horario?')) {
      this.disponibilidadService.eliminarHorario(this.medicoSeleccionado.idMedico!, idDisponibilidad).subscribe({
        next: () => {
          this.successMessage = 'Horario eliminado correctamente';
          this.cargarDisponibilidades(this.medicoSeleccionado!.idMedico!);
          setTimeout(() => this.successMessage = '', 3000);
        },
        error: (err) => {
          console.error('Error al eliminar horario:', err);
          this.errorMessage = err.error?.error || 'Error al eliminar el horario';
        }
      });
    }
  }

  obtenerNombreEspecialidad(idEspecialidad: number): string {
    const esp = this.especialidades.find(e => e.idEspecialidad === idEspecialidad);
    return esp ? esp.nombre : 'Desconocida';
  }

  obtenerHorariosPorEspecialidad(idEspecialidad: number): DisponibilidadMedico[] {
    return this.disponibilidades.filter(d => d.idEspecialidad === idEspecialidad);
  }

  cerrarSesion(): void {
    localStorage.removeItem('admin');
    this.router.navigate(['/admin/login']);
  }

  validarEdadMinima(fecha: string): boolean {
    if (!fecha) return true;
    
    const fechaNacimiento = new Date(fecha);
    const hoy = new Date();
    hoy.setHours(0, 0, 0, 0);
    
    let edad = hoy.getFullYear() - fechaNacimiento.getFullYear();
    const mes = hoy.getMonth() - fechaNacimiento.getMonth();
    const dia = hoy.getDate() - fechaNacimiento.getDate();
    
    if (mes < 0 || (mes === 0 && dia < 0)) {
      edad--;
    }
    
    return edad >= 18;
  }
  validarFechaNoFutura(fecha: string): boolean {
    if (!fecha) return true;
    
    const fechaIngresada = new Date(fecha);
    const hoy = new Date();
    hoy.setHours(0, 0, 0, 0);
    
    return fechaIngresada <= hoy;
  }
  
}
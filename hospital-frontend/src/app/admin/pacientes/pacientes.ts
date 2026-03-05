
import { CommonModule, NgIf } from '@angular/common';
import { Component, OnInit, ViewChild, ElementRef } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { PacienteAdminService } from '../../services/paciente-admin.service';
import { PacienteInfo } from '../../models/paciente-info.model';
import { PacienteEdicion } from '../../models/paciente-edicion.model';
import { Subject } from 'rxjs';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';

@Component({
  selector: 'app-pacientes',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule, NgIf],
  templateUrl: './pacientes.html',
  styleUrls: ['./pacientes.css']
})
export class Pacientes implements OnInit {

  @ViewChild('inputBusqueda') inputBusqueda!: ElementRef;

  pacientes: PacienteInfo[] = [];
  pacientesPaginados: PacienteInfo[] = [];
  cargando: boolean = false;
  error: string = '';
  

  dniFiltro: string = '';
  private searchSubject = new Subject<string>();


  paginaActual: number = 1;
  itemsPorPagina: number = 10;
  totalPaginas: number = 0;


  mostrarModalDetalles: boolean = false;
  pacienteSeleccionado: any = null;
  cargandoDetalles: boolean = false;


  mostrarModalEdicion: boolean = false;
  pacienteEditar: PacienteEdicion = {};
  idPacienteEditar: number = 0;
  guardandoEdicion: boolean = false;

  constructor(
    private router: Router,
    private pacienteService: PacienteAdminService
  ) {}

  ngOnInit(): void {
    this.cargarPacientes();
    this.configurarBusquedaAutomatica();
  }


  configurarBusquedaAutomatica(): void {
    this.searchSubject
      .pipe(
        debounceTime(500),
        distinctUntilChanged()
      )
      .subscribe(searchText => {
        this.buscarPacientes(searchText);
      });
  }


  onBusquedaChange(): void {
    this.searchSubject.next(this.dniFiltro);
  }

  buscarPacientes(dni: string): void {
    const dniBusqueda = dni.trim();
    
    if (!dniBusqueda) {
      this.cargarPacientes();
      return;
    }

    this.cargando = true;
    this.error = '';

    this.pacienteService.obtenerPacientes(dniBusqueda).subscribe({
      next: (data) => {
        this.pacientes = data;
        this.paginaActual = 1;
        this.calcularPaginacion();
        this.cargando = false;
        
        if (data.length === 0) {
          this.error = `No se encontraron pacientes con DNI que inicie con: ${dniBusqueda}`;
        }


        setTimeout(() => {
          if (this.inputBusqueda && this.inputBusqueda.nativeElement) {
            this.inputBusqueda.nativeElement.focus();
          }
        }, 0);
      },
      error: (err) => {
        console.error('Error al buscar pacientes:', err);
        this.error = 'Error al buscar pacientes. Por favor, intente nuevamente.';
        this.cargando = false;
        this.pacientes = [];
        this.calcularPaginacion();
      }
    });
  }

  cargarPacientes(): void {
    this.cargando = true;
    this.error = '';
    
    this.pacienteService.obtenerPacientes().subscribe({
      next: (data) => {
        this.pacientes = data;
        this.calcularPaginacion();
        this.cargando = false;

        if (this.dniFiltro) {
          setTimeout(() => {
            if (this.inputBusqueda && this.inputBusqueda.nativeElement) {
              this.inputBusqueda.nativeElement.focus();
            }
          }, 0);
        }
      },
      error: (err) => {
        console.error('Error al cargar pacientes:', err);
        this.error = 'Error al cargar los pacientes. Por favor, intente nuevamente.';
        this.cargando = false;
        this.pacientes = [];
        this.calcularPaginacion();
      }
    });
  }


  limpiarFiltro(): void {
    this.dniFiltro = '';
    this.error = '';
    this.cargarPacientes();
  }


  calcularPaginacion(): void {
    this.totalPaginas = Math.ceil(this.pacientes.length / this.itemsPorPagina);
    this.actualizarPaginaActual();
  }


  actualizarPaginaActual(): void {
    const inicio = (this.paginaActual - 1) * this.itemsPorPagina;
    const fin = inicio + this.itemsPorPagina;
    this.pacientesPaginados = this.pacientes.slice(inicio, fin);
  }


  irAPagina(pagina: number): void {
    if (pagina >= 1 && pagina <= this.totalPaginas) {
      this.paginaActual = pagina;
      this.actualizarPaginaActual();
    }
  }

  getPaginas(): number[] {
    return Array.from({ length: this.totalPaginas }, (_, i) => i + 1);
  }


  validarSoloNumeros(event: KeyboardEvent): void {
    const charCode = event.which ? event.which : event.keyCode;
    if (charCode > 31 && (charCode < 48 || charCode > 57)) {
      event.preventDefault();
    }
  }


  verDetalles(paciente: PacienteInfo): void {
    this.mostrarModalDetalles = true;
    this.cargandoDetalles = true;
    
    this.pacienteService.obtenerPacienteCompleto(paciente.idPaciente).subscribe({
      next: (data) => {
        this.pacienteSeleccionado = data;
        this.cargandoDetalles = false;
      },
      error: (err) => {
        console.error('Error al obtener detalles:', err);
        this.pacienteSeleccionado = null;
        this.cargandoDetalles = false;
        alert('Error al cargar los detalles del paciente');
        this.cerrarModalDetalles();
      }
    });
  }

  cerrarModalDetalles(): void {
    this.mostrarModalDetalles = false;
    this.pacienteSeleccionado = null;
  }


  editarPaciente(paciente: PacienteInfo): void {
    this.idPacienteEditar = paciente.idPaciente;
    this.mostrarModalEdicion = true;
    this.cargandoDetalles = true;

    this.pacienteService.obtenerPacienteCompleto(paciente.idPaciente).subscribe({
      next: (data) => {
        this.pacienteEditar = {
          telefono: data.telefono || '',
          direccion: data.direccion || '',
          talla: data.talla || null,
          peso: data.peso || null,
          clave: ''
        };
        this.cargandoDetalles = false;
      },
      error: (err) => {
        console.error('Error al cargar datos:', err);
        this.cargandoDetalles = false;
        alert('Error al cargar los datos del paciente');
        this.cerrarModalEdicion();
      }
    });
  }

  guardarEdicion(): void {
    this.guardandoEdicion = true;

    const datosActualizados: any = {
      telefono: this.pacienteEditar.telefono,
      direccion: this.pacienteEditar.direccion,
      talla: this.pacienteEditar.talla,
      peso: this.pacienteEditar.peso
    };

    if (this.pacienteEditar.clave && this.pacienteEditar.clave.trim()) {
      datosActualizados.clave = this.pacienteEditar.clave;
    }

    this.pacienteService.actualizarPaciente(this.idPacienteEditar, datosActualizados).subscribe({
      next: (response) => {
        alert('Paciente actualizado correctamente');
        this.guardandoEdicion = false;
        this.cerrarModalEdicion();
        
        if (this.dniFiltro.trim()) {
          this.buscarPacientes(this.dniFiltro);
        } else {
          this.cargarPacientes();
        }
      },
      error: (err) => {
        console.error('Error al actualizar:', err);
        alert('Error al actualizar el paciente');
        this.guardandoEdicion = false;
      }
    });
  }

  cerrarModalEdicion(): void {
    this.mostrarModalEdicion = false;
    this.pacienteEditar = {};
    this.idPacienteEditar = 0;
  }


  eliminarPaciente(paciente: PacienteInfo): void {
    const confirmacion = confirm(
      `¿Está seguro de eliminar al paciente?\n\n` +
      `Nombre: ${paciente.nombre}\n` +
      `DNI: ${paciente.dni}\n\n` +
      `Esta acción no se puede deshacer.`
    );

    if (confirmacion) {
      this.pacienteService.eliminarPaciente(paciente.idPaciente).subscribe({
        next: (response) => {
          alert('Paciente eliminado correctamente');
          
          if (this.dniFiltro.trim()) {
            this.buscarPacientes(this.dniFiltro);
          } else {
            this.cargarPacientes();
          }
        },
        error: (err) => {
          console.error('Error al eliminar:', err);
          alert('Error al eliminar el paciente. Es posible que tenga citas asociadas.');
        }
      });
    }
  }

  cerrarSesion(): void {
    localStorage.removeItem('admin');
    this.router.navigate(['/admin/login']);
  }

  ngOnDestroy(): void {
    this.searchSubject.complete();
  }
}
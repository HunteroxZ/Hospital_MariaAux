
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { RecepcionistaService } from '../../services/recepcionista.service';
import { CitaPacienteRecepcionista, PagoRequest } from '../../models/recepcionista.model';
import { MenuPagoComponent } from "../menu-pago/menu-pago.component";

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

  constructor(
    private recepcionistaService: RecepcionistaService,
    private router: Router
  ) {}

  ngOnInit(): void {
    const sesion = this.recepcionistaService.obtenerSesion();
    if (sesion) {
      this.nombreRecepcionista = `${sesion.nombres} ${sesion.apellidos}`;
    }
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

    this.recepcionistaService.buscarCitasPorDni(this.dniBusqueda).subscribe({
      next: (citas) => {
        this.citasEncontradas = citas;
        if (citas.length === 0) {
          this.errorMessage = 'No se encontraron citas pendientes para este DNI';
        }
      },
      error: (error) => {
        this.errorMessage = error.error?.error || 'Error al buscar citas';
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

      const ventana = window.open('', '_blank');
      if (!ventana) {
        console.error('No se pudo abrir ventana de impresión');
        return;
      }

      const contenido = this.generarHTMLTicketSIS();
      ventana.document.write(contenido);
      ventana.document.close();
      
      setTimeout(() => {
        ventana.print();
        ventana.close();
        this.mostrarModalTicketSIS = false;
      }, 500);

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
          // Aun así, actualizar la lista
          this.cerrarMenuPago();
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
          <h2>HOSPITAL MARÍA AUXILIADORA</h2>
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

  cerrarSesion(): void {
    this.recepcionistaService.cerrarSesion();
    this.router.navigate(['/recepcionista/login']);
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
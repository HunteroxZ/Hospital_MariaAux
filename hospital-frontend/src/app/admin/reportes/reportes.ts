
import { CommonModule } from '@angular/common';
import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { Chart } from 'chart.js/auto';
import { ReporteService } from '../../services/reporte.service';
import { EspecialidadService } from '../../services/especialidad.service';
import { MedicoService } from '../../services/medico.service';

@Component({
  selector: 'app-reportes',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './reportes.html',
  styleUrls: ['./reportes.css']
})
export class Reportes implements OnInit, OnDestroy {
  
  tipoReporte: string = '';
  loading = false;
  errorMessage = '';


  filtrosCitas = {
    fechaInicio: this.obtenerFechaInicioPorDefecto(),
    fechaFin: this.obtenerFechaHoy(),
    idEspecialidad: null as number | null,
    idMedico: null as number | null,
    estado: null as string | null
  };

  filtrosIngresos = {
    fechaInicio: this.obtenerFechaInicioPorDefecto(),
    fechaFin: this.obtenerFechaHoy()
  };

  filtrosAdicionales = {
    fechaInicio: this.obtenerFechaInicioPorDefecto(),
    fechaFin: this.obtenerFechaHoy(),
    fechaCorte: ''
  };


  datosCitas: any[] = [];
  datosIngresos: any = null;
  datosPacientes: any[] = [];
  datosMedicos: any[] = [];
  datosAdicionales: any = null;
  private chartsAdicionales: Chart[] = [];


  especialidades: any[] = [];
  medicos: any[] = [];

  constructor(
    private router: Router,
    private reporteService: ReporteService,
    private especialidadService: EspecialidadService,
    private medicoService: MedicoService
  ) {}

  ngOnInit(): void {
    this.cargarEspecialidades();
    this.cargarMedicos();
  }

  ngOnDestroy(): void {
    this.destruirChartsAdicionales();
  }

  cerrarSesion(): void {
    localStorage.removeItem('admin');
    this.router.navigate(['/admin/login']);
  }

  seleccionarTipo(tipo: string): void {
    this.tipoReporte = tipo;
    this.limpiarDatos();
    this.destruirChartsAdicionales();
    this.errorMessage = '';
  }

  cargarEspecialidades(): void {
    this.especialidadService.obtenerTodas().subscribe({
      next: (data) => {
        this.especialidades = data;
      },
      error: (error) => {
        console.error('Error al cargar especialidades:', error);
      }
    });
  }

  cargarMedicos(): void {
    this.medicoService.obtenerTodos().subscribe({
      next: (data) => {
        this.medicos = data;
      },
      error: (error) => {
        console.error('Error al cargar médicos:', error);
      }
    });
  }

  generarReporteCitas(): void {
    if (!this.validarFechas(this.filtrosCitas.fechaInicio, this.filtrosCitas.fechaFin)) {
      this.errorMessage = 'La fecha de inicio no puede ser mayor que la fecha fin.';
      return;
    }

    this.loading = true;
    this.errorMessage = '';

    this.reporteService.obtenerReporteCitas(
      this.filtrosCitas.fechaInicio,
      this.filtrosCitas.fechaFin,
      this.filtrosCitas.idEspecialidad || undefined,
      this.filtrosCitas.idMedico || undefined,
      this.filtrosCitas.estado || undefined
    ).subscribe({
      next: (data) => {
        this.datosCitas = data;
        this.loading = false;
        if (data.length === 0) {
          this.errorMessage = 'No se encontraron citas con los filtros seleccionados.';
        }
      },
      error: (error) => {
        console.error('Error al generar reporte:', error);
        this.errorMessage = 'Error al generar el reporte. Intente nuevamente.';
        this.loading = false;
      }
    });
  }

  descargarReporteCitasPDF(): void {
    this.loading = true;
    this.reporteService.descargarReporteCitasPDF(
      this.filtrosCitas.fechaInicio,
      this.filtrosCitas.fechaFin,
      this.filtrosCitas.idEspecialidad || undefined,
      this.filtrosCitas.idMedico || undefined,
      this.filtrosCitas.estado || undefined
    ).subscribe({
      next: (blob) => {
        this.descargarArchivo(blob, 'reporte_citas.pdf');
        this.loading = false;
      },
      error: (error) => {
        console.error('Error al descargar PDF:', error);
        this.errorMessage = 'Error al descargar el PDF. Intente nuevamente.';
        this.loading = false;
      }
    });
  }

  generarReporteIngresos(): void {
    if (!this.validarFechas(this.filtrosIngresos.fechaInicio, this.filtrosIngresos.fechaFin)) {
      this.errorMessage = 'La fecha de inicio no puede ser mayor que la fecha fin.';
      return;
    }

    this.loading = true;
    this.errorMessage = '';

    this.reporteService.obtenerReporteIngresos(
      this.filtrosIngresos.fechaInicio,
      this.filtrosIngresos.fechaFin
    ).subscribe({
      next: (data) => {
        this.datosIngresos = data;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error al generar reporte:', error);
        this.errorMessage = 'Error al generar el reporte. Intente nuevamente.';
        this.loading = false;
      }
    });
  }

  descargarReporteIngresosPDF(): void {
    this.loading = true;
    this.reporteService.descargarReporteIngresosPDF(
      this.filtrosIngresos.fechaInicio,
      this.filtrosIngresos.fechaFin
    ).subscribe({
      next: (blob) => {
        this.descargarArchivo(blob, 'reporte_ingresos.pdf');
        this.loading = false;
      },
      error: (error) => {
        console.error('Error al descargar PDF:', error);
        this.errorMessage = 'Error al descargar el PDF. Intente nuevamente.';
        this.loading = false;
      }
    });
  }

  generarReportePacientes(): void {
    this.loading = true;
    this.errorMessage = '';

    this.reporteService.obtenerReportePacientes().subscribe({
      next: (data) => {
        this.datosPacientes = data;
        this.loading = false;
        if (data.length === 0) {
          this.errorMessage = 'No hay pacientes registrados en el sistema.';
        }
      },
      error: (error) => {
        console.error('Error al generar reporte:', error);
        this.errorMessage = 'Error al generar el reporte. Intente nuevamente.';
        this.loading = false;
      }
    });
  }

  descargarReportePacientesPDF(): void {
    this.loading = true;
    this.reporteService.descargarReportePacientesPDF().subscribe({
      next: (blob) => {
        this.descargarArchivo(blob, 'reporte_pacientes.pdf');
        this.loading = false;
      },
      error: (error) => {
        console.error('Error al descargar PDF:', error);
        this.errorMessage = 'Error al descargar el PDF. Intente nuevamente.';
        this.loading = false;
      }
    });
  }

  generarReporteMedicos(): void {
    this.loading = true;
    this.errorMessage = '';

    this.reporteService.obtenerReporteMedicos().subscribe({
      next: (data) => {
        this.datosMedicos = data;
        this.loading = false;
        if (data.length === 0) {
          this.errorMessage = 'No hay médicos registrados en el sistema.';
        }
      },
      error: (error) => {
        console.error('Error al generar reporte:', error);
        this.errorMessage = 'Error al generar el reporte. Intente nuevamente.';
        this.loading = false;
      }
    });
  }

  descargarReporteMedicosPDF(): void {
    this.loading = true;
    this.reporteService.descargarReporteMedicosPDF().subscribe({
      next: (blob) => {
        this.descargarArchivo(blob, 'reporte_medicos.pdf');
        this.loading = false;
      },
      error: (error) => {
        console.error('Error al descargar PDF:', error);
        this.errorMessage = 'Error al descargar el PDF. Intente nuevamente.';
        this.loading = false;
      }
    });
  }

  generarReporteAdicionales(): void {
    if (!this.validarFechas(this.filtrosAdicionales.fechaInicio, this.filtrosAdicionales.fechaFin)) {
      this.errorMessage = 'La fecha de inicio no puede ser mayor que la fecha fin.';
      return;
    }

    this.loading = true;
    this.errorMessage = '';

    this.reporteService.obtenerReporteAdicionales(
      this.filtrosAdicionales.fechaInicio,
      this.filtrosAdicionales.fechaFin,
      this.filtrosAdicionales.fechaCorte || undefined
    ).subscribe({
      next: (data) => {
        this.datosAdicionales = data;
        this.loading = false;
        if (!data || data.totalCitas === 0) {
          this.errorMessage = 'No se encontraron citas con los filtros seleccionados.';
        } else {
          setTimeout(() => this.renderChartsAdicionales(), 0);
        }
      },
      error: (error) => {
        console.error('Error al generar reporte:', error);
        this.errorMessage = 'Error al generar el reporte. Intente nuevamente.';
        this.loading = false;
      }
    });
  }

  descargarReporteAdicionalesPDF(): void {
    this.loading = true;
    this.reporteService.descargarReporteAdicionalesPDF(
      this.filtrosAdicionales.fechaInicio,
      this.filtrosAdicionales.fechaFin,
      this.filtrosAdicionales.fechaCorte || undefined
    ).subscribe({
      next: (blob) => {
        this.descargarArchivo(blob, 'reporte_adicionales.pdf');
        this.loading = false;
        setTimeout(() => this.renderChartsAdicionales(), 0);
      },
      error: (error) => {
        console.error('Error al descargar PDF:', error);
        this.errorMessage = 'Error al descargar el PDF. Intente nuevamente.';
        this.loading = false;
        setTimeout(() => this.renderChartsAdicionales(), 0);
      }
    });
  }

  nombreAnonimo(index: number): string {
    return `Médico ${index + 1}`;
  }

  private destruirChartsAdicionales(): void {
    this.chartsAdicionales.forEach(c => c.destroy());
    this.chartsAdicionales = [];
  }

  private renderChartsAdicionales(): void {
    if (!this.datosAdicionales) return;
    this.destruirChartsAdicionales();

    const porMedico = this.datosAdicionales.porMedico || [];
    const porEsp = this.datosAdicionales.porEspecialidad || [];

    const canvasMedicos = document.getElementById('chartAdicionalesMedicos') as HTMLCanvasElement | null;
    if (canvasMedicos && porMedico.length > 0) {
      this.chartsAdicionales.push(new Chart(canvasMedicos, {
        type: 'bar',
        data: {
          labels: porMedico.map((_: any, i: number) => this.nombreAnonimo(i)),
          datasets: [
            { label: 'Normales', data: porMedico.map((m: any) => m.citasNormales), backgroundColor: '#5dade2' },
            { label: 'Adicionales', data: porMedico.map((m: any) => m.citasAdicionales), backgroundColor: '#27ae60' }
          ]
        },
        options: { responsive: true, maintainAspectRatio: false, plugins: { title: { display: true, text: 'Citas por médico (anónimo)' } } }
      }));
    }

    const canvasEsp = document.getElementById('chartAdicionalesEspecialidad') as HTMLCanvasElement | null;
    if (canvasEsp && porEsp.length > 0) {
      this.chartsAdicionales.push(new Chart(canvasEsp, {
        type: 'bar',
        data: {
          labels: porEsp.map((e: any) => e.nombreEspecialidad),
          datasets: [
            { label: 'Normales', data: porEsp.map((e: any) => e.citasNormales), backgroundColor: '#5dade2' },
            { label: 'Adicionales', data: porEsp.map((e: any) => e.citasAdicionales), backgroundColor: '#27ae60' }
          ]
        },
        options: { responsive: true, maintainAspectRatio: false, plugins: { title: { display: true, text: 'Citas por especialidad' } } }
      }));
    }

    const canvasComp = document.getElementById('chartAdicionalesComparativa') as HTMLCanvasElement | null;
    if (canvasComp) {
      this.chartsAdicionales.push(new Chart(canvasComp, {
        type: 'bar',
        data: {
          labels: ['Antes del corte', 'Después del corte'],
          datasets: [
            { label: 'Promedio diario de adicionales', data: [this.datosAdicionales.promedioDiarioAntes, this.datosAdicionales.promedioDiarioDespues], backgroundColor: ['#f39c12', '#27ae60'] }
          ]
        },
        options: { responsive: true, maintainAspectRatio: false, plugins: { title: { display: true, text: 'Promedio diario antes vs después' } } }
      }));
    }
  }


  private descargarArchivo(blob: Blob, nombreArchivo: string): void {
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = nombreArchivo;
    link.click();
    window.URL.revokeObjectURL(url);
  }

  private validarFechas(fechaInicio: string, fechaFin: string): boolean {
    return new Date(fechaInicio) <= new Date(fechaFin);
  }

  private limpiarDatos(): void {
    this.datosCitas = [];
    this.datosIngresos = null;
    this.datosPacientes = [];
    this.datosMedicos = [];
    this.datosAdicionales = null;
  }

  private obtenerFechaHoy(): string {
    return new Date().toISOString().split('T')[0];
  }

  private obtenerFechaInicioPorDefecto(): string {
    const fecha = new Date();
    fecha.setMonth(fecha.getMonth() - 1); 
    return fecha.toISOString().split('T')[0];
  }

  mostrarMensajeVacio(): boolean {
    if (this.tipoReporte === 'citas' && this.datosCitas.length === 0) return true;
    if (this.tipoReporte === 'ingresos' && !this.datosIngresos) return true;
    if (this.tipoReporte === 'pacientes' && this.datosPacientes.length === 0) return true;
    if (this.tipoReporte === 'medicos' && this.datosMedicos.length === 0) return true;
    if (this.tipoReporte === 'adicionales' && !this.datosAdicionales) return true;
    return false;
  }
}

// src/app/admin/inicio/inicio.ts
import { Component, OnInit } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { DashboardService } from '../../services/dashboard.service';
import { DashboardStats, Notificacion } from '../../models/dashboard.model';

@Component({
  selector: 'app-inicio',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './inicio.html',
  styleUrls: ['./inicio.css']
})
export class Inicio implements OnInit {
  admin = JSON.parse(localStorage.getItem('admin') || '{}');
  stats: DashboardStats | null = null;
  loading = true;
  errorMessage = '';
  
  usarRangoPersonalizado = false;
  fechaInicio: string = '';
  fechaFin: string = '';
  
  notificacionesActivas: Notificacion[] = [];

  constructor(
    private router: Router,
    private dashboardService: DashboardService
  ) {}

  ngOnInit(): void {
    this.inicializarFechas();
    this.cargarEstadisticas();
  }

  inicializarFechas(): void {
    const hoy = new Date();
    const hace7Dias = new Date();
    hace7Dias.setDate(hoy.getDate() - 6);
    
    this.fechaFin = this.formatearFechaInput(hoy);
    this.fechaInicio = this.formatearFechaInput(hace7Dias);
  }

  formatearFechaInput(fecha: Date): string {
    const year = fecha.getFullYear();
    const month = String(fecha.getMonth() + 1).padStart(2, '0');
    const day = String(fecha.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }

  cargarEstadisticas(): void {
    this.loading = true;
    this.errorMessage = '';

    if (this.usarRangoPersonalizado && this.fechaInicio && this.fechaFin) {
      this.cargarEstadisticasConRango();
    } else {
      this.dashboardService.obtenerEstadisticas().subscribe({
        next: (data) => {
          this.stats = data;
          this.notificacionesActivas = data.notificaciones || [];
          this.loading = false;
        },
        error: (error) => {
          console.error('Error al cargar estadísticas:', error);
          this.errorMessage = 'No se pudieron cargar las estadísticas. Intente nuevamente.';
          this.loading = false;
        }
      });
    }
  }

  cargarEstadisticasConRango(): void {
    if (!this.fechaInicio || !this.fechaFin) {
      this.errorMessage = 'Debe seleccionar ambas fechas';
      this.loading = false;
      return;
    }

    const inicio = new Date(this.fechaInicio);
    const fin = new Date(this.fechaFin);
    
    if (inicio > fin) {
      this.errorMessage = 'La fecha de inicio no puede ser posterior a la fecha fin';
      this.loading = false;
      return;
    }

    const diffTime = Math.abs(fin.getTime() - inicio.getTime());
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    
    if (diffDays > 30) {
      this.errorMessage = 'El rango de fechas no puede ser mayor a 30 días';
      this.loading = false;
      return;
    }

    this.dashboardService.obtenerEstadisticasConRango(this.fechaInicio, this.fechaFin).subscribe({
      next: (data) => {
        this.stats = data;
        this.notificacionesActivas = data.notificaciones || [];
        this.loading = false;
      },
      error: (error) => {
        console.error('Error al cargar estadísticas con rango:', error);
        this.errorMessage = 'Error al cargar estadísticas. Intente nuevamente.';
        this.loading = false;
      }
    });
  }

  aplicarRango(): void {
    this.usarRangoPersonalizado = true;
    this.cargarEstadisticas();
  }

  resetearRango(): void {
    this.usarRangoPersonalizado = false;
    this.inicializarFechas();
    this.cargarEstadisticas();
  }

  calcularAlturaBarra(valor: number): number {
    if (!this.stats || !this.stats.citasPorDia || this.stats.citasPorDia.length === 0) {
      return 5; 
    }


    const maxCitas = Math.max(...this.stats.citasPorDia.map(d => d.numeroCitas));
    
    console.log('Max citas:', maxCitas, 'Valor actual:', valor); 
    
    if (maxCitas === 0) {
      return 5; 
    }


    const porcentaje = (valor / maxCitas) * 80;
    

    return Math.max(porcentaje, 5);
  }

  cerrarNotificacion(index: number): void {
    this.notificacionesActivas.splice(index, 1);
  }

  getNotificacionClass(color: string): string {
    switch(color) {
      case 'ROJO': return 'notificacion-rojo';
      case 'VERDE': return 'notificacion-verde';
      case 'AMARILLO': return 'notificacion-amarillo';
      case 'AZUL': return 'notificacion-azul';
      default: return '';
    }
  }

  cerrarSesion(): void {
    localStorage.removeItem('admin');
    this.router.navigate(['/admin/login']);
  }
}


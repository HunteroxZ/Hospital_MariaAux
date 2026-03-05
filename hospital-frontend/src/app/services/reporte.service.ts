
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ReporteService {
  private apiUrl = 'http://localhost:8080/reportes';

  constructor(private http: HttpClient) {}


  obtenerReporteCitas(fechaInicio: string, fechaFin: string, idEspecialidad?: number, idMedico?: number, estado?: string): Observable<any[]> {
    let params = new HttpParams()
      .set('fechaInicio', fechaInicio)
      .set('fechaFin', fechaFin);
    
    if (idEspecialidad) params = params.set('idEspecialidad', idEspecialidad.toString());
    if (idMedico) params = params.set('idMedico', idMedico.toString());
    if (estado) params = params.set('estado', estado);
    
    return this.http.get<any[]>(`${this.apiUrl}/citas`, { params });
  }

  descargarReporteCitasPDF(fechaInicio: string, fechaFin: string, idEspecialidad?: number, idMedico?: number, estado?: string): Observable<Blob> {
    let params = new HttpParams()
      .set('fechaInicio', fechaInicio)
      .set('fechaFin', fechaFin);
    
    if (idEspecialidad) params = params.set('idEspecialidad', idEspecialidad.toString());
    if (idMedico) params = params.set('idMedico', idMedico.toString());
    if (estado) params = params.set('estado', estado);
    
    return this.http.get(`${this.apiUrl}/citas/pdf`, { params, responseType: 'blob' });
  }


  obtenerReporteIngresos(fechaInicio: string, fechaFin: string): Observable<any> {
    const params = new HttpParams()
      .set('fechaInicio', fechaInicio)
      .set('fechaFin', fechaFin);
    
    return this.http.get<any>(`${this.apiUrl}/ingresos`, { params });
  }

  descargarReporteIngresosPDF(fechaInicio: string, fechaFin: string): Observable<Blob> {
    const params = new HttpParams()
      .set('fechaInicio', fechaInicio)
      .set('fechaFin', fechaFin);
    
    return this.http.get(`${this.apiUrl}/ingresos/pdf`, { params, responseType: 'blob' });
  }


  obtenerReportePacientes(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/pacientes`);
  }

  descargarReportePacientesPDF(): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/pacientes/pdf`, { responseType: 'blob' });
  }


  obtenerReporteMedicos(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/medicos`);
  }

  descargarReporteMedicosPDF(): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/medicos/pdf`, { responseType: 'blob' });
  }
}
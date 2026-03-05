import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PacienteInfo } from '../models/paciente-info.model';

@Injectable({
  providedIn: 'root'
})
export class PacienteAdminService {
  private apiUrl = 'http://localhost:8080';

  constructor(private http: HttpClient) {}


  obtenerPacientes(dni?: string): Observable<PacienteInfo[]> {
    let params = new HttpParams();
    
    if (dni && dni.trim()) {
      params = params.set('dni', dni.trim());
    }
    
    return this.http.get<PacienteInfo[]>(`${this.apiUrl}/pacientes/info`, { params });
  }

  obtenerPacientePorId(id: number): Observable<PacienteInfo> {
    return this.http.get<PacienteInfo>(`${this.apiUrl}/pacientes/${id}/info`);
  }


  obtenerPacienteCompleto(id: number): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/${id}`);
  }


  actualizarPaciente(id: number, datos: any): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/${id}`, datos);
  }


  eliminarPaciente(id: number): Observable<any> {
    return this.http.delete<any>(`${this.apiUrl}/${id}`);
  }
}

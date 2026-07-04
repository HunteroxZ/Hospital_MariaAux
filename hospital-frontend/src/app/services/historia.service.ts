
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { HistoriaMedicaDTO, RegistrarHistoriaRequest } from '../models/historia-medica';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class HistoriaService {
  private apiUrl = environment.apiUrl + '/historiales';

  constructor(private http: HttpClient) {}

  registrarHistoria(request: RegistrarHistoriaRequest): Observable<any> {
    return this.http.post(`${this.apiUrl}`, request);
  }


  obtenerHistorialPorPaciente(idPaciente: number): Observable<HistoriaMedicaDTO[]> {
    return this.http.get<HistoriaMedicaDTO[]>(`${this.apiUrl}/paciente/${idPaciente}`);
  }


  buscarHistorialPorDniYMedico(idMedico: number, dni: string): Observable<HistoriaMedicaDTO[]> {
    const params = new HttpParams().set('dni', dni);
    return this.http.get<HistoriaMedicaDTO[]>(`${this.apiUrl}/medico/${idMedico}/buscar`, { params });
  }
}
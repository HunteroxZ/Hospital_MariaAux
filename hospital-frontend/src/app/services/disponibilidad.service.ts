
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface DisponibilidadMedico {
  idDisponibilidad?: number;
  idMedico: number;
  idEspecialidad: number;
  diaSemana: 'lunes' | 'martes' | 'miercoles' | 'jueves' | 'viernes' | 'sabado';
  horaInicio: string;
  horaFin: string;
}

export interface RegistrarDisponibilidadRequest {
  idEspecialidad: number;
  diaSemana: 'lunes' | 'martes' | 'miercoles' | 'jueves' | 'viernes' | 'sabado';
  horaInicio: string;
  horaFin: string;
}

@Injectable({
  providedIn: 'root'
})
export class DisponibilidadService {
  private apiUrl = environment.apiUrl + '/medicos';

  constructor(private http: HttpClient) {}


  obtenerHorarios(idMedico: number): Observable<DisponibilidadMedico[]> {
    return this.http.get<DisponibilidadMedico[]>(`${this.apiUrl}/${idMedico}/disponibilidad`);
  }

  anadirHorario(idMedico: number, request: RegistrarDisponibilidadRequest): Observable<DisponibilidadMedico> {
    return this.http.post<DisponibilidadMedico>(`${this.apiUrl}/${idMedico}/disponibilidad`, request);
  }

  eliminarHorario(idMedico: number, idDisponibilidad: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${idMedico}/disponibilidad/${idDisponibilidad}`);
  }
}

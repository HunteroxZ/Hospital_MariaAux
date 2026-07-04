
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Medico, RegistrarMedicoRequest, ActualizarMedicoRequest, Especialidad } from '../models/medico.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class MedicoService {
  private apiUrl = environment.apiUrl + '/medicos';

  constructor(private http: HttpClient) {}

  obtenerTodos(): Observable<Medico[]> {
    return this.http.get<Medico[]>(this.apiUrl);
  }

  obtenerPorId(id: number): Observable<Medico> {
    return this.http.get<Medico>(`${this.apiUrl}/${id}`);
  }

  registrar(request: RegistrarMedicoRequest): Observable<Medico> {
    return this.http.post<Medico>(this.apiUrl, request);
  }

  actualizar(id: number, request: ActualizarMedicoRequest): Observable<Medico> {
    return this.http.put<Medico>(`${this.apiUrl}/${id}`, request);
  }

  eliminar(id: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${id}`);
  }

  asignarEspecialidad(idMedico: number, idEspecialidad: number): Observable<any> {
    return this.http.post(`${this.apiUrl}/${idMedico}/especialidades`, 
      { idEspecialidad });
  }

  obtenerEspecialidades(idMedico: number): Observable<Especialidad[]> {
    return this.http.get<Especialidad[]>(`${this.apiUrl}/${idMedico}/especialidades`);
  }

  quitarEspecialidad(idMedico: number, idEspecialidad: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${idMedico}/especialidades/${idEspecialidad}`);
  }
}
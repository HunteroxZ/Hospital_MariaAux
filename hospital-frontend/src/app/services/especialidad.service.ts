

import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Especialidad } from '../models/medico.model';

@Injectable({
  providedIn: 'root'
})
export class EspecialidadService {
  private apiUrl = 'http://localhost:8080/especialidades';

  constructor(private http: HttpClient) {}


  obtenerTodas(): Observable<Especialidad[]> {
    return this.http.get<Especialidad[]>(this.apiUrl);
  }

  obtenerPorId(id: number): Observable<Especialidad> {
    return this.http.get<Especialidad>(`${this.apiUrl}/${id}`);
  }
}

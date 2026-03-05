import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Administrador } from '../models/administrador.model';

@Injectable({
  providedIn: 'root'
})
export class AdministradorService {
  private apiUrl = 'http://localhost:8080/administrador';

  constructor(private http: HttpClient) {}

  login(credenciales: { dni: string; contrasena: string }): Observable<Administrador> {
    return this.http.post<Administrador>(`${this.apiUrl}/login`, credenciales);
  }

}


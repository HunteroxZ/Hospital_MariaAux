import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Administrador } from '../models/administrador.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class AdministradorService {
  private apiUrl = environment.apiUrl + '/administrador';

  constructor(private http: HttpClient) {}

  login(credenciales: { dni: string; contrasena: string }): Observable<Administrador> {
    return this.http.post<Administrador>(`${this.apiUrl}/login`, credenciales);
  }

}


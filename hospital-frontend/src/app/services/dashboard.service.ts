import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { DashboardStats } from '../models/dashboard.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class DashboardService {
  private apiUrl = environment.apiUrl + '/dashboard';

  constructor(private http: HttpClient) {}

  obtenerEstadisticas(): Observable<DashboardStats> {
    return this.http.get<DashboardStats>(`${this.apiUrl}/estadisticas`);
  }

  obtenerEstadisticasConRango(fechaInicio: string, fechaFin: string): Observable<DashboardStats> {
    const params = new HttpParams()
      .set('fechaInicio', fechaInicio)
      .set('fechaFin', fechaFin);
    
    return this.http.get<DashboardStats>(`${this.apiUrl}/estadisticas/rango`, { params });
  }
}

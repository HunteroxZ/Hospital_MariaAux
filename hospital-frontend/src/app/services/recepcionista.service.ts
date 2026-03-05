
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { 
  LoginRecepcionistaRequest, 
  LoginRecepcionistaResponse,
  CitaPacienteRecepcionista,
  PagoRequest,
  PagoResponse
} from '../models/recepcionista.model';

@Injectable({
  providedIn: 'root'
})
export class RecepcionistaService {
  private baseUrl = 'http://localhost:8080/recepcionista';

  constructor(private http: HttpClient) { }

  login(credentials: LoginRecepcionistaRequest): Observable<any> {
    return this.http.post(`${this.baseUrl}/login`, credentials);
  }

  buscarCitasPorDni(dni: string): Observable<CitaPacienteRecepcionista[]> {
    return this.http.get<CitaPacienteRecepcionista[]>(`${this.baseUrl}/buscar-cita`, {
      params: { dni }
    });
  }

  procesarPago(pago: PagoRequest): Observable<PagoResponse> {
    return this.http.post<PagoResponse>(`${this.baseUrl}/procesar-pago`, pago);
  }

  // Guardar datos de sesión
  guardarSesion(response: any): void {
    localStorage.setItem('recepcionistaToken', 'true');
    localStorage.setItem('recepcionistaData', JSON.stringify(response.recepcionista));
  }

  // Obtener datos de sesión
  obtenerSesion(): LoginRecepcionistaResponse | null {
    const data = localStorage.getItem('recepcionistaData');
    return data ? JSON.parse(data) : null;
  }

  // Verificar si está logueado
  estaLogueado(): boolean {
    return localStorage.getItem('recepcionistaToken') === 'true';
  }

  // Cerrar sesión
  cerrarSesion(): void {
    localStorage.removeItem('recepcionistaToken');
    localStorage.removeItem('recepcionistaData');
  }
}

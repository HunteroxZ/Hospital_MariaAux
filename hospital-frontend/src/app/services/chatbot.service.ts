import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ChatRequest, ChatResponse } from '../models/chatbot.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ChatbotService {

  private apiUrl = environment.apiUrl + '/chatbot';

  constructor(private http: HttpClient) {}

  enviarMensaje(mensaje: string, idPaciente: number): Observable<ChatResponse> {
    const request: ChatRequest = { mensaje, idPaciente };
    return this.http.post<ChatResponse>(`${this.apiUrl}/mensaje`, request);
  }

  limpiarConversacion(idPaciente: number): Observable<any> {
    return this.http.post(`${this.apiUrl}/limpiar/${idPaciente}`, {});
  }
}

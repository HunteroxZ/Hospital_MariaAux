export interface ChatRequest {
  mensaje: string;
  idPaciente: number;
}

export interface ChatResponse {
  respuesta: string;
  accion: string;
  datos: any;
}

export interface ChatMessage {
  texto: string;
  esBot: boolean;
  fecha: Date;
}

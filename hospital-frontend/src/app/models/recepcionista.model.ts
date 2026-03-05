// models/recepcionista.model.ts (Actualizado con SIS)

export interface LoginRecepcionistaRequest {
  dni: string;
  clave: string;
}

export interface LoginRecepcionistaResponse {
  idRecepcionista: number;
  nombres: string;
  apellidos: string;
  correo: string;
}

export interface CitaPacienteRecepcionista {
  idCita: number;
  nombrePaciente: string;
  dniPaciente: string;
  nombreMedico: string;
  especialidad: string;
  fecha: string;
  hora: string;
  motivoConsulta: string;
  estado: string;
  precio: number;
  tieneSIS?: boolean;
}

export interface PagoRequest {
  idCita: number;
  metodoPago: 'EFECTIVO' | 'TARJETA' | 'QR' | 'SIS';
  montoPagado: number;
  ruc?: string;
  razonSocial?: string;
  direccion?: string;
  numeroCuotas?: number;
  tipoTarjeta?: 'QR' | 'TARJETA';
  tieneSIS?: boolean;
}

export interface PagoResponse {
  exito: boolean;
  mensaje: string;
  numeroComprobante: string;
  fechaHoraPago: string;
  vuelto: number;
  esPagoSIS?: boolean;
}

export interface TarjetaSimulada {
  numeroTarjeta: string;
  tipo: 'VISA' | 'MAST' | 'AMEX';
}

export interface Cuota {
  numero: number;
  fechaVencimiento: string;
  importe: number;
  moneda: string;
}
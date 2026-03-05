
export interface DashboardStats {
  totalPacientes: number;
  totalMedicos: number;
  totalEspecialidades: number;
  totalCitasHoy: number;
  
  citasPendientes: number;
  citasConfirmadas: number;
  citasAtendidas: number;
  citasCanceladas: number;
  citasNoPresentadas: number;
  
  topEspecialidades: EspecialidadStats[];
  topMedicos: MedicoStats[];
  citasPorEstado: { [key: string]: number };
  citasPorDia: CitasPorDia[];
  ingresosEstimados: number;
  notificaciones: Notificacion[];
}

export interface EspecialidadStats {
  nombreEspecialidad: string;
  numeroCitas: number;
  ingresoTotal: number;
}

export interface MedicoStats {
  nombreCompleto: string;
  numeroCitas: number;
  especialidadPrincipal: string;
}

export interface CitasPorDia {
  fecha: string;
  numeroCitas: number;
}

export interface Notificacion {
  tipo: 'success' | 'warning' | 'error' | 'info';
  mensaje: string;
  color: 'VERDE' | 'ROJO' | 'AMARILLO' | 'AZUL';
}
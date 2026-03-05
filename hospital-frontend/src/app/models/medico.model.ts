
export interface Medico {
  idMedico?: number;
  nombres: string;
  apellidos: string;
  dni: string;
  codigoColegiatura: string;
  correo: string;
  clave?: string;
  direccion?: string;
  telefono?: string;
  fechaNacimiento?: string;
  estado: 'ACTIVO' | 'INACTIVO';
  edad?: number;
}

export interface RegistrarMedicoRequest {
  nombres: string;
  apellidos: string;
  dni: string;
  codigoColegiatura: string;
  correo: string;
  clave: string;
  direccion?: string;
  telefono?: string;
  fechaNacimiento?: string;
}

export interface ActualizarMedicoRequest {
  nombres?: string;
  apellidos?: string;
  correo?: string;
  clave?: string;
  direccion?: string;
  telefono?: string;
  fechaNacimiento?: string;
  estado?: 'ACTIVO' | 'INACTIVO';
}

export interface Especialidad {
  idEspecialidad: number;
  nombre: string;
  descripcion?: string;
  precio: number;
}
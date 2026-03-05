export interface HistoriaMedicaDTO {
  idHistoriaMedica: number;
  idCita: number;
  fechaConsulta: string;
  diagnostico: string;
  sintomas: string;
  nombreMedico: string;
  nombrePaciente?: string;
  especialidad: string;
  idEspecialidad: number;
  antecedentes?: string;
  antecedentesPersonales?: string;
  antecedentesFamiliares?: string;
  historiaEnfermedadActual?: string;
  historiaPsicosocial?: string;
}

export interface RegistrarHistoriaRequest {
  idPaciente: number;
  idCita: number;
  idMedico: number;
  sintomas: string;
  diagnostico: string;
  antecedentes?: string;
  antecedentesPersonales?: string;
  antecedentesFamiliares?: string;
  historiaEnfermedadActual?: string;
  historiaPsicosocial?: string;
}

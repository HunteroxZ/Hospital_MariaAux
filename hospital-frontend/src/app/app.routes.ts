// app.routes.ts (Actualizado)
import { Routes } from '@angular/router';

// Componentes Paciente
import { RegistroComponent } from './registro/registro.component';
import { LoginComponent } from './login/login.component';
import { PacienteDashboardComponent } from './paciente-dashboard/paciente-dashboard.component';
import { EditarPerfilComponent } from './editar-perfil/editar-perfil.component';
import { GenerarCitaComponent } from './generar-cita/generar-cita.component';
import { VerCitasComponent } from './ver-citas/ver-citas.component';

// Componentes Médico
import { MedicoLoginComponent } from './medico-login/medico-login.component';
import { MedicoDashboardComponent } from './medico-dashboard/medico-dashboard.component';
import { MedicoAgendaComponent } from './medico-agenda/medico-agenda.component';
import { MedicoPerfilComponent } from './medico-perfil/medico-perfil.component';
import { MedicoHistorialRegistroComponent } from './medico-historial-registro/medico-historial-registro.component';
import { MedicoHistorialBuscarComponent } from './medico-historial-buscar/medico-historial-buscar.component';

// Componentes Recepcionista
import { RecepcionistaLoginComponent } from './recepcionista/recepcionista-login/recepcionista-login.component';
import { RecepcionistaDashboardComponent } from './recepcionista/recepcionista-dashboard/recepcionista-dashboard.component';

// Componente Selección de Rol
import { SeleccionarRolComponent } from './seleccionar-rol/seleccionar-rol.component';

// Componente Administrador 
import { AdminLogin } from './admin/admin-login/admin-login';
import { Inicio } from './admin/inicio/inicio';
import { Medicos } from './admin/medicos/medicos';
import { Pacientes } from './admin/pacientes/pacientes';
import { Reportes } from './admin/reportes/reportes';

// Guardias
import { loginGuard } from './auth/login.guard';
import { medicoGuard } from './auth/medico-guard';
import { medicoLoginGuard } from './auth/medico-login-guard';
import { recepcionistaGuard } from './auth/recepcionista.guard';
import { recepcionistaLoginGuard } from './auth/recepcionistaLoginGuard';

export const routes: Routes = [
  // --- Ruta Principal: Selección de Rol ---
  { path: 'seleccionar-rol', component: SeleccionarRolComponent },

  // --- Rutas de Autenticación PACIENTE ---
  { path: 'login', component: LoginComponent, canActivate: [loginGuard] },
  { path: 'registro', component: RegistroComponent },

  // --- Rutas de Autenticación ADMINISTRADOR ---
  { path: 'admin/login', component: AdminLogin },
  { path: 'admin/dashboard/inicio', component: Inicio },
  { path: 'admin/dashboard/medicos', component: Medicos },
  { path: 'admin/dashboard/pacientes', component: Pacientes },
  { path: 'admin/dashboard/reportes', component: Reportes },

  // --- Rutas del Paciente (Protegidas) ---
  { path: 'dashboard', component: PacienteDashboardComponent },
  { path: 'editar-perfil', component: EditarPerfilComponent },
  { path: 'generar-cita', component: GenerarCitaComponent },
  { path: 'ver-citas', component: VerCitasComponent },

  // --- Rutas de Autenticación MÉDICO ---
  { path: 'medico/login', component: MedicoLoginComponent, canActivate: [medicoLoginGuard] },

  // --- Rutas del MÉDICO (Protegidas por MedicoGuard) ---
  { path: 'medico/dashboard', component: MedicoDashboardComponent, canActivate: [medicoGuard] },
  { path: 'medico/agenda', component: MedicoAgendaComponent, canActivate: [medicoGuard] },
  { path: 'medico/perfil', component: MedicoPerfilComponent, canActivate: [medicoGuard] },
  { path: 'medico/historial/cita/:idCita/:idPaciente/:idEspecialidad', component: MedicoHistorialRegistroComponent, canActivate: [medicoGuard] },
  { path: 'medico/historiales', component: MedicoHistorialBuscarComponent, canActivate: [medicoGuard] },

  // --- Rutas de RECEPCIONISTA ---
  { path: 'recepcionista/login', component: RecepcionistaLoginComponent, canActivate: [recepcionistaLoginGuard] },
  { path: 'recepcionista/dashboard', component: RecepcionistaDashboardComponent, canActivate: [recepcionistaGuard] },

  // --- Rutas por Defecto ---
  { path: '', redirectTo: '/seleccionar-rol', pathMatch: 'full' },
  { path: '**', redirectTo: '/seleccionar-rol' }
];

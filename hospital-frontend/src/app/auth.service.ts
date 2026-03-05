import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AuthService {


  private pacienteLogueado = new BehaviorSubject<any>(null);
  public paciente$ = this.pacienteLogueado.asObservable();

  constructor() {
    const paciente = localStorage.getItem('paciente');
    if (paciente) {
      this.pacienteLogueado.next(JSON.parse(paciente));
    }
  }


  login(paciente: any) {
    localStorage.setItem('paciente', JSON.stringify(paciente));
    this.pacienteLogueado.next(paciente);
  }


  logout() {
    localStorage.removeItem('paciente');
    this.pacienteLogueado.next(null);
  }

  getPacienteId(): number | null {
    const paciente = this.pacienteLogueado.getValue();
    return paciente ? paciente.idPaciente : null;
  }


  getPacienteData(): any {
     return this.pacienteLogueado.getValue();
  }
}
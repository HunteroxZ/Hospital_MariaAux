import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class MedicoAuthService {

  private readonly MEDICO_STORAGE_KEY = 'medico_session';

  private medicoData: any = null; 

  constructor() {

    const data = localStorage.getItem(this.MEDICO_STORAGE_KEY);
    if (data) {
      this.medicoData = JSON.parse(data);
    }
  }


  login(data: any): void {
    localStorage.setItem(this.MEDICO_STORAGE_KEY, JSON.stringify(data));
    this.medicoData = data; 
  }


  logout(): void {
    localStorage.removeItem(this.MEDICO_STORAGE_KEY);
    this.medicoData = null; 
  }


  getMedicoData(): any | null {
   
    return this.medicoData; 
  }


  getMedicoId(): number | null {
    const data = this.getMedicoData();
    
    return data ? data.idMedico : null; 
  }


  isLoggedIn(): boolean {
    return this.getMedicoId() !== null;
  }
}
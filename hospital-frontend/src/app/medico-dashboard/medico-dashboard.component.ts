import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';

import { MedicoAuthService } from '../auth/medico-auth.service';

@Component({
  selector: 'app-medico-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './medico-dashboard.component.html',
  styleUrl: './medico-dashboard.component.css'
})
export class MedicoDashboardComponent implements OnInit {

  nombreMedico = 'Doctor(a)'; 

  constructor(
    private router: Router,
    private medicoAuthService: MedicoAuthService 
  ) {}

  ngOnInit(): void {

    const medicoData = this.medicoAuthService.getMedicoData();
    if (medicoData) {

      this.nombreMedico = `Dr(a). ${medicoData.nombres} ${medicoData.apellidos}`;
    }

  }


  onLogoutMedico() {
    console.log("Cerrando sesión de médico...");

    this.medicoAuthService.logout();
    

    this.router.navigateByUrl('/medico/login'); 
  }
}
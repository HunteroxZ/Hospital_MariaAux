import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router'; 

@Component({
  selector: 'app-seleccionar-rol', 
  standalone: true, 
  imports: [CommonModule], 
  templateUrl: './seleccionar-rol.component.html', 
  styleUrl: './seleccionar-rol.component.css'
})
export class SeleccionarRolComponent {

  constructor(private router: Router) {}


  goToLogin(path: string): void {
    
    this.router.navigate([path]); 
  }
}
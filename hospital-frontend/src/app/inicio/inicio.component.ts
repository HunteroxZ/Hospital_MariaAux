import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

@Component({
  selector: 'app-inicio',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './inicio.component.html',
  styleUrl: './inicio.component.css'
})
export class InicioComponent {

  menuAbierto = false;

  constructor(private router: Router) {}

  irAAgendar(): void {
    this.router.navigate(['/seleccionar-rol']);
  }

  irASeccion(id: string): void {
    this.menuAbierto = false;
    const el = document.getElementById(id);
    if (el) {
      el.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }
  }

  alternarMenu(): void {
    this.menuAbierto = !this.menuAbierto;
  }
}

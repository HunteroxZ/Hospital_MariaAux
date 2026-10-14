import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterOutlet, NavigationEnd } from '@angular/router';
import { filter } from 'rxjs/operators';
import { ChatbotComponent } from './chatbot/chatbot.component';
import { AuthService } from './auth.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, ChatbotComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {
  title = 'sistema-gestion-hospitalaria';

  // El chatbot se muestra solo con sesión iniciada y fuera de la página de inicio.
  sesionIniciada = false;
  esPaginaInicio = true;

  constructor(private authService: AuthService, private router: Router) {
    this.actualizarEstadoSesion();
    // Paciente: reactivo ante login/logout.
    this.authService.paciente$.subscribe(() => this.actualizarEstadoSesion());
    // Demás roles y cambios de ruta: reevaluar en cada navegación.
    this.router.events
      .pipe(filter((e): e is NavigationEnd => e instanceof NavigationEnd))
      .subscribe((e) => this.actualizarEstadoSesion(e.urlAfterRedirects));
  }

  private actualizarEstadoSesion(url?: string): void {
    const rutaActual = (url ?? this.router.url).split('?')[0];
    this.esPaginaInicio = rutaActual === '/';
    this.sesionIniciada = !!(
      localStorage.getItem('paciente') ||
      localStorage.getItem('medico_session') ||
      localStorage.getItem('recepcionistaToken') ||
      localStorage.getItem('admin')
    );
  }
}
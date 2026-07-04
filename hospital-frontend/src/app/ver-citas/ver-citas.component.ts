import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common'; 
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../auth.service';
import { Router, RouterLink } from '@angular/router'; 
import { environment } from '../../environments/environment';

@Component({
  selector: 'app-ver-citas',
  standalone: true,
  imports: [CommonModule, RouterLink], 
  templateUrl: './ver-citas.component.html',
  styleUrl: './ver-citas.component.css'
})
export class VerCitasComponent implements OnInit {

  private apiUrl = environment.apiUrl + '/citas/paciente/';
  pacienteId: number | null = null;
  citas: any[] = []; 
  isLoading = false;
  errorMessage = '';

  constructor(
    private http: HttpClient,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.pacienteId = this.authService.getPacienteId();
    if (this.pacienteId) {
      this.cargarCitas();
    } else {
      // Si por alguna razón no hay ID, redirige al login
      alert('Error: Debes iniciar sesión para ver tus citas.');
      this.router.navigate(['/login']);
    }
  }

  cargarCitas(): void {
    if (!this.pacienteId) return; 

    this.isLoading = true; 
    this.errorMessage = ''; 
    this.citas = []; 

    this.http.get<any[]>(this.apiUrl + this.pacienteId).subscribe({
      next: (data) => {
        this.citas = data;
        this.isLoading = false; 
      },
      error: (err) => {

        if (err.status === 404) { 
          this.errorMessage = 'No se encontró información para este paciente (ID: ' + this.pacienteId + ').';
        } else { 
          this.errorMessage = 'Error al cargar las citas desde el servidor.';
        }
        console.error('Error fetching citas:', err); 
        this.isLoading = false; 
      }
    });
  }
  formatHora(hora: string): string {
    return hora ? hora.substring(0, 5) : ''; 
  }
}
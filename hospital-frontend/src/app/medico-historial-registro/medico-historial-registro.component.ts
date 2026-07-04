import { Component, OnInit } from '@angular/core';
import { CommonModule, DatePipe, NgIf } from '@angular/common'; 
import { Router, ActivatedRoute, RouterLink } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms'; 
import { MedicoAuthService } from '../auth/medico-auth.service';
import { environment } from '../../environments/environment';
import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';

@Component({
  selector: 'app-medico-historial-registro',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, DatePipe, NgIf], 
  templateUrl: './medico-historial-registro.component.html',
  styleUrl: './medico-historial-registro.component.css'
})
export class MedicoHistorialRegistroComponent implements OnInit {

  private apiUrlHistorial = environment.apiUrl + '/historiales'; 
  private apiUrlPaciente = environment.apiUrl + '/'; 
  
  idCita: number = 0;
  idPaciente: number = 0;
  idMedico: number | null = null;
  idEspecialidadActual: number = 0; 
  
  nombrePaciente: string = 'Cargando...'; 
  estadoCita: string = 'cargando'; 

  historialForm: FormGroup;
  historialCompleto: any[] = []; 
  historialFiltrado: any[] = []; 

  isLoading = true;
  isSubmitting = false;
  
  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private http: HttpClient,
    private fb: FormBuilder,
    private medicoAuthService: MedicoAuthService
  ) {
    this.historialForm = this.fb.group({
      sintomas: ['', Validators.required], 
      diagnostico: ['', Validators.required], 
      antecedentes: [''],
      antecedentesPersonales: [''],
      antecedentesFamiliares: [''],
      historiaEnfermedadActual: [''],
      historiaPsicosocial: ['']
    });
  }

  ngOnInit(): void {
    
    this.idCita = Number(this.route.snapshot.paramMap.get('idCita'));
    this.idPaciente = Number(this.route.snapshot.paramMap.get('idPaciente'));
    this.idEspecialidadActual = Number(this.route.snapshot.paramMap.get('idEspecialidad'));

    this.idMedico = this.medicoAuthService.getMedicoId();
    
    if (!this.idCita || !this.idPaciente || !this.idMedico || !this.idEspecialidadActual) {
        alert("Error crítico: Faltan IDs (cita, paciente, médico o especialidad). Redirigiendo.");
        this.router.navigate(['/medico/agenda']);
        return;
    }
    
    this.cargarDatosPagina();
  }

  async cargarDatosPagina() {
    try {
      this.isLoading = true;
      

      const pacienteData: any = await this.http.get<any>(`${this.apiUrlPaciente}${this.idPaciente}`).toPromise();
      this.nombrePaciente = `${pacienteData.nombres} ${pacienteData.apellidos}`;


      const historialData = await this.http.get<any[]>(`${this.apiUrlHistorial}/paciente/${this.idPaciente}`).toPromise();
      this.historialCompleto = historialData || [];

      this.historialFiltrado = this.historialCompleto.filter(h => h.idEspecialidad === this.idEspecialidadActual);

      const entradaActual = this.historialCompleto.find(h => h.idCita === this.idCita);
      
      if (entradaActual) {
          this.estadoCita = 'atendida';
      } else {
          this.estadoCita = 'pendiente'; 
          
          if (this.historialCompleto.length > 0) {
              this.autocompletarAntecedentes(this.historialCompleto[0]);
          }
      }

      this.isLoading = false;
    } catch (err: any) {
      console.error("Error al cargar datos:", err);
      this.isLoading = false;
    }
  }
  
  autocompletarAntecedentes(ultimaEntrada: any): void {
      this.historialForm.patchValue({
          antecedentes: ultimaEntrada.antecedentes || '',
          antecedentesPersonales: ultimaEntrada.antecedentesPersonales || '',
          antecedentesFamiliares: ultimaEntrada.antecedentesFamiliares || '',
          historiaEnfermedadActual: ultimaEntrada.historiaEnfermedadActual || '',
          historiaPsicosocial: ultimaEntrada.historiaPsicosocial || ''
      });
  }

  onSubmit(): void {
    if (this.historialForm.invalid) {
      this.historialForm.markAllAsTouched();
      alert("Por favor, rellene los campos obligatorios (Síntomas y Diagnóstico).");
      return;
    }
    this.isSubmitting = true;
    const requestBody = {
      idPaciente: this.idPaciente,
      idCita: this.idCita,
      idMedico: this.idMedico,
      ...this.historialForm.value
    };
    
    this.http.post(this.apiUrlHistorial, requestBody).subscribe({
      next: (response: any) => {
        alert(response.mensaje);
        this.isSubmitting = false;
        
        this.cargarDatosPagina(); 
      },
      error: (err) => {
        alert(`Error: ${err.error.error || 'Error'}`);
        this.isSubmitting = false;
      }
    });
  }
  
  volverAAgenda(): void {
    this.router.navigate(['/medico/agenda']);
  }


  imprimirHistorialPDF(entrada: any): void {
    const doc = new jsPDF();
    
    doc.setFontSize(20);
    doc.text("Resumen de Consulta Médica", 105, 20, { align: 'center' });
    doc.setFontSize(14);
    doc.text("Hospital María Auxiliadora", 105, 30, { align: 'center' });

    doc.setFontSize(12);
    doc.setFont('helvetica', 'bold');
    doc.text("PACIENTE:", 20, 50);
    doc.setFont('helvetica', 'normal');
    doc.text(this.nombrePaciente, 55, 50);
    doc.setFont('helvetica', 'bold');
    doc.text("FECHA:", 20, 60);
    doc.setFont('helvetica', 'normal');
    const fechaFormateada = new Date(entrada.fechaConsulta + 'T00:00:00').toLocaleDateString('es-ES', {
      day: '2-digit', month: '2-digit', year: 'numeric'
    });
    doc.text(fechaFormateada, 55, 60);
    doc.setFont('helvetica', 'bold');
    doc.text("MÉDICO:", 20, 70);
    doc.setFont('helvetica', 'normal');
    doc.text(entrada.nombreMedico, 55, 70);
    doc.setFont('helvetica', 'bold');
    doc.text("ESPECIALIDAD:", 20, 80);
    doc.setFont('helvetica', 'normal');
    doc.text(entrada.especialidad, 55, 80);

    doc.setLineWidth(0.5);
    doc.line(20, 85, 190, 85);


    const bodyData = [
      ['Síntomas Reportados', entrada.sintomas],
      ['Diagnóstico Médico', entrada.diagnostico]
    ];

    autoTable(doc, {
        startY: 95, 
        head: [['Concepto', 'Descripción']],
        body: bodyData,
        theme: 'striped',
        headStyles: { fillColor: [44, 62, 80], textColor: [255, 255, 255] },
        columnStyles: { 0: { fontStyle: 'bold', cellWidth: 50 }, 1: { cellWidth: 'auto' } }
    });

    doc.save(`Resumen_Cita_${this.nombrePaciente}_${entrada.fechaConsulta}.pdf`);
  }
}
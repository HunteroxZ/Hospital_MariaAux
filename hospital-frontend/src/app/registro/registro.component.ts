import { Component } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common'; 
import { Router, RouterLink } from '@angular/router'; 

@Component({
  selector: 'app-registro',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule, 
    RouterLink 
  ],
  templateUrl: './registro.component.html',
  styleUrl: './registro.component.css'
})
export class RegistroComponent {

  private apiUrl = 'http://localhost:8080/registro';
  public fechaMaxima: string;
  public registroForm: FormGroup;

  constructor(
    private http: HttpClient, 
    private router: Router,
    private fb: FormBuilder
  ) {

    const hoy = new Date();
    this.fechaMaxima = hoy.toISOString().split('T')[0];

    this.registroForm = this.fb.group({
      dni: ['', [
        Validators.required,
        Validators.pattern(/^[0-9]{8}$/)
      ]],
      nombres: ['', [
        Validators.required,
        Validators.pattern(/^[a-zA-ZáéíóúÁÉÍÓÚñÑ\s]+$/)
      ]],
      apellidos: ['', [
        Validators.required,
        Validators.pattern(/^[a-zA-ZáéíóúÁÉÍÓÚñÑ\s]+$/)
      ]],
      correo: [
        '',
        [
          Validators.required,
          Validators.email,
          Validators.pattern(/^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/)
        ]
      ],
      clave: ['', [
        Validators.required,
        Validators.minLength(6)
      ]],
      fechaNacimiento: [
        '',
        [
          Validators.required,
          this.validadorFechaMaxima()
        ]
      ],
      sexo: ['M', Validators.required],

      telefono: [
        '',
        [
          Validators.pattern(/^[0-9]{9}$/)
        ]
      ],
      direccion: [''],
      ruc: ['', [
        Validators.pattern(/^20[0-9]{9}$/)
      ]],
      talla: [null, [
        Validators.min(0.5),
        Validators.max(2.5),
        Validators.pattern(/^\d+(\.\d{1,2})?$/)
      ]],
      peso: [null, [
        Validators.min(1),
        Validators.max(300),
        Validators.pattern(/^\d+(\.\d{1})?$/)
      ]],
      estadoCivil: ['Soltero']
    });
  }

  get f() {
    return this.registroForm.controls;
  }

  campoInvalido(campo: string): boolean {
    const control = this.registroForm.get(campo);
    return !!(control && control.invalid && (control.dirty || control.touched));
  }

  obtenerError(campo: string, tipo: string): boolean {
    const control = this.registroForm.get(campo);
    return !!(control && control.hasError(tipo) && (control.dirty || control.touched));
  }

  onSubmit() {
    if (this.registroForm.invalid) {
      Object.values(this.registroForm.controls).forEach(ctrl => ctrl.markAsTouched());
      return;
    }

    this.http.post(this.apiUrl, this.registroForm.value).subscribe({
      next: (resp: any) => {
        alert('¡Paciente registrado con éxito!');
        this.router.navigate(['/login']);
      },
      error: err => alert('Error al registrar: ' + err.error.error)
    });
  }
  validadorFechaMaxima() {
    return (control: any) => {
      if (!control.value) return null;

      const fechaIngresada = new Date(control.value);
      const hoy = new Date();
      hoy.setHours(0, 0, 0, 0);


      if (fechaIngresada > hoy) {
        return { fechaFutura: true };
      }


      let edad = hoy.getFullYear() - fechaIngresada.getFullYear();
      const mes = hoy.getMonth() - fechaIngresada.getMonth();
      const dia = hoy.getDate() - fechaIngresada.getDate();


      if (mes < 0 || (mes === 0 && dia < 0)) {
        edad--;
      }


      if (edad < 18) {
        return { menorDeEdad: true };
      }

      return null;
    };
  }
  limitarDecimales(event: any, maxDecimales: number) {
    let valor = event.target.value;


    valor = valor.replace(/[^0-9.]/g, "");


    const partes = valor.split(".");
    if (partes.length > 2) {
      valor = partes[0] + "." + partes[1];
    }


    if (partes[1] && partes[1].length > maxDecimales) {
      valor = partes[0] + "." + partes[1].slice(0, maxDecimales);
    }

    event.target.value = valor;
    

    const controlName = event.target.getAttribute("formControlName");
    this.registroForm.get(controlName)?.setValue(valor, { emitEvent: false });
  }
}

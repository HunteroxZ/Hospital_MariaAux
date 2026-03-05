
import { Component, Input, Output, EventEmitter, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RecepcionistaService } from '../../services/recepcionista.service';
import { CitaPacienteRecepcionista, PagoRequest, PagoResponse, TarjetaSimulada, Cuota } from '../../models/recepcionista.model';

@Component({
  selector: 'app-menu-pago',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './menu-pago.component.html',
  styleUrl: './menu-pago.component.css'
})
export class MenuPagoComponent implements OnInit {
  @Input() cita!: CitaPacienteRecepcionista;
  @Output() cerrar = new EventEmitter<void>();
  @Output() pagoCompletado = new EventEmitter<void>();

  montoPagado: number = 0;
  vuelto: number = 0;
  ruc: string = '';
  razonSocial: string = '';
  direccion: string = '';
  
  mostrarModalIzipay: boolean = false;
  mostrarModalTarjeta: boolean = false;
  mostrarModalPasarTarjeta: boolean = false;
  mostrarModalConfirmacion: boolean = false;
  mostrarModalQR: boolean = false;
  mostrarQRGenerado: boolean = false;
  mostrarFallbackQR: boolean = false;
  
  montoPagoTarjeta: number = 0;
  numeroCuotas: number = 1;
  pagarEnCuotas: boolean = false;
  tipoMetodoPago: 'EFECTIVO' | 'TARJETA' | 'QR' = 'EFECTIVO';
  
  pagoResponse: PagoResponse | null = null;
  errorMessage: string = '';
  errorMessageTarjeta: string = '';
  errorMessageQR: string = '';
  
  qrImagePath: string = 'icons/qr.png';
  vueltoTarjetaCalculado: number = 0;
  

  tarjetaSimulada: TarjetaSimulada | null = null;
  
  cuotasGeneradas: Cuota[] = [];

  constructor(private recepcionistaService: RecepcionistaService) {}

  ngOnInit(): void {
    this.montoPagoTarjeta = this.cita.precio;
    this.montoPagado = this.cita.precio;
    this.calcularVuelto();
  }

  calcularVuelto(): void {
    if (this.montoPagado >= this.cita.precio) {
      this.vuelto = this.montoPagado - this.cita.precio;
    } else {
      this.vuelto = 0;
    }
  }

  calcularVueltoTarjeta(): void {
    if (this.montoPagoTarjeta >= this.cita.precio) {
      this.vueltoTarjetaCalculado = this.montoPagoTarjeta - this.cita.precio;
    } else {
      this.vueltoTarjetaCalculado = 0;
    }
  }

  calcularSubtotal(): number {
    return this.cita.precio / 1.18;
  }

  calcularIGV(): number {
    return this.cita.precio - this.calcularSubtotal();
  }

  validarRUC(): boolean {
    if (this.ruc) {
      if (this.ruc.length !== 11 || !this.ruc.startsWith('20')) {
        this.errorMessage = 'El RUC debe tener 11 dígitos y empezar con 20';
        return false;
      }
      if (!/^\d+$/.test(this.ruc)) {
        this.errorMessage = 'El RUC debe contener solo números';
        return false;
      }
    }
    return true;
  }

  procesarPagoEfectivo(): void {
    if (this.montoPagado < this.cita.precio) {
      this.errorMessage = 'El monto pagado es insuficiente';
      return;
    }

    if (this.ruc && !this.validarRUC()) {
      return;
    }

    if (this.ruc && (!this.razonSocial || !this.direccion)) {
      this.errorMessage = 'Complete todos los campos de facturación';
      return;
    }

    const pagoRequest: PagoRequest = {
      idCita: this.cita.idCita,
      metodoPago: 'EFECTIVO',
      montoPagado: this.montoPagado,
      ruc: this.ruc || undefined,
      razonSocial: this.razonSocial || undefined,
      direccion: this.direccion || undefined
    };

    this.recepcionistaService.procesarPago(pagoRequest).subscribe({
      next: (response) => {
        this.pagoResponse = response;
        this.tipoMetodoPago = 'EFECTIVO';
        this.mostrarModalConfirmacion = true;
      },
      error: (error) => {
        this.errorMessage = error.error?.error || 'Error al procesar el pago';
      }
    });
  }

  abrirModalIzipay(): void {
    this.mostrarModalIzipay = true;
  }

  cerrarModalIzipay(): void {
    this.mostrarModalIzipay = false;
  }

  seleccionarPagoTarjeta(): void {
    this.tipoMetodoPago = 'TARJETA';
    this.mostrarModalIzipay = false;
    this.mostrarModalTarjeta = true;
    this.montoPagoTarjeta = this.cita.precio;
    this.calcularVueltoTarjeta();
  }

  seleccionarPagoQR(): void {
    this.tipoMetodoPago = 'QR';
    this.mostrarModalIzipay = false;
    this.mostrarModalQR = true;
    this.montoPagoTarjeta = this.cita.precio;
    this.calcularVueltoTarjeta();
    this.mostrarFallbackQR = false;
  }

  continuarPagoTarjeta(): void {
    this.errorMessageTarjeta = '';
    
    if (this.montoPagoTarjeta < this.cita.precio) {
      this.errorMessageTarjeta = 'El monto debe ser igual o mayor al precio de la cita';
      return;
    }

    if (this.pagarEnCuotas && (this.numeroCuotas < 1 || this.numeroCuotas > 3)) {
      this.errorMessageTarjeta = 'El número de cuotas debe estar entre 1 y 3';
      return;
    }

    this.calcularVueltoTarjeta();
    
    this.generarTarjetaSimulada();
    
    if (this.pagarEnCuotas) {
      this.generarCuotas();
    }
    
    this.mostrarModalTarjeta = false;
    this.mostrarModalPasarTarjeta = true;
  }

  generarTarjetaSimulada(): void {
    const tipos: ('VISA' | 'MAST' | 'AMEX')[] = ['VISA', 'MAST', 'AMEX'];
    const tipoAleatorio = tipos[Math.floor(Math.random() * tipos.length)];
    const ultimosCuatro = Math.floor(1000 + Math.random() * 9000).toString();
    
    this.tarjetaSimulada = {
      numeroTarjeta: `****${ultimosCuatro}`,
      tipo: tipoAleatorio
    };
  }

  generarCuotas(): void {
    this.cuotasGeneradas = [];
    const importePorCuota = this.montoPagoTarjeta / this.numeroCuotas;
    const fechaBase = new Date();
    
    for (let i = 0; i < this.numeroCuotas; i++) {
      const fechaVencimiento = new Date(fechaBase);
      fechaVencimiento.setDate(fechaVencimiento.getDate() + (i + 1));
      
      this.cuotasGeneradas.push({
        numero: i + 1,
        fechaVencimiento: fechaVencimiento.toLocaleDateString('es-PE', {
          day: '2-digit',
          month: '2-digit',
          year: '2-digit'
        }),
        importe: importePorCuota,
        moneda: 'soles'
      });
    }
  }

  siguientePasarTarjeta(): void {
    this.mostrarModalPasarTarjeta = false;
    this.procesarPagoConTarjeta();
  }

  procesarPagoConTarjeta(): void {
    const pagoRequest: PagoRequest = {
      idCita: this.cita.idCita,
      metodoPago: 'TARJETA',
      montoPagado: this.montoPagoTarjeta,
      numeroCuotas: this.pagarEnCuotas ? this.numeroCuotas : 1,
      tipoTarjeta: 'TARJETA'
    };

    this.recepcionistaService.procesarPago(pagoRequest).subscribe({
      next: (response) => {
        this.pagoResponse = response;
        this.tipoMetodoPago = 'TARJETA';
        this.mostrarModalConfirmacion = true;
      },
      error: (error) => {
        this.errorMessage = error.error?.error || 'Error al procesar el pago';
      }
    });
  }

  generarQR(): void {
    this.errorMessageQR = '';
    
    if (this.montoPagoTarjeta < this.cita.precio) {
      this.errorMessageQR = 'El monto debe ser igual o mayor al precio de la cita';
      return;
    }
    
    this.calcularVueltoTarjeta();
    this.mostrarQRGenerado = true;
    this.mostrarFallbackQR = false;
  }

  continuarQR(): void {
    const pagoRequest: PagoRequest = {
      idCita: this.cita.idCita,
      metodoPago: 'TARJETA',
      montoPagado: this.montoPagoTarjeta,
      tipoTarjeta: 'QR'
    };

    this.recepcionistaService.procesarPago(pagoRequest).subscribe({
      next: (response) => {
        this.pagoResponse = response;
        this.tipoMetodoPago = 'QR';
        this.mostrarModalQR = false;
        this.mostrarModalConfirmacion = true;
      },
      error: (error) => {
        this.errorMessage = error.error?.error || 'Error al procesar el pago';
      }
    });
  }

  manejarErrorQR(event: Event): void {
    console.warn('Error al cargar imagen QR, mostrando fallback CSS');
    this.mostrarFallbackQR = true;
    const imgElement = event.target as HTMLImageElement;
    imgElement.style.display = 'none';
  }

  imprimirBoleta(): void {
    const ventana = window.open('', '_blank');
    if (!ventana) {
      console.error('No se pudo abrir ventana de impresión');
      return;
    }

    const contenido = this.generarHTMLBoleta();
    ventana.document.write(contenido);
    ventana.document.close();
    
    setTimeout(() => {
      ventana.print();
      ventana.close();
      this.pagoCompletado.emit();
      this.cerrarModal();
    }, 500);
  }

  generarHTMLBoleta(): string {
    const fecha = new Date();
    const fechaStr = fecha.toLocaleDateString('es-PE');
    const horaStr = fecha.toLocaleTimeString('es-PE');
    const tieneRUC = this.ruc && this.razonSocial;
    const tipoComprobante = tieneRUC ? 'FACTURA ELECTRÓNICA' : 'BOLETA DE VENTA ELECTRÓNICA';
    
    const subtotal = this.calcularSubtotal();
    const igv = this.calcularIGV();
    
    let montoPagadoFinal = 0;
    let metodoPagoStr = '';
    let infoAdicional = '';
    
    switch (this.tipoMetodoPago) {
      case 'TARJETA':
        montoPagadoFinal = this.cita.precio;
        metodoPagoStr = 'TARJETA';
        if (this.tarjetaSimulada) {
          infoAdicional = `
            <div style="background: #e8f4f8; padding: 10px; margin: 10px 0; border-left: 3px solid #2980b9;">
              <p><strong>Referencia Tarjeta:</strong> ${this.tarjetaSimulada.numeroTarjeta}</p>
              <p><strong>Tipo:</strong> ${this.tarjetaSimulada.tipo}</p>
              <p><strong>Monto:</strong> S/ ${this.cita.precio.toFixed(2)}</p>
            </div>
          `;
          
          if (this.pagarEnCuotas && this.cuotasGeneradas.length > 0) {
            infoAdicional += `
              <div style="margin-top: 15px;">
                <h4 style="margin: 10px 0; color: #2c3e50;">Detalle de Cuotas</h4>
                <table style="width: 100%; border-collapse: collapse; margin: 10px 0; font-size: 10pt;">
                  <thead>
                    <tr>
                      <th style="border: 1px solid #000; padding: 8px; background: #eee;">Cuota</th>
                      <th style="border: 1px solid #000; padding: 8px; background: #eee;">Fecha Vencimiento</th>
                      <th style="border: 1px solid #000; padding: 8px; background: #eee;">Importe</th>
                      <th style="border: 1px solid #000; padding: 8px; background: #eee;">Moneda</th>
                    </tr>
                  </thead>
                  <tbody>
                    ${this.cuotasGeneradas.map(cuota => `
                      <tr>
                        <td style="border: 1px solid #000; padding: 8px; text-align: center;">${cuota.numero}</td>
                        <td style="border: 1px solid #000; padding: 8px; text-align: center;">${cuota.fechaVencimiento}</td>
                        <td style="border: 1px solid #000; padding: 8px; text-align: right;">S/ ${cuota.importe.toFixed(2)}</td>
                        <td style="border: 1px solid #000; padding: 8px; text-align: center;">${cuota.moneda}</td>
                      </tr>
                    `).join('')}
                  </tbody>
                </table>
              </div>
            `;
          }
        }
        break;
      case 'QR':
        montoPagadoFinal = this.cita.precio;
        metodoPagoStr = 'TARJETA (QR)';
        break;
      default:
        montoPagadoFinal = this.montoPagado;
        metodoPagoStr = 'EFECTIVO';
    }

    return `
      <!DOCTYPE html>
      <html>
      <head>
        <title>${tipoComprobante}</title>
        <style>
          @media print {
            body { font-size: 12pt; }
            .no-print { display: none !important; }
          }
          
          body { 
            font-family: 'Arial', sans-serif; 
            padding: 15px; 
            max-width: 700px; 
            margin: 0 auto;
            color: #000;
            font-size: 11pt;
            line-height: 1.4;
          }
          
          .header { 
            text-align: center; 
            margin-bottom: 20px; 
            border-bottom: 2px solid #000; 
            padding-bottom: 15px; 
          }
          
          .header h2 { 
            margin: 5px 0; 
            font-size: 16pt;
          }
          
          .header h3 { 
            margin: 10px 0; 
            color: #d00;
            font-size: 14pt;
          }
          
          .info { 
            margin: 15px 0; 
            padding: 10px;
            background: #f5f5f5;
          }
          
          .info p { 
            margin: 5px 0; 
          }
          
          .metodo-pago { 
            margin: 15px 0; 
            padding: 10px;
            background: #e8f4f8;
            border-left: 3px solid #2980b9;
          }
          
          table { 
            width: 100%; 
            border-collapse: collapse; 
            margin: 20px 0;
            font-size: 10pt;
          }
          
          th, td { 
            border: 1px solid #000; 
            padding: 8px; 
            text-align: left; 
          }
          
          th { 
            background-color: #eee;
            font-weight: bold;
          }
          
          .totales { 
            margin-top: 20px; 
            padding: 15px;
            background: #f9f9f9;
            border: 1px solid #000;
          }
          
          .total-line {
            display: flex;
            justify-content: space-between;
            margin: 8px 0;
            padding-bottom: 8px;
            border-bottom: 1px dashed #999;
          }
          
          .total-final {
            font-size: 12pt;
            font-weight: bold;
            border-bottom: 2px solid #000;
            padding-top: 10px;
          }
          
          .vuelto-section {
            background: #d4edda;
            padding: 10px;
            margin-top: 15px;
            border: 1px solid #c3e6cb;
          }
          
          .footer { 
            text-align: center; 
            margin-top: 30px; 
            font-size: 9pt; 
            color: #666;
            padding-top: 15px;
            border-top: 1px solid #ccc;
          }
          
          .fecha-hora {
            text-align: right;
            font-size: 9pt;
            margin-bottom: 15px;
          }
        </style>
      </head>
      <body>
        <div class="fecha-hora">
          <strong>Fecha:</strong> ${fechaStr} &nbsp; <strong>Hora:</strong> ${horaStr}
        </div>
        
        <div class="header">
          <h2>HOSPITAL MARÍA AUXILIADORA</h2>
          <p>Av. Miguel Iglesias 968, San Juan de Miraflores 15801</p>
          <h3>${tipoComprobante}</h3>
          <p><strong>N°:</strong> ${this.pagoResponse?.numeroComprobante || 'N/A'}</p>
        </div>
        
        <div class="info">
          <p><strong>Paciente:</strong> ${this.cita.nombrePaciente}</p>
          <p><strong>DNI:</strong> ${this.cita.dniPaciente}</p>
          <p><strong>Médico:</strong> ${this.cita.nombreMedico}</p>
          <p><strong>Especialidad:</strong> ${this.cita.especialidad}</p>
          ${tieneRUC ? `
            <hr style="margin: 10px 0;">
            <p><strong>RUC:</strong> ${this.ruc}</p>
            <p><strong>Razón Social:</strong> ${this.razonSocial}</p>
            <p><strong>Dirección:</strong> ${this.direccion}</p>
          ` : ''}
        </div>
        
        <div class="metodo-pago">
          <p><strong>Método de Pago:</strong> ${metodoPagoStr}</p>
        </div>
        
        ${infoAdicional}
        
        <table>
          <thead>
            <tr>
              <th style="width: 50%;">Descripción</th>
              <th style="width: 15%;">Cantidad</th>
              <th style="width: 20%;">Precio Unit.</th>
              <th style="width: 15%;">Total</th>
            </tr>
          </thead>
          <tbody>
            <tr>
              <td>Consulta ${this.cita.especialidad}</td>
              <td>1</td>
              <td>S/ ${this.cita.precio.toFixed(2)}</td>
              <td>S/ ${this.cita.precio.toFixed(2)}</td>
            </tr>
          </tbody>
        </table>
        
        <div class="totales">
          <div class="total-line">
            <span>SUB TOTAL:</span>
            <span>S/ ${subtotal.toFixed(2)}</span>
          </div>
          
          <div class="total-line">
            <span>IGV (18%):</span>
            <span>S/ ${igv.toFixed(2)}</span>
          </div>
          
          <div class="total-line total-final">
            <span>TOTAL:</span>
            <span>S/ ${this.cita.precio.toFixed(2)}</span>
          </div>
          
          ${this.tipoMetodoPago === 'EFECTIVO' && this.vuelto > 0 ? `
            <div class="vuelto-section">
              <div class="total-line">
                <span>MONTO PAGADO:</span>
                <span>S/ ${montoPagadoFinal.toFixed(2)}</span>
              </div>
              <div class="total-line" style="font-weight: bold; color: #155724;">
                <span>VUELTO:</span>
                <span>S/ ${this.vuelto.toFixed(2)}</span>
              </div>
            </div>
          ` : ''}
        </div>
        
        <div class="footer">
          <p>¡Gracias por su visita!</p>
          <p>Documento generado electrónicamente - No requiere firma</p>
          <p>Conserve este comprobante</p>
        </div>
        
        <div class="no-print" style="text-align: center; margin-top: 20px; padding: 10px;">
          <button onclick="window.print()" style="padding: 10px 20px; background: #27ae60; color: white; border: none; border-radius: 5px; cursor: pointer;">
            Imprimir
          </button>
          <button onclick="window.close()" style="padding: 10px 20px; background: #e74c3c; color: white; border: none; border-radius: 5px; margin-left: 10px; cursor: pointer;">
            Cerrar
          </button>
        </div>
      </body>
      </html>
    `;
  }

  cerrarModal(): void {
    this.cerrar.emit();
  }
}

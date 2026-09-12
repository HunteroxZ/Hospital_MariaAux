import { Component, OnInit, OnDestroy, ElementRef, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../auth.service';
import { ChatbotService } from '../services/chatbot.service';
import { ChatMessage } from '../models/chatbot.model';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-chatbot',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './chatbot.component.html',
  styleUrl: './chatbot.component.css'
})
export class ChatbotComponent implements OnInit, OnDestroy {

  @ViewChild('chatMessages') chatMessagesRef!: ElementRef;
  @ViewChild('chatInput') chatInputRef!: ElementRef;

  abierto = false;
  mensajeInput = '';
  mensajes: ChatMessage[] = [];
  cargando = false;
  idPaciente: number | null = null;
  nombrePaciente = '';
  esPaciente = false;

  private pacienteSub!: Subscription;

  constructor(
    private authService: AuthService,
    private chatbotService: ChatbotService
  ) {}

  ngOnInit(): void {
    this.pacienteSub = this.authService.paciente$.subscribe(paciente => {
      const nuevoId = paciente ? paciente.idPaciente : null;
      if (nuevoId !== this.idPaciente) {
        const idAnterior = this.idPaciente;
        this.mensajes = [];
        this.mensajeInput = '';
        this.cargando = false;
        if (idAnterior) {
          this.chatbotService.limpiarConversacion(idAnterior).subscribe({
            next: () => {},
            error: () => {}
          });
        }
      }
      if (paciente) {
        const esNuevoUsuario = paciente.idPaciente !== this.idPaciente;
        this.idPaciente = paciente.idPaciente;
        this.nombrePaciente = paciente.nombres || '';
        this.esPaciente = true;
        if (esNuevoUsuario && this.abierto) {
          this.iniciarConversacion();
        }
      } else {
        this.idPaciente = null;
        this.nombrePaciente = '';
        this.esPaciente = false;
      }
    });
  }

  ngOnDestroy(): void {
    if (this.pacienteSub) {
      this.pacienteSub.unsubscribe();
    }
  }

  toggleChat(): void {
    this.abierto = !this.abierto;
    if (this.abierto) {
      if (this.mensajes.length === 0) {
        this.iniciarConversacion();
      } else {
        setTimeout(() => this.scrollAlFinal(), 50);
      }
    }
  }

  iniciarConversacion(): void {
    if (!this.idPaciente) {
      this.agregarMensaje('Debe iniciar sesion para usar el chatbot.', true);
      return;
    }

    this.cargando = true;
    this.chatbotService.enviarMensaje('iniciar', this.idPaciente).subscribe({
      next: (response) => {
        this.agregarMensaje(response.respuesta, true);
        this.cargando = false;
      },
      error: (error) => {
        this.agregarMensaje('Error al conectar con el chatbot. Intente de nuevo.', true);
        this.cargando = false;
      }
    });
  }

  enviarMensaje(): void {
    const texto = this.mensajeInput.trim();
    if (!texto || !this.idPaciente) return;

    this.agregarMensaje(texto, false);
    this.mensajeInput = '';
    this.cargando = true;

    this.chatbotService.enviarMensaje(texto, this.idPaciente).subscribe({
      next: (response) => {
        this.agregarMensaje(response.respuesta, true);
        this.cargando = false;
        setTimeout(() => {
          if (this.chatInputRef) {
            this.chatInputRef.nativeElement.focus();
          }
        }, 50);
      },
      error: (error) => {
        this.agregarMensaje('Error al procesar su mensaje. Intente de nuevo.', true);
        this.cargando = false;
        setTimeout(() => {
          if (this.chatInputRef) {
            this.chatInputRef.nativeElement.focus();
          }
        }, 50);
      }
    });
  }

  formatearTexto(texto: string): string {
    return texto.replace(/\n/g, '<br>');
  }

  private agregarMensaje(texto: string, esBot: boolean): void {
    this.mensajes.push({
      texto,
      esBot,
      fecha: new Date()
    });
    setTimeout(() => this.scrollAlFinal(), 50);
  }

  scrollAlFinal(): void {
    if (this.chatMessagesRef) {
      const el = this.chatMessagesRef.nativeElement;
      el.scrollTop = el.scrollHeight;
    }
  }

  onKeyPress(event: KeyboardEvent): void {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      this.enviarMensaje();
    }
  }
}

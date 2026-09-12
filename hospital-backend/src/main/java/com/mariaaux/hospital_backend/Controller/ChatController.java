package com.mariaaux.hospital_backend.Controller;

import com.mariaaux.hospital_backend.dto.ChatRequest;
import com.mariaaux.hospital_backend.dto.ChatResponse;
import com.mariaaux.hospital_backend.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/chatbot")
@CrossOrigin(origins = "*")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @PostMapping("/mensaje")
    public ResponseEntity<?> enviarMensaje(@RequestBody ChatRequest request) {
        try {
            ChatResponse response = chatService.procesarMensaje(request.getIdPaciente(), request.getMensaje());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/limpiar/{idPaciente}")
    public ResponseEntity<?> limpiarConversacion(@PathVariable Long idPaciente) {
        chatService.limpiarConversacion(idPaciente);
        return ResponseEntity.ok(Map.of("mensaje", "Conversación reiniciada"));
    }
}

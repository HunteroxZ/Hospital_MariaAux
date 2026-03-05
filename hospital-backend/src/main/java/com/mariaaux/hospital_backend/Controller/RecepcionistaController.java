package com.mariaaux.hospital_backend.Controller;

import com.mariaaux.hospital_backend.dto.*;
import com.mariaaux.hospital_backend.dto.Recepcionista.CitaPacienteRecepcionistaDTO;
import com.mariaaux.hospital_backend.dto.Recepcionista.LoginResponseRecepcionista;
import com.mariaaux.hospital_backend.dto.Recepcionista.PagoRequest;
import com.mariaaux.hospital_backend.dto.Recepcionista.PagoResponse;
import com.mariaaux.hospital_backend.dto.Recepcionista.RegistrarRecepcionistaRequest;
import com.mariaaux.hospital_backend.model.Recepcionista;
import com.mariaaux.hospital_backend.service.RecepcionistaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/recepcionista")
@CrossOrigin(origins = "*")
public class RecepcionistaController {

    @Autowired
    private RecepcionistaService recepcionistaService;


    @PostMapping("/login")
    public ResponseEntity<?> loginRecepcionista(@RequestBody LoginRequest loginRequest) {
        try {
            LoginResponseRecepcionista response = recepcionistaService.loginRecepcionista(
                loginRequest.getDni(), 
                loginRequest.getClave()
            );
            
            return ResponseEntity.ok(Map.of(
                "mensaje", "Login de recepcionista exitoso",
                "recepcionista", response 
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error interno al iniciar sesión del recepcionista."));
        }
    }

    @PostMapping("/registrar")
    public ResponseEntity<?> registrarRecepcionista(@Valid @RequestBody RegistrarRecepcionistaRequest request) {
        try {
            Recepcionista nuevo = recepcionistaService.registrarRecepcionista(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevo);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error interno al registrar el recepcionista."));
        }
    }

    @GetMapping("/buscar-cita")
    public ResponseEntity<?> buscarCitasPorDni(@RequestParam String dni) {
        try {
            List<CitaPacienteRecepcionistaDTO> citas = recepcionistaService.buscarCitasPendientesPorDni(dni);
            return ResponseEntity.ok(citas);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error interno al buscar citas."));
        }
    }


    @PostMapping("/procesar-pago")
    public ResponseEntity<?> procesarPago(@Valid @RequestBody PagoRequest request) {
        try {
            PagoResponse response = recepcionistaService.procesarPago(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error interno al procesar el pago."));
        }
    }
}

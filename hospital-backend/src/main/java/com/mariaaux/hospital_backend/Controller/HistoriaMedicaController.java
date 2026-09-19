package com.mariaaux.hospital_backend.Controller;

import com.mariaaux.hospital_backend.dto.RegistrarHistoriaRequest;
import com.mariaaux.hospital_backend.dto.HistoriaMedicaDTO;
import com.mariaaux.hospital_backend.service.HistoriaMedicaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/historiales")
@CrossOrigin(origins = "*")
public class HistoriaMedicaController {

    @Autowired
    private HistoriaMedicaService historialService;

    private static String mensajeError(RuntimeException e) {
        if (e instanceof org.springframework.web.server.ResponseStatusException rse) {
            String razon = rse.getReason();
            if (razon != null && !razon.isBlank()) {
                return razon;
            }
        }
        String msg = e.getMessage() != null ? e.getMessage() : "Error en la operación.";
        return msg.replaceAll("^[0-9]{3} [A-Z_ ]+ \"|\"$", "");
    }
    
    @PostMapping
    public ResponseEntity<?> registrarNuevaEntrada(@Valid @RequestBody RegistrarHistoriaRequest request) {
        try {
            historialService.registrarNuevaEntrada(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "mensaje", "Historial registrado y cita marcada como atendida."
            ));
        } catch (RuntimeException e) {
            HttpStatus status = (e instanceof org.springframework.web.server.ResponseStatusException) ? 
                                 (HttpStatus)((org.springframework.web.server.ResponseStatusException) e).getStatusCode() : 
                                 HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status).body(Map.of("error", mensajeError(e)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Error interno al registrar el historial."));
        }
    }

    @GetMapping("/paciente/{idPaciente}")
    public ResponseEntity<?> obtenerHistorialPorPaciente(@PathVariable Long idPaciente) {
        try {
            List<HistoriaMedicaDTO> historial = historialService.obtenerHistorialPorPaciente(idPaciente);
            return ResponseEntity.ok(historial);
        } catch (RuntimeException e) {
             HttpStatus status = (e instanceof org.springframework.web.server.ResponseStatusException) ? 
                                  (HttpStatus)((org.springframework.web.server.ResponseStatusException) e).getStatusCode() : 
                                  HttpStatus.NOT_FOUND;
             return ResponseEntity.status(status).body(Map.of("error", mensajeError(e)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Error interno al obtener el historial."));
        }
    }

    @GetMapping("/medico/{idMedico}/buscar")
    public ResponseEntity<?> buscarHistorialPorDniYMedico(
            @PathVariable Long idMedico,
            @RequestParam String dni) {
        try {
            List<HistoriaMedicaDTO> historial = historialService.obtenerHistorialPorDniYMedico(dni, idMedico);
            return ResponseEntity.ok(historial);
        } catch (RuntimeException e) {
            HttpStatus status = (e instanceof org.springframework.web.server.ResponseStatusException) ? 
                                 (HttpStatus)((org.springframework.web.server.ResponseStatusException) e).getStatusCode() : 
                                 HttpStatus.NOT_FOUND;
            return ResponseEntity.status(status).body(Map.of("error", mensajeError(e)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(Map.of("error", "Error interno al buscar el historial."));
        }
    }
}
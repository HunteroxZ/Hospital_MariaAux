package com.mariaaux.hospital_backend.Controller;

import com.mariaaux.hospital_backend.dto.RegistrarDisponibilidadRequest;
import com.mariaaux.hospital_backend.model.DisponibilidadMedico;
import com.mariaaux.hospital_backend.service.DisponibilidadMedicoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/medicos/{idMedico}/disponibilidad")
@CrossOrigin(origins = "*")
public class DisponibilidadMedicoController {

    @Autowired
    private DisponibilidadMedicoService disponibilidadService;

    /**
     * 
     * @param idMedico 
     * @param request 
     * @return 
     */
    @PostMapping
    public ResponseEntity<?> anadirHorario(
            @PathVariable Long idMedico,
            @Valid @RequestBody RegistrarDisponibilidadRequest request) {
        try {
            DisponibilidadMedico nueva = disponibilidadService.anadirDisponibilidad(idMedico, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(nueva);
        } catch (RuntimeException e) {
            HttpStatus status = e.getMessage().contains("no encontrado") ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Error interno al añadir disponibilidad."));
        }
    }

    /**
     * 
     * @param idMedico
     * @return 
     */
    @GetMapping
    public ResponseEntity<?> obtenerHorarios(@PathVariable Long idMedico) {
        try {
            List<DisponibilidadMedico> horarios = disponibilidadService.obtenerDisponibilidadPorMedico(idMedico);
            return ResponseEntity.ok(horarios);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Error interno al obtener disponibilidad."));
        }
    }

    /**
     * 
      @param idMedico
      @param idDisponibilidad
      @return 
     */
    @DeleteMapping("/{idDisponibilidad}")
    public ResponseEntity<?> eliminarHorario(
            @PathVariable Long idMedico,
            @PathVariable Long idDisponibilidad) {
        try {
            disponibilidadService.eliminarDisponibilidad(idDisponibilidad);
            return ResponseEntity.ok(Map.of("mensaje", "Bloque de disponibilidad eliminado correctamente."));
        } catch (RuntimeException e) { 
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Error interno al eliminar disponibilidad."));
        }
    }
}
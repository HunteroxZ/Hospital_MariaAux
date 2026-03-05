package com.mariaaux.hospital_backend.Controller;

import com.mariaaux.hospital_backend.dto.CitaDetalleDTO;
import com.mariaaux.hospital_backend.dto.RegistrarCitaRequest;
import com.mariaaux.hospital_backend.model.Cita;
import com.mariaaux.hospital_backend.service.CitaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/citas") 
@CrossOrigin(origins = "*")
public class CitaController {

    @Autowired
    private CitaService citaService;

    @PostMapping
    public ResponseEntity<?> registrarNuevaCita(@Valid @RequestBody RegistrarCitaRequest request) {
        try {
            Cita nuevaCita = citaService.registrarCita(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "mensaje", "Cita registrada exitosamente",
                "idCita", nuevaCita.getIdCita()
            ));
        } catch (RuntimeException e) {
            
            HttpStatus status = (e instanceof org.springframework.web.server.ResponseStatusException) ? 
                                 (HttpStatus)((org.springframework.web.server.ResponseStatusException) e).getStatusCode() : 
                                 HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Error interno al registrar la cita."));
        }
    }

    @GetMapping("/paciente/{idPaciente}")
    public ResponseEntity<?> obtenerCitasDePaciente(@PathVariable Long idPaciente) {
        try {
            List<CitaDetalleDTO> citas = citaService.obtenerCitasPorPaciente(idPaciente);
            return ResponseEntity.ok(citas);
        } catch (RuntimeException e) {
           
            HttpStatus status = (e instanceof org.springframework.web.server.ResponseStatusException) ? 
                                 (HttpStatus)((org.springframework.web.server.ResponseStatusException) e).getStatusCode() : 
                                 HttpStatus.NOT_FOUND;
            return ResponseEntity.status(status).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Error interno al obtener las citas del paciente."));
        }
    }

    @PutMapping("/{idCita}/atendida")
    public ResponseEntity<?> marcarComoAtendida(@PathVariable Long idCita) {
        try {
            Cita citaActualizada = citaService.marcarCitaComoAtendida(idCita);
            return ResponseEntity.ok(Map.of(
                "mensaje", "Cita marcada como atendida",
                "idCita", citaActualizada.getIdCita(),
                "estado", citaActualizada.getEstado()
            ));
        } catch (RuntimeException e) {
            HttpStatus status = (e instanceof org.springframework.web.server.ResponseStatusException) ? 
                                 (HttpStatus)((org.springframework.web.server.ResponseStatusException) e).getStatusCode() : 
                                 HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(Map.of("error", "Error interno al marcar cita como atendida."));
        }
    }

    @PutMapping("/{idCita}/no-presentado")
    public ResponseEntity<?> marcarComoNoPresentado(@PathVariable Long idCita) {
        try {
            Cita citaActualizada = citaService.marcarCitaComoNoPresentada(idCita);
            return ResponseEntity.ok(Map.of(
                "mensaje", "Cita marcada como no presentado",
                "idCita", citaActualizada.getIdCita(),
                "estado", citaActualizada.getEstado()
            ));
        } catch (RuntimeException e) {
            HttpStatus status = (e instanceof org.springframework.web.server.ResponseStatusException) ? 
                                 (HttpStatus)((org.springframework.web.server.ResponseStatusException) e).getStatusCode() : 
                                 HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(Map.of("error", "Error interno al marcar cita como no presentado."));
        }
    }

    @GetMapping("/reservadas/{idMedico}/{fecha}")
    public ResponseEntity<?> obtenerHorasReservadas(
            @PathVariable Long idMedico,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        try {
            List<LocalTime> horasReservadas = citaService.obtenerHorasReservadasPorMedicoYFecha(idMedico, fecha);
            return ResponseEntity.ok(horasReservadas);
        } catch (RuntimeException e) {
            HttpStatus status = (e instanceof org.springframework.web.server.ResponseStatusException) ? 
                                 (HttpStatus)((org.springframework.web.server.ResponseStatusException) e).getStatusCode() : 
                                 HttpStatus.NOT_FOUND;
            return ResponseEntity.status(status).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
      
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(Map.of("error", "Error interno al obtener horas reservadas."));
        }
    }
}
package com.mariaaux.hospital_backend.Controller;

import com.mariaaux.hospital_backend.dto.LoginRequest;
import com.mariaaux.hospital_backend.dto.LoginResponse;
import com.mariaaux.hospital_backend.dto.PacienteUpdateRequest;
import com.mariaaux.hospital_backend.model.Paciente;
import com.mariaaux.hospital_backend.service.PacienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import com.mariaaux.hospital_backend.dto.PacienteInfoDTO;



import jakarta.validation.Valid;

@RestController
@CrossOrigin(origins = "*")
public class PacienteController {

    @Autowired
    private PacienteService pacienteService;

    @PostMapping("/registro")
   
    public ResponseEntity<?> registrarPaciente(@Valid @RequestBody Paciente paciente) {
        try {
            Paciente nuevoPaciente = pacienteService.registrarPaciente(paciente);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "mensaje", "Paciente registrado exitosamente",
                "idPaciente", nuevoPaciente.getIdPaciente()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }


    @PostMapping("/login")
    public ResponseEntity<?> loginPaciente(@RequestBody LoginRequest loginRequest) {
        try {
            LoginResponse response = pacienteService.loginPaciente(loginRequest.getDni(), loginRequest.getClave());
            return ResponseEntity.ok(Map.of(
                "mensaje", "Login exitoso",
                "paciente", response
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        }
    }


    @GetMapping("/{idPaciente}")
    public ResponseEntity<?> obtenerPaciente(@PathVariable Long idPaciente) {
        try {
            Paciente paciente = pacienteService.obtenerPaciente(idPaciente);
            return ResponseEntity.ok(paciente);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }


    @PutMapping("/{idPaciente}")
    public ResponseEntity<?> actualizarPaciente(@PathVariable Long idPaciente, @RequestBody PacienteUpdateRequest datos) {
        try {
            pacienteService.actualizarPaciente(idPaciente, datos);
            return ResponseEntity.ok(Map.of("mensaje", "Paciente actualizado correctamente"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{idPaciente}")
    public ResponseEntity<?> eliminarPaciente(@PathVariable Long idPaciente) {
        try {
            pacienteService.eliminarPaciente(idPaciente);
            return ResponseEntity.ok(Map.of("mensaje", "Paciente eliminado correctamente"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }
    /**
     * @param dni 
     */
    @GetMapping("/pacientes/info")
    public ResponseEntity<?> obtenerInfoPacientes(
            @RequestParam(required = false) String dni) {
        try {
            List<PacienteInfoDTO> pacientesInfo;
            
            
            if (dni != null && !dni.trim().isEmpty()) {
                pacientesInfo = pacienteService.buscarPacientesPorDni(dni.trim());
                
                return ResponseEntity.ok(pacientesInfo);
            } else {
                
                pacientesInfo = pacienteService.obtenerInfoTodosPacientes();
                return ResponseEntity.ok(pacientesInfo);
            }
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(Map.of("error", "Error al obtener información de pacientes: " + e.getMessage()));
        }
    }

    @GetMapping("/pacientes/{idPaciente}/info")
    public ResponseEntity<?> obtenerInfoPaciente(@PathVariable Long idPaciente) {
        try {
            PacienteInfoDTO pacienteInfo = pacienteService.obtenerInfoPaciente(idPaciente);
            return ResponseEntity.ok(pacienteInfo);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(Map.of("error", "Error al obtener información del paciente"));
        }
    }
}
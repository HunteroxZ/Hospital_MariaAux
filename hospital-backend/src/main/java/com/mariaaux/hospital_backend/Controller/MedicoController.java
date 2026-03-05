package com.mariaaux.hospital_backend.Controller;

import com.mariaaux.hospital_backend.dto.ActualizarMedicoRequest;
import com.mariaaux.hospital_backend.dto.CitaMedicoDTO;
import com.mariaaux.hospital_backend.dto.EspecialidadDTO;
import com.mariaaux.hospital_backend.dto.LoginRequest;

import com.mariaaux.hospital_backend.dto.LoginResponseMedico;
import com.mariaaux.hospital_backend.dto.RegistrarMedicoRequest;
import com.mariaaux.hospital_backend.model.Medico;
import com.mariaaux.hospital_backend.service.CitaService;
import com.mariaaux.hospital_backend.service.MedicoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/medicos")
@CrossOrigin(origins = "*")
public class MedicoController {

    @Autowired
    private MedicoService medicoService;

    @Autowired
    private CitaService citaService;

    @PostMapping("/login")
    public ResponseEntity<?> loginMedico(@RequestBody LoginRequest loginRequest) {
        try {
            LoginResponseMedico response = medicoService.loginMedico(loginRequest.getDni(), loginRequest.getClave());
            
            return ResponseEntity.ok(Map.of(
                "mensaje", "Login de médico exitoso",
                "medico", response 
            ));
        } catch (RuntimeException e) {
            
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Error interno al iniciar sesión del médico."));
        }
    }


    @GetMapping
    public ResponseEntity<List<Medico>> obtenerTodos() {
        try {
            List<Medico> medicos = medicoService.obtenerTodosLosMedicos();
            return ResponseEntity.ok(medicos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    public ResponseEntity<?> registrarNuevo(@Valid @RequestBody RegistrarMedicoRequest request) {
        try {
            Medico nuevoMedico = medicoService.registrarMedico(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevoMedico);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Error interno al registrar el médico."));
        }
    }

    @PostMapping("/{idMedico}/especialidades")
    public ResponseEntity<?> asignarEspecialidadAMedico(@PathVariable Long idMedico, @RequestBody Map<String, Long> requestBody) {
        Long idEspecialidad = requestBody.get("idEspecialidad");
        if (idEspecialidad == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Se requiere el campo 'idEspecialidad' en el cuerpo JSON."));
        }
        try {
            medicoService.asignarEspecialidad(idMedico, idEspecialidad);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("mensaje", "Especialidad asignada correctamente."));
        } catch (RuntimeException e) {
            HttpStatus status = e.getMessage().contains("no encontrado") ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Error interno al asignar especialidad."));
        }
    }

    @GetMapping("/{idMedico}/especialidades")
    public ResponseEntity<?> obtenerEspecialidadesDeMedico(@PathVariable Long idMedico) {
        try {
            List<EspecialidadDTO> especialidades = medicoService.obtenerEspecialidadesPorMedico(idMedico);
            return ResponseEntity.ok(especialidades);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Error interno al obtener especialidades."));
        }
    }

    @DeleteMapping("/{idMedico}/especialidades/{idEspecialidad}")
    public ResponseEntity<?> quitarEspecialidadDeMedico(@PathVariable Long idMedico, @PathVariable Long idEspecialidad) {
         try {
            medicoService.quitarEspecialidad(idMedico, idEspecialidad);
            return ResponseEntity.ok(Map.of("mensaje", "Especialidad quitada correctamente del médico."));
        } catch (RuntimeException e) {
             return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Error interno al quitar especialidad."));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(@PathVariable Long id) {
        Optional<Medico> optMedico = medicoService.obtenerMedicoPorId(id);
        if (optMedico.isPresent()) {
            return ResponseEntity.ok(optMedico.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                 .body(Map.of("error", "Médico no encontrado con ID: " + id));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @RequestBody ActualizarMedicoRequest request) {
         try {
            Medico actualizado = medicoService.actualizarMedico(id, request);
            return ResponseEntity.ok(actualizado);
        } catch (RuntimeException e) {
            HttpStatus status = e.getMessage().contains("no encontrado") ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Error interno al actualizar el médico."));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        try {
            medicoService.eliminarMedico(id);
            return ResponseEntity.ok(Map.of("mensaje", "Médico eliminado correctamente."));
        } catch (RuntimeException e) {
             return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Error interno al eliminar el médico."));
        }
    }

    @GetMapping("/{idMedico}/citas")
    public ResponseEntity<?> obtenerCitasDelMedicoPorFecha(
            @PathVariable Long idMedico,
            @RequestParam(required = true) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        try {
            List<CitaMedicoDTO> citas = citaService.obtenerCitasPorMedicoYFecha(idMedico, fecha);
            return ResponseEntity.ok(citas);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(Map.of("error", "Error interno al obtener las citas del médico."));
        }
    }
}
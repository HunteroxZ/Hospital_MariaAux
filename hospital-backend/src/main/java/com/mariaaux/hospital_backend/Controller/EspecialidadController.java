package com.mariaaux.hospital_backend.Controller;

import com.mariaaux.hospital_backend.dto.MedicoDTO; 
import com.mariaaux.hospital_backend.model.Especialidad;
import com.mariaaux.hospital_backend.service.EspecialidadService;
import com.mariaaux.hospital_backend.service.MedicoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/especialidades")
@CrossOrigin(origins = "*")
public class EspecialidadController {

    @Autowired
    private EspecialidadService especialidadService;

    @Autowired
    private MedicoService medicoService;

    @GetMapping
    public ResponseEntity<List<Especialidad>> obtenerTodas() {
        try {
            List<Especialidad> especialidades = especialidadService.obtenerTodasLasEspecialidades();
            return ResponseEntity.ok(especialidades);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    public ResponseEntity<?> crearNueva(@Valid @RequestBody Especialidad especialidad) {
        try {
            Especialidad nuevaEspecialidad = especialidadService.crearEspecialidad(especialidad);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevaEspecialidad);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Error interno al crear la especialidad."));
        }
    }
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(@PathVariable Long id) {
        Optional<Especialidad> optEspecialidad = especialidadService.obtenerEspecialidadPorId(id);
        if (optEspecialidad.isPresent()) {
            return ResponseEntity.ok(optEspecialidad.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                 .body(Map.of("error", "Especialidad no encontrada con ID: " + id));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @Valid @RequestBody Especialidad especialidad) {
         try {
            Especialidad actualizada = especialidadService.actualizarEspecialidad(id, especialidad);
            return ResponseEntity.ok(actualizada);
        } catch (RuntimeException e) {
            HttpStatus status = e.getMessage().contains("no encontrada") ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Error interno al actualizar la especialidad."));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        try {
            especialidadService.eliminarEspecialidad(id);
            return ResponseEntity.ok(Map.of("mensaje", "Especialidad eliminada correctamente"));
        } catch (RuntimeException e) {
             return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Error interno al eliminar la especialidad."));
        }
    }

    @GetMapping("/{idEspecialidad}/medicos")
    public ResponseEntity<?> obtenerMedicosDeEspecialidad(@PathVariable Long idEspecialidad) {
        try {
            List<MedicoDTO> medicosDTO = medicoService.obtenerMedicosPorEspecialidad(idEspecialidad);
            return ResponseEntity.ok(medicosDTO);
        } catch (RuntimeException e) {
             return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Error interno al obtener médicos por especialidad."));
        }
    }
}
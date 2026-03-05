package com.mariaaux.hospital_backend.Controller;

import com.mariaaux.hospital_backend.dto.AdministradorRequestDTO;
import com.mariaaux.hospital_backend.dto.AdministradorResponseDTO;

import com.mariaaux.hospital_backend.service.AdministradorService;
import com.mariaaux.hospital_backend.dto.CredencialesAdmin;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("administrador")
public class AdministradorController {

    @Autowired
    private AdministradorService administradorService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody CredencialesAdmin credenciales) {
        try {
            AdministradorResponseDTO administradorDTO = administradorService.login(credenciales);
            return ResponseEntity.ok(administradorDTO);
        } catch (EntityNotFoundException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciales inválidas");
        }
    }

    @PostMapping
    public ResponseEntity<AdministradorResponseDTO> crearAdministrador(@RequestBody AdministradorRequestDTO adminDTO) {
        AdministradorResponseDTO nuevoAdmin = administradorService.crearAdministrador(adminDTO);
        return new ResponseEntity<>(nuevoAdmin, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<AdministradorResponseDTO>> obtenerTodos() {
        List<AdministradorResponseDTO> administradores = administradorService.obtenerTodos();
        return ResponseEntity.ok(administradores);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AdministradorResponseDTO> actualizarAdministrador(@PathVariable Long id, @RequestBody AdministradorRequestDTO adminDTO) {
        try {
            AdministradorResponseDTO adminActualizado = administradorService.actualizarAdministrador(id, adminDTO);
            return ResponseEntity.ok(adminActualizado);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarAdministrador(@PathVariable Long id) {
        try {
            administradorService.eliminarAdministrador(id);
            return ResponseEntity.noContent().build(); 
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }


    @GetMapping("/{id}")
    public ResponseEntity<AdministradorResponseDTO> obtenerPorId(@PathVariable Long id) {
        AdministradorResponseDTO administrador = administradorService.obtenerPorId(id);
        if (administrador != null) {
            return ResponseEntity.ok(administrador);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}


package com.mariaaux.hospital_backend.service;

import com.mariaaux.hospital_backend.dto.AdministradorRequestDTO;
import com.mariaaux.hospital_backend.dto.AdministradorResponseDTO;
import com.mariaaux.hospital_backend.model.Administrador;
import com.mariaaux.hospital_backend.repository.AdministradorRepository;
import com.mariaaux.hospital_backend.dto.CredencialesAdmin;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdministradorService {

    @Autowired
    private AdministradorRepository administradorRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public AdministradorResponseDTO login(CredencialesAdmin credenciales) {

        Administrador admin = administradorRepository.findByDni(credenciales.getDni())
                .orElseThrow(() -> new EntityNotFoundException("Administrador no encontrado"));


        if (!passwordEncoder.matches(credenciales.getContrasena(), admin.getContrasena())) {
            throw new IllegalArgumentException("Credenciales inválidas");
        }

        return convertirADTO(admin);
    }

    public AdministradorResponseDTO crearAdministrador(AdministradorRequestDTO adminDTO) {
        Administrador administrador = new Administrador();
        administrador.setDni(adminDTO.getDni());
        

        String contrasenaEncriptada = passwordEncoder.encode(adminDTO.getContrasena());
        administrador.setContrasena(contrasenaEncriptada);
        
        administrador.setNombre(adminDTO.getNombre());
        administrador.setApellido(adminDTO.getApellido());
        administrador.setEmail(adminDTO.getEmail());

        Administrador nuevoAdmin = administradorRepository.save(administrador);
        return convertirADTO(nuevoAdmin);
    }

    public List<AdministradorResponseDTO> obtenerTodos() {
        return administradorRepository.findAll().stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    public AdministradorResponseDTO obtenerPorId(Long id) {
        return administradorRepository.findById(id)
                .map(this::convertirADTO)
                .orElse(null);
    }

    private AdministradorResponseDTO convertirADTO(Administrador administrador) {
        AdministradorResponseDTO dto = new AdministradorResponseDTO();
        dto.setId(administrador.getId());
        dto.setDni(administrador.getDni());
        dto.setNombre(administrador.getNombre());
        dto.setApellido(administrador.getApellido());
        dto.setEmail(administrador.getEmail());
        return dto;
    }

    public AdministradorResponseDTO actualizarAdministrador(Long id, AdministradorRequestDTO adminDTO) {
        Administrador administradorExistente = administradorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Administrador no encontrado con ID: " + id));

        administradorExistente.setDni(adminDTO.getDni());
        administradorExistente.setNombre(adminDTO.getNombre());
        administradorExistente.setApellido(adminDTO.getApellido());
        administradorExistente.setEmail(adminDTO.getEmail());
        

        if (adminDTO.getContrasena() != null && !adminDTO.getContrasena().isEmpty()) {
            String contrasenaEncriptada = passwordEncoder.encode(adminDTO.getContrasena());
            administradorExistente.setContrasena(contrasenaEncriptada);
        }
        
        Administrador adminActualizado = administradorRepository.save(administradorExistente);
        return convertirADTO(adminActualizado);
    }

    public void eliminarAdministrador(Long id) {
        if (!administradorRepository.existsById(id)) {
            throw new EntityNotFoundException("Administrador no encontrado con ID: " + id);
        }
        administradorRepository.deleteById(id);
    }
}


package com.mariaaux.hospital_backend.service;

import com.mariaaux.hospital_backend.dto.LoginResponse;
import com.mariaaux.hospital_backend.dto.PacienteUpdateRequest;
import com.mariaaux.hospital_backend.model.Paciente;
import com.mariaaux.hospital_backend.repository.PacienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.mariaaux.hospital_backend.dto.PacienteInfoDTO;
import com.mariaaux.hospital_backend.repository.CitaRepository;

import java.util.List;
import java.util.stream.Collectors;


@Service
public class PacienteService {

    @Autowired
    private CitaRepository citaRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public Paciente registrarPaciente(Paciente paciente) {
        

        if (paciente.getDni() == null || paciente.getNombres() == null || paciente.getApellidos() == null ||
            paciente.getSexo() == null || paciente.getCorreo() == null || paciente.getClave() == null ||
            paciente.getFechaNacimiento() == null) {
            throw new RuntimeException("Por favor completa todos los campos requeridos (dni, nombres, apellidos, sexo, correo, clave, fechaNacimiento)");
        }

  
        if (paciente.getDni().length() != 8 || !paciente.getDni().matches("[0-9]+")) {
            throw new RuntimeException("El DNI debe tener 8 caracteres numéricos.");
        }

    
        if (pacienteRepository.existsByDni(paciente.getDni())) {
            throw new RuntimeException("El DNI ya está registrado en el sistema");
        }
        if (pacienteRepository.existsByCorreo(paciente.getCorreo())) {
            throw new RuntimeException("El correo ya está registrado en el sistema");
        }
        


        String claveEncriptada = passwordEncoder.encode(paciente.getClave());
        paciente.setClave(claveEncriptada);
        return pacienteRepository.save(paciente);
    }

    @Transactional(readOnly = true)
    public LoginResponse loginPaciente(String dni, String clave) {

        Paciente paciente = pacienteRepository.findByDni(dni)
                .orElseThrow(() -> new RuntimeException("DNI o contraseña incorrectos"));

        if (!passwordEncoder.matches(clave, paciente.getClave())) {
            throw new RuntimeException("DNI o contraseña incorrectos");
        }

        return new LoginResponse(
            paciente.getIdPaciente(),
            paciente.getNombres(),
            paciente.getApellidos(),
            paciente.getCorreo(),
            paciente.getSexo().name()
        );
    }
    
    @Transactional(readOnly = true)
    public Paciente obtenerPaciente(Long idPaciente) {
        return pacienteRepository.findById(idPaciente)
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));
    }


    @Transactional
    public Paciente actualizarPaciente(Long idPaciente, PacienteUpdateRequest datos) {

        Paciente paciente = obtenerPaciente(idPaciente); 


        if (datos.getTelefono() != null) {
            paciente.setTelefono(datos.getTelefono());
        }
        if (datos.getDireccion() != null) {
            paciente.setDireccion(datos.getDireccion());
        }
        if (datos.getTalla() != null) {
            paciente.setTalla(datos.getTalla());
        }
        if (datos.getPeso() != null) {
            paciente.setPeso(datos.getPeso());
        }

        if (datos.getClave() != null && !datos.getClave().isEmpty()) {
            paciente.setClave(passwordEncoder.encode(datos.getClave()));
        }

        return pacienteRepository.save(paciente);
    }

    @Transactional
    public void eliminarPaciente(Long idPaciente) {
        if (!pacienteRepository.existsById(idPaciente)) {
            throw new RuntimeException("Paciente no encontrado");
        }
        pacienteRepository.deleteById(idPaciente);
    }


    @Transactional(readOnly = true)
    public List<PacienteInfoDTO> obtenerInfoTodosPacientes() {
        List<Paciente> pacientes = pacienteRepository.findAll();
        
        return pacientes.stream()
            .map(this::convertirAPacienteInfoDTO)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PacienteInfoDTO obtenerInfoPaciente(Long idPaciente) {
        Paciente paciente = pacienteRepository.findById(idPaciente)
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado con ID: " + idPaciente));
        
        return convertirAPacienteInfoDTO(paciente);
    }


    private PacienteInfoDTO convertirAPacienteInfoDTO(Paciente paciente) {
        String nombreCompleto = paciente.getNombres() + " " + paciente.getApellidos();
        

        Long numeroCitas = citaRepository.findByIdPacienteOrderByFechaDescHoraDesc(paciente.getIdPaciente())
                                        .stream()
                                        .count();
        
        return new PacienteInfoDTO(
            paciente.getIdPaciente(),
            nombreCompleto,
            paciente.getDni(),
            paciente.getCorreo(),
            paciente.getTelefono() != null ? paciente.getTelefono() : "Sin teléfono",
            numeroCitas
        );
    }
    /**
     * 
     * @param dni
     * @return 
     */
    @Transactional(readOnly = true)
    public List<PacienteInfoDTO> buscarPacientesPorDni(String dni) {
        List<Paciente> pacientes = pacienteRepository.findByDniStartingWith(dni);
        
        return pacientes.stream()
            .map(this::convertirAPacienteInfoDTO)
            .collect(Collectors.toList());
    }
}
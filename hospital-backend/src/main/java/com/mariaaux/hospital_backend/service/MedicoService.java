package com.mariaaux.hospital_backend.service;

import com.mariaaux.hospital_backend.dto.MedicoDTO;
import com.mariaaux.hospital_backend.dto.ActualizarMedicoRequest;
import com.mariaaux.hospital_backend.dto.EspecialidadDTO;
import com.mariaaux.hospital_backend.dto.LoginResponseMedico;
import com.mariaaux.hospital_backend.dto.RegistrarMedicoRequest;
import com.mariaaux.hospital_backend.model.Medico;
import com.mariaaux.hospital_backend.model.EstadoMedico;
import com.mariaaux.hospital_backend.model.Especialidad;
import com.mariaaux.hospital_backend.model.MedicoEspecialidad;
import com.mariaaux.hospital_backend.model.MedicoEspecialidadId;
import com.mariaaux.hospital_backend.repository.MedicoRepository;
import com.mariaaux.hospital_backend.repository.EspecialidadRepository;
import com.mariaaux.hospital_backend.repository.MedicoEspecialidadRepository;
import com.mariaaux.hospital_backend.repository.DisponibilidadMedicoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MedicoService {

    @Autowired
    private MedicoRepository medicoRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EspecialidadRepository especialidadRepository;

    @Autowired
    private MedicoEspecialidadRepository medicoEspecialidadRepository;
    
    @Autowired
    private DisponibilidadMedicoRepository disponibilidadMedicoRepository; 
    

    
    @Transactional(readOnly = true)
    public LoginResponseMedico loginMedico(String dni, String clave) {
        Medico medico = medicoRepository.findByDni(dni)
                .orElseThrow(() -> new RuntimeException("DNI o contraseña incorrectos"));

        if (!passwordEncoder.matches(clave, medico.getClave())) {
            throw new RuntimeException("DNI o contraseña incorrectos");
        }

        if (medico.getEstado() == EstadoMedico.INACTIVO) {
            throw new RuntimeException("El médico se encuentra inactivo. Contacte al administrador.");
        }

        return new LoginResponseMedico(
            medico.getIdMedico(),
            medico.getNombres(),
            medico.getApellidos(),
            medico.getCorreo()
        );
    }
    


    @Transactional(readOnly = true)
    public List<Medico> obtenerTodosLosMedicos() {
        return medicoRepository.findAll();
    }

    @Transactional
    public Medico registrarMedico(RegistrarMedicoRequest request) {
        if (medicoRepository.existsByDni(request.getDni())) {
            throw new RuntimeException("El DNI ya está registrado.");
        }
        if (medicoRepository.existsByCodigoColegiatura(request.getCodigoColegiatura())) {
            throw new RuntimeException("El Código de Colegiatura ya está registrado.");
        }
        if (medicoRepository.existsByCorreo(request.getCorreo())) {
            throw new RuntimeException("El Correo ya está registrado.");
        }

        Medico nuevoMedico = new Medico();
        nuevoMedico.setNombres(request.getNombres());
        nuevoMedico.setApellidos(request.getApellidos());
        nuevoMedico.setDni(request.getDni());
        nuevoMedico.setCodigoColegiatura(request.getCodigoColegiatura());
        nuevoMedico.setCorreo(request.getCorreo());
        nuevoMedico.setDireccion(request.getDireccion());
        nuevoMedico.setTelefono(request.getTelefono());
        nuevoMedico.setFechaNacimiento(request.getFechaNacimiento());
        nuevoMedico.setClave(passwordEncoder.encode(request.getClave()));
        nuevoMedico.setEstado(EstadoMedico.ACTIVO);

        return medicoRepository.save(nuevoMedico);
    }

    @Transactional
    public MedicoEspecialidad asignarEspecialidad(Long idMedico, Long idEspecialidad) {
        Medico medico = medicoRepository.findById(idMedico)
                .orElseThrow(() -> new RuntimeException("Médico no encontrado con ID: " + idMedico));
        Especialidad especialidad = especialidadRepository.findById(idEspecialidad)
                .orElseThrow(() -> new RuntimeException("Especialidad no encontrada con ID: " + idEspecialidad));

        MedicoEspecialidadId relacionId = new MedicoEspecialidadId(idMedico, idEspecialidad);
        if (medicoEspecialidadRepository.existsById(relacionId)) {
            throw new RuntimeException("El médico ya tiene asignada esa especialidad.");
        }

        MedicoEspecialidad nuevaRelacion = new MedicoEspecialidad(medico, especialidad);
        return medicoEspecialidadRepository.save(nuevaRelacion);
    }

    @Transactional(readOnly = true)
    public List<EspecialidadDTO> obtenerEspecialidadesPorMedico(Long idMedico) {
        if (!medicoRepository.existsById(idMedico)) {
            throw new RuntimeException("Médico no encontrado con ID: " + idMedico);
        }
        
        List<MedicoEspecialidad> relaciones = medicoEspecialidadRepository.findById_IdMedico(idMedico);
        
        return relaciones.stream()
                        .map(relacion -> {
                            Especialidad esp = relacion.getEspecialidad();
                            return new EspecialidadDTO(
                                esp.getIdEspecialidad(),
                                esp.getNombre(),
                                esp.getDescripcion(),
                                esp.getPrecio()
                            );
                        })
                        .collect(Collectors.toList());
    }


    @Transactional
    public void quitarEspecialidad(Long idMedico, Long idEspecialidad) {
        MedicoEspecialidadId relacionId = new MedicoEspecialidadId(idMedico, idEspecialidad);
        if (!medicoEspecialidadRepository.existsById(relacionId)) {
            throw new RuntimeException("El médico no tiene asignada esa especialidad.");
        }
        
        
        disponibilidadMedicoRepository.deleteByIdMedicoAndIdEspecialidad(idMedico, idEspecialidad);
        

        medicoEspecialidadRepository.deleteById(relacionId);
    }

    @Transactional(readOnly = true)
    public Optional<Medico> obtenerMedicoPorId(Long id) {
        return medicoRepository.findById(id);
    }

    @Transactional
    public Medico actualizarMedico(Long id, ActualizarMedicoRequest request) {
        Medico medico = medicoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Médico no encontrado con ID: " + id));

        if (request.getCorreo() != null && !request.getCorreo().equalsIgnoreCase(medico.getCorreo()) &&
            medicoRepository.existsByCorreo(request.getCorreo())) {
            throw new RuntimeException("El nuevo correo ya está registrado por otro médico.");
        }

        if (request.getNombres() != null) medico.setNombres(request.getNombres());
        if (request.getApellidos() != null) medico.setApellidos(request.getApellidos());
        if (request.getCorreo() != null) medico.setCorreo(request.getCorreo());
        if (request.getDireccion() != null) medico.setDireccion(request.getDireccion());
        if (request.getTelefono() != null) medico.setTelefono(request.getTelefono());
        if (request.getFechaNacimiento() != null) {
             medico.setFechaNacimiento(request.getFechaNacimiento());
        }
        if (request.getEstado() != null) medico.setEstado(request.getEstado());
        if (request.getClave() != null && !request.getClave().isEmpty()) {
            medico.setClave(passwordEncoder.encode(request.getClave()));
        }

        return medicoRepository.save(medico);
    }

    @Transactional
    public void eliminarMedico(Long id) {
        if (!medicoRepository.existsById(id)) {
            throw new RuntimeException("Médico no encontrado con ID: " + id);
        }
        medicoRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<MedicoDTO> obtenerMedicosPorEspecialidad(Long idEspecialidad) {
        if (!especialidadRepository.existsById(idEspecialidad)) {
            throw new RuntimeException("Especialidad no encontrada con ID: " + idEspecialidad);
        }
        List<MedicoEspecialidad> relaciones = medicoEspecialidadRepository.findById_IdEspecialidad(idEspecialidad);
        return relaciones.stream()
                         .map(MedicoEspecialidad::getMedico)
                         .map(medico -> new MedicoDTO(
                                 medico.getIdMedico(),
                                 medico.getNombres(),
                                 medico.getApellidos()))
                         .collect(Collectors.toList());
    }
}
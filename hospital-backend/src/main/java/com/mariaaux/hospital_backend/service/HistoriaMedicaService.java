package com.mariaaux.hospital_backend.service;

import com.mariaaux.hospital_backend.dto.RegistrarHistoriaRequest;
import com.mariaaux.hospital_backend.dto.HistoriaMedicaDTO;
import com.mariaaux.hospital_backend.model.*;
import com.mariaaux.hospital_backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;
import java.util.Optional;

@Service
public class HistoriaMedicaService {

    @Autowired
    private HistoriaMedicaRepository historiaMedicaRepository;
    @Autowired
    private CitaRepository citaRepository;
    @Autowired
    private MedicoRepository medicoRepository;
    @Autowired
    private EspecialidadRepository especialidadRepository;
    @Autowired
    private PacienteRepository pacienteRepository;


    @Transactional
    public HistoriaMedica registrarNuevaEntrada(RegistrarHistoriaRequest request) {
        
        Cita cita = citaRepository.findById(request.getIdCita())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cita no encontrada."));

        if (cita.getEstado() == EstadoCita.atendida) {
             throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Esta cita ya fue marcada como atendida y tiene un historial.");
        }
        
        if (historiaMedicaRepository.findByIdCita(request.getIdCita()).isPresent()) {
             throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ya existe un historial registrado para esta cita.");
        }

        HistoriaMedica nuevaEntrada = new HistoriaMedica();
        nuevaEntrada.setIdPaciente(request.getIdPaciente());
        nuevaEntrada.setIdCita(request.getIdCita());
        nuevaEntrada.setIdMedico(request.getIdMedico());
        nuevaEntrada.setSintomas(request.getSintomas());
        nuevaEntrada.setDiagnostico(request.getDiagnostico());
        nuevaEntrada.setFechaConsulta(cita.getFecha());

        nuevaEntrada.setAntecedentesPersonales(request.getAntecedentesPersonales());
        nuevaEntrada.setAntecedentesFamiliares(request.getAntecedentesFamiliares());
        nuevaEntrada.setHistoriaEnfermedadActual(request.getHistoriaEnfermedadActual());
        nuevaEntrada.setHistoriaPsicosocial(request.getHistoriaPsicosocial());

        HistoriaMedica historiaGuardada = historiaMedicaRepository.save(nuevaEntrada);

        cita.setEstado(EstadoCita.atendida);
        citaRepository.save(cita);

        return historiaGuardada;
    }

    @Transactional(readOnly = true)
    public List<HistoriaMedicaDTO> obtenerHistorialPorPaciente(Long idPaciente) {
        if (!pacienteRepository.existsById(idPaciente)) {
             throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Paciente no encontrado con ID: " + idPaciente);
        }
        
        List<HistoriaMedica> historial = historiaMedicaRepository.findByIdPacienteOrderByFechaConsultaDesc(idPaciente);
        return convertirADTO(historial);
    }


    @Transactional(readOnly = true)
    public List<HistoriaMedicaDTO> obtenerHistorialPorDniYMedico(String dni, Long idMedico) {
        // Verificar que el médico existe
        if (!medicoRepository.existsById(idMedico)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Médico no encontrado con ID: " + idMedico);
        }

        // Buscar historial usando el query del repositorio
        List<HistoriaMedica> historial = historiaMedicaRepository.findHistorialByDniPacienteAndIdMedico(dni, idMedico);
        
        if (historial.isEmpty()) {
            // Verificar si el paciente existe
            Optional<Paciente> paciente = pacienteRepository.findByDni(dni);
            if (paciente.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontró ningún paciente con DNI: " + dni);
            }
            // Si existe pero no tiene historial con este médico
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, 
                "No se encontró historial para el DNI " + dni + " atendido por este médico.");
        }

        return convertirADTO(historial);
    }


    private List<HistoriaMedicaDTO> convertirADTO(List<HistoriaMedica> historial) {
        return historial.stream().map(h -> {
            Optional<Cita> optCita = citaRepository.findById(h.getIdCita());
            Medico medico = medicoRepository.findById(h.getIdMedico()).orElse(null);
            Paciente paciente = pacienteRepository.findById(h.getIdPaciente()).orElse(null);
            
            String nombreMedicoCompleto = (medico != null) ? medico.getNombres() + " " + medico.getApellidos() : "Médico Desconocido";
            String nombrePacienteCompleto = (paciente != null) ? paciente.getNombres() + " " + paciente.getApellidos() : "Paciente Desconocido";
            String especialidadNombre = "Especialidad Desconocida";
            Long idEspecialidadCita = null;
            
            if (optCita.isPresent()) {
                Optional<Especialidad> optEspecialidad = especialidadRepository.findById(optCita.get().getIdEspecialidad());
                especialidadNombre = optEspecialidad.map(Especialidad::getNombre).orElse("Desconocida");
                idEspecialidadCita = optCita.get().getIdEspecialidad();
            }
            
            HistoriaMedicaDTO dto = new HistoriaMedicaDTO();
            dto.setIdHistoriaMedica(h.getIdHistoriaMedica());
            dto.setIdCita(h.getIdCita());
            dto.setFechaConsulta(h.getFechaConsulta());
            dto.setDiagnostico(h.getDiagnostico());
            dto.setSintomas(h.getSintomas());
            dto.setNombreMedico(nombreMedicoCompleto);
            dto.setNombrePaciente(nombrePacienteCompleto); 
            dto.setEspecialidad(especialidadNombre);
            dto.setIdEspecialidad(idEspecialidadCita);
            dto.setAntecedentes(h.getAntecedentes());
            dto.setAntecedentesPersonales(h.getAntecedentesPersonales());
            dto.setAntecedentesFamiliares(h.getAntecedentesFamiliares());
            dto.setHistoriaEnfermedadActual(h.getHistoriaEnfermedadActual());
            dto.setHistoriaPsicosocial(h.getHistoriaPsicosocial());
            
            return dto;
        }).collect(Collectors.toList());
    }
}
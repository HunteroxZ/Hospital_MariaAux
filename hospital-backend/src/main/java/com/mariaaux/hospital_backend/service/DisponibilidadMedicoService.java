package com.mariaaux.hospital_backend.service;

import com.mariaaux.hospital_backend.dto.RegistrarDisponibilidadRequest;
import com.mariaaux.hospital_backend.model.DisponibilidadMedico;
import com.mariaaux.hospital_backend.repository.DisponibilidadMedicoRepository;
import com.mariaaux.hospital_backend.repository.MedicoRepository;
import com.mariaaux.hospital_backend.repository.EspecialidadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DisponibilidadMedicoService {

    @Autowired
    private DisponibilidadMedicoRepository disponibilidadRepository;

    @Autowired
    private MedicoRepository medicoRepository; 

    @Autowired
    private EspecialidadRepository especialidadRepository;

    /**
     * 
     * @param idMedico 
     * @param request 
     * @return
     */
    @Transactional
    public DisponibilidadMedico anadirDisponibilidad(Long idMedico, RegistrarDisponibilidadRequest request) {

        // --- Validaciones ---
        if (!medicoRepository.existsById(idMedico)) {
            throw new RuntimeException("Médico no encontrado con ID: " + idMedico);
        }
        if (!especialidadRepository.existsById(request.getIdEspecialidad())) {
            throw new RuntimeException("Especialidad no encontrada con ID: " + request.getIdEspecialidad());
        }
        if (request.getHoraFin().isBefore(request.getHoraInicio()) || request.getHoraFin().equals(request.getHoraInicio())) {
            throw new RuntimeException("La hora de fin debe ser posterior a la hora de inicio.");
        }

        
        boolean seSolapa = disponibilidadRepository.existsOverlappingDisponibilidad(
                idMedico,
                request.getDiaSemana(),
                request.getHoraInicio(),
                request.getHoraFin()
        );
        if (seSolapa) {
            throw new RuntimeException("El horario propuesto (" + request.getDiaSemana() + " " +
                                       request.getHoraInicio() + "-" + request.getHoraFin() +
                                       ") se duplica con un horario existente para este médico.");
        }

        
        DisponibilidadMedico nuevaDisponibilidad = new DisponibilidadMedico();
        nuevaDisponibilidad.setIdMedico(idMedico); 
        nuevaDisponibilidad.setIdEspecialidad(request.getIdEspecialidad());
        nuevaDisponibilidad.setDiaSemana(request.getDiaSemana());
        nuevaDisponibilidad.setHoraInicio(request.getHoraInicio());
        nuevaDisponibilidad.setHoraFin(request.getHoraFin());

        return disponibilidadRepository.save(nuevaDisponibilidad);
    }

    /**
     * 
     * @param idMedico 
     * @return 
     */
    @Transactional(readOnly = true)
    public List<DisponibilidadMedico> obtenerDisponibilidadPorMedico(Long idMedico) {
        if (!medicoRepository.existsById(idMedico)) {
            throw new RuntimeException("Médico no encontrado con ID: " + idMedico);
        }
        return disponibilidadRepository.findByIdMedicoOrderByDiaSemanaAscHoraInicioAsc(idMedico);
    }

    /**
     * 
     * @param idDisponibilidad 
     */
    @Transactional
    public void eliminarDisponibilidad(Long idDisponibilidad) {
        if (!disponibilidadRepository.existsById(idDisponibilidad)) {
            throw new RuntimeException("Bloque de disponibilidad no encontrado con ID: " + idDisponibilidad);
        }
        disponibilidadRepository.deleteById(idDisponibilidad);
    }
}
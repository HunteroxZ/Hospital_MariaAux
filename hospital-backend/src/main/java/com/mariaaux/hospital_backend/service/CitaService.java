package com.mariaaux.hospital_backend.service;

import com.mariaaux.hospital_backend.dto.CitaDetalleDTO;
import com.mariaaux.hospital_backend.dto.CitaMedicoDTO; 
import com.mariaaux.hospital_backend.dto.RegistrarCitaRequest;
import com.mariaaux.hospital_backend.model.*; 
import com.mariaaux.hospital_backend.repository.CitaRepository;
import com.mariaaux.hospital_backend.repository.PacienteRepository;
import com.mariaaux.hospital_backend.repository.MedicoRepository;
import com.mariaaux.hospital_backend.repository.EspecialidadRepository;
import com.mariaaux.hospital_backend.repository.DisponibilidadMedicoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.DayOfWeek; 
import java.util.List;
import java.util.stream.Collectors;
import java.util.Comparator; 

@Service
public class CitaService {

    @Autowired
    private CitaRepository citaRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private MedicoRepository medicoRepository;

    @Autowired
    private EspecialidadRepository especialidadRepository;

    @Autowired
    private DisponibilidadMedicoRepository disponibilidadMedicoRepository;

    @Transactional
    public Cita registrarCita(RegistrarCitaRequest request) {

        if (!pacienteRepository.existsById(request.getIdPaciente())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "El paciente no existe");
        }
        if (!medicoRepository.existsById(request.getIdMedico())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "El médico no existe");
        }
        if (!especialidadRepository.existsById(request.getIdEspecialidad())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "La especialidad no existe");
        }


        LocalDate fechaCita = request.getFecha();
        LocalTime horaCita = request.getHora();
        Long idMedico = request.getIdMedico();
        Long idEspecialidad = request.getIdEspecialidad();

        DiaSemana diaSemanaEnum = traducirDiaSemana(fechaCita.getDayOfWeek());

        List<DisponibilidadMedico> disponibilidades = disponibilidadMedicoRepository
                .findByIdMedicoAndIdEspecialidadOrderByDiaSemanaAscHoraInicioAsc(idMedico, idEspecialidad);

        if (disponibilidades.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El médico no tiene horarios configurados para esa especialidad.");
        }

        boolean horarioValido = false;
        for (DisponibilidadMedico disp : disponibilidades) {
            if (disp.getDiaSemana() == diaSemanaEnum) {
                LocalTime horaInicioBloque = disp.getHoraInicio();
                LocalTime horaFinBloque = disp.getHoraFin();
                if (!horaCita.isBefore(horaInicioBloque) && horaCita.isBefore(horaFinBloque)) {
                    horarioValido = true;
                    break;
                }
            }
        }

        if (!horarioValido) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El médico no atiende en el día u hora seleccionados para esa especialidad. Verifique la disponibilidad.");
        }

        if (citaRepository.existsByIdPacienteAndFecha(request.getIdPaciente(), request.getFecha())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usted ya ha registrado una cita para esta fecha. Por favor, escoja otra fecha.");
        }


        if (citaRepository.existsByIdMedicoAndFechaAndHora(request.getIdMedico(), request.getFecha(), request.getHora())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El médico ya tiene otra cita programada en esa hora. Por favor, escoja otra hora.");
        }


        Cita nuevaCita = new Cita();
        nuevaCita.setIdPaciente(request.getIdPaciente());
        nuevaCita.setIdMedico(request.getIdMedico());
        nuevaCita.setIdEspecialidad(request.getIdEspecialidad());
        nuevaCita.setFecha(request.getFecha());
        nuevaCita.setHora(request.getHora());
        nuevaCita.setMotivoConsulta(request.getMotivoConsulta());
        nuevaCita.setSintomas(request.getSintomas());

        return citaRepository.save(nuevaCita);
    }


    private DiaSemana traducirDiaSemana(DayOfWeek diaIngles) {
        switch (diaIngles) {
            case MONDAY: return DiaSemana.lunes;
            case TUESDAY: return DiaSemana.martes;
            case WEDNESDAY: return DiaSemana.miercoles;
            case THURSDAY: return DiaSemana.jueves;
            case FRIDAY: return DiaSemana.viernes;
            case SATURDAY: return DiaSemana.sabado;
            case SUNDAY:
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se admiten citas en domingo.");
            default:
                throw new IllegalArgumentException("Día de la semana inválido: " + diaIngles);
        }
    }



    /**
     * 
     * @param idCita 
     * @return 
     */
    @Transactional
    public Cita marcarCitaComoAtendida(Long idCita) {
        Cita cita = citaRepository.findById(idCita)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cita no encontrada con ID: " + idCita));

 
        if (cita.getEstado() == EstadoCita.atendida || cita.getEstado() == EstadoCita.cancelada || cita.getEstado() == EstadoCita.no_presentado) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El estado actual de la cita no permite ser marcada como atendida.");
        }

        cita.setEstado(EstadoCita.atendida);
        return citaRepository.save(cita);
    }

    /**
     * 
     * @param idCita 
     * @return 
     */
    @Transactional
    public Cita marcarCitaComoNoPresentada(Long idCita) {
        Cita cita = citaRepository.findById(idCita)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cita no encontrada con ID: " + idCita));

        
        if (cita.getEstado() == EstadoCita.atendida || cita.getEstado() == EstadoCita.cancelada || cita.getEstado() == EstadoCita.no_presentado) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El estado actual de la cita no puede ser modificado a no presentado.");
        }

        cita.setEstado(EstadoCita.no_presentado);
        return citaRepository.save(cita);
    }

    @Transactional(readOnly = true)
    public List<CitaDetalleDTO> obtenerCitasPorPaciente(Long idPaciente) {
        if (!pacienteRepository.existsById(idPaciente)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Paciente no encontrado con ID: " + idPaciente);
        }

        List<Cita> citas = citaRepository.findByIdPacienteOrderByFechaDescHoraDesc(idPaciente);

        return citas.stream().map(cita -> {
            Medico medico = medicoRepository.findById(cita.getIdMedico()).orElse(null);
            Especialidad especialidad = especialidadRepository.findById(cita.getIdEspecialidad()).orElse(null);

            String nombreMedicoCompleto = (medico != null) ? medico.getNombres() + " " + medico.getApellidos() : "Desconocido";
            String nombreEspecialidad = (especialidad != null) ? especialidad.getNombre() : "Desconocida";

            return new CitaDetalleDTO(
                cita.getIdCita(), cita.getFecha(), cita.getHora(),
                cita.getMotivoConsulta(), cita.getSintomas(), cita.getEstado(),
                cita.getFechaCreacion(), cita.getIdPaciente(), cita.getIdMedico(),
                cita.getIdEspecialidad(), nombreMedicoCompleto, nombreEspecialidad
            );
        }).collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public List<LocalTime> obtenerHorasReservadasPorMedicoYFecha(Long idMedico, LocalDate fecha) {
        if (!medicoRepository.existsById(idMedico)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Médico no encontrado con ID: " + idMedico);
        }
        List<Cita> citasExistentes = citaRepository.findByIdMedicoAndFecha(idMedico, fecha);
        return citasExistentes.stream()
                              .map(Cita::getHora)
                              .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public List<CitaMedicoDTO> obtenerCitasPorMedicoYFecha(Long idMedico, LocalDate fecha) {
        if (!medicoRepository.existsById(idMedico)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Médico no encontrado con ID: " + idMedico);
        }

        List<Cita> citasDelDia = citaRepository.findByIdMedicoAndFecha(idMedico, fecha);

        return citasDelDia.stream()
            .map(cita -> {
                Paciente paciente = pacienteRepository.findById(cita.getIdPaciente()).orElse(null);
                String nombrePacienteCompleto = (paciente != null) ? paciente.getNombres() + " " + paciente.getApellidos() : "Paciente Desconocido";

                return new CitaMedicoDTO(
                    cita.getIdCita(),
                    cita.getHora(),
                    nombrePacienteCompleto,
                    cita.getMotivoConsulta(),
                    cita.getEstado(),
                    cita.getIdPaciente(),
                    cita.getIdEspecialidad()
                );
            })
            .sorted(Comparator.comparing(CitaMedicoDTO::getHora))
            .collect(Collectors.toList());
    }
}
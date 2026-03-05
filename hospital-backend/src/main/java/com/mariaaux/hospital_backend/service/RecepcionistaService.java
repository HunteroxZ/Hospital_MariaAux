package com.mariaaux.hospital_backend.service;


import com.mariaaux.hospital_backend.dto.Recepcionista.*;
import com.mariaaux.hospital_backend.model.*;
import com.mariaaux.hospital_backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RecepcionistaService {

    @Autowired
    private RecepcionistaRepository recepcionistaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CitaRepository citaRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private MedicoRepository medicoRepository;

    @Autowired
    private EspecialidadRepository especialidadRepository;

    @Transactional(readOnly = true)
    public LoginResponseRecepcionista loginRecepcionista(String dni, String clave) {
        Recepcionista recepcionista = recepcionistaRepository.findByDni(dni)
                .orElseThrow(() -> new RuntimeException("DNI o contraseña incorrectos"));

        if (!passwordEncoder.matches(clave, recepcionista.getClave())) {
            throw new RuntimeException("DNI o contraseña incorrectos");
        }

        if (recepcionista.getEstado() == EstadoRecepcionista.INACTIVO) {
            throw new RuntimeException("El recepcionista se encuentra inactivo. Contacte al administrador.");
        }

        return new LoginResponseRecepcionista(
            recepcionista.getIdRecepcionista(),
            recepcionista.getNombres(),
            recepcionista.getApellidos(),
            recepcionista.getCorreo()
        );
    }

    @Transactional
    public Recepcionista registrarRecepcionista(RegistrarRecepcionistaRequest request) {
        if (recepcionistaRepository.existsByDni(request.getDni())) {
            throw new RuntimeException("El DNI ya está registrado.");
        }
        if (recepcionistaRepository.existsByCorreo(request.getCorreo())) {
            throw new RuntimeException("El correo ya está registrado.");
        }

        Recepcionista nuevo = new Recepcionista();
        nuevo.setNombres(request.getNombres());
        nuevo.setApellidos(request.getApellidos());
        nuevo.setDni(request.getDni());
        nuevo.setCorreo(request.getCorreo());
        nuevo.setDireccion(request.getDireccion());
        nuevo.setTelefono(request.getTelefono());
        nuevo.setClave(passwordEncoder.encode(request.getClave()));
        nuevo.setEstado(EstadoRecepcionista.ACTIVO);

        return recepcionistaRepository.save(nuevo);
    }

    @Transactional(readOnly = true)
    public List<CitaPacienteRecepcionistaDTO> buscarCitasPendientesPorDni(String dniPaciente) {
        Paciente paciente = pacienteRepository.findByDni(dniPaciente)
                .orElseThrow(() -> new RuntimeException("No se encontró ningún paciente con DNI: " + dniPaciente));

        List<Cita> citasPendientes = citaRepository.findByIdPacienteOrderByFechaDescHoraDesc(paciente.getIdPaciente())
                .stream()
                .filter(c -> c.getEstado() == EstadoCita.pendiente)
                .collect(Collectors.toList());

        if (citasPendientes.isEmpty()) {
            throw new RuntimeException("No se encontraron citas pendientes para el paciente con DNI: " + dniPaciente);
        }

        return citasPendientes.stream().map(cita -> {
            Medico medico = medicoRepository.findById(cita.getIdMedico()).orElse(null);
            Especialidad especialidad = especialidadRepository.findById(cita.getIdEspecialidad()).orElse(null);

            String nombrePaciente = paciente.getNombres() + " " + paciente.getApellidos();
            String nombreMedico = medico != null ? medico.getNombres() + " " + medico.getApellidos() : "Desconocido";
            String nombreEspecialidad = especialidad != null ? especialidad.getNombre() : "Desconocida";
            BigDecimal precio = especialidad != null ? especialidad.getPrecio() : BigDecimal.ZERO;

            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

            return new CitaPacienteRecepcionistaDTO(
                cita.getIdCita(),
                nombrePaciente,
                dniPaciente,
                nombreMedico,
                nombreEspecialidad,
                cita.getFecha().format(dateFormatter),
                cita.getHora().format(timeFormatter),
                cita.getMotivoConsulta(),
                cita.getEstado().name(),
                precio,
                false
            );
        }).collect(Collectors.toList());
    }

    @Transactional
    public PagoResponse procesarPago(PagoRequest request) {
        Cita cita = citaRepository.findById(request.getIdCita())
                .orElseThrow(() -> new RuntimeException("Cita no encontrada con ID: " + request.getIdCita()));

        if (cita.getEstado() != EstadoCita.pendiente) {
            throw new RuntimeException("La cita no está en estado pendiente. No se puede procesar el pago.");
        }

        Especialidad especialidad = especialidadRepository.findById(cita.getIdEspecialidad())
                .orElseThrow(() -> new RuntimeException("Especialidad no encontrada"));

        BigDecimal montoCita = especialidad.getPrecio();
        BigDecimal vuelto = BigDecimal.ZERO;
        Boolean esPagoSIS = false;

    
        if (request.getTieneSIS() != null && request.getTieneSIS()) {
            esPagoSIS = true;
            montoCita = BigDecimal.ZERO;
        } else {
            
            if ("EFECTIVO".equalsIgnoreCase(request.getMetodoPago())) {
                if (request.getMontoPagado().compareTo(montoCita) < 0) {
                    throw new RuntimeException("El monto pagado es insuficiente. Monto requerido: " + montoCita);
                }
                vuelto = request.getMontoPagado().subtract(montoCita);
            }
        }

        
        cita.setEstado(EstadoCita.confirmada);
        citaRepository.save(cita);

       
        String numeroComprobante = esPagoSIS ? "TICKET-" + System.currentTimeMillis() : "COMP-" + System.currentTimeMillis();
        LocalDateTime fechaHoraPago = LocalDateTime.now();

        return new PagoResponse(
            true,
            esPagoSIS ? "Ticket generado exitosamente para paciente SIS" : "Pago procesado exitosamente",
            numeroComprobante,
            fechaHoraPago,
            vuelto,
            esPagoSIS
        );
    }

    @Transactional
    public PagoResponse procesarTicketSIS(Long idCita) {
        Cita cita = citaRepository.findById(idCita)
                .orElseThrow(() -> new RuntimeException("Cita no encontrada con ID: " + idCita));

        if (cita.getEstado() != EstadoCita.pendiente) {
            throw new RuntimeException("La cita no está en estado pendiente.");
        }

        cita.setEstado(EstadoCita.confirmada);
        citaRepository.save(cita);

        String numeroTicket = "TICKET-SIS-" + System.currentTimeMillis();
        LocalDateTime fechaHora = LocalDateTime.now();

        return new PagoResponse(
            true,
            "Ticket SIS generado exitosamente",
            numeroTicket,
            fechaHora,
            BigDecimal.ZERO,
            true
        );
    }
}

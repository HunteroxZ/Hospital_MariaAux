package com.mariaaux.hospital_backend.service;

import com.mariaaux.hospital_backend.dto.DashboardDTO;
import com.mariaaux.hospital_backend.model.*;
import com.mariaaux.hospital_backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private MedicoRepository medicoRepository;

    @Autowired
    private EspecialidadRepository especialidadRepository;

    @Autowired
    private CitaRepository citaRepository;

    @Transactional(readOnly = true)
    public DashboardDTO obtenerEstadisticas() {
        DashboardDTO dashboard = new DashboardDTO();
        
        dashboard.setTotalPacientes(pacienteRepository.count());
        dashboard.setTotalMedicos(medicoRepository.count());
        dashboard.setTotalEspecialidades(especialidadRepository.count());
        
        LocalDate hoy = LocalDate.now();
        List<Cita> todasLasCitas = citaRepository.findAll();
        
        dashboard.setTotalCitasHoy(
            todasLasCitas.stream()
                .filter(c -> c.getFecha().equals(hoy))
                .count()
        );
        
        dashboard.setCitasPendientes(
            todasLasCitas.stream()
                .filter(c -> c.getEstado() == EstadoCita.pendiente)
                .count()
        );
        
        dashboard.setCitasConfirmadas(
            todasLasCitas.stream()
                .filter(c -> c.getEstado() == EstadoCita.confirmada)
                .count()
        );
        
        dashboard.setCitasAtendidas(
            todasLasCitas.stream()
                .filter(c -> c.getEstado() == EstadoCita.atendida)
                .count()
        );
        
        dashboard.setCitasCanceladas(
            todasLasCitas.stream()
                .filter(c -> c.getEstado() == EstadoCita.cancelada)
                .count()
        );
        
        dashboard.setCitasNoPresentadas(
            todasLasCitas.stream()
                .filter(c -> c.getEstado() == EstadoCita.no_presentado)
                .count()
        );

        Map<String, Long> citasPorEstado = todasLasCitas.stream()
            .collect(Collectors.groupingBy(
                c -> c.getEstado().name(),
                Collectors.counting()
            ));
        dashboard.setCitasPorEstado(citasPorEstado);
        
        dashboard.setTopEspecialidades(obtenerTopEspecialidades(todasLasCitas));
        dashboard.setTopMedicos(obtenerTopMedicos(todasLasCitas));
        dashboard.setCitasPorDia(obtenerCitasPorDia(todasLasCitas, hoy, hoy.minusDays(6)));
        
        dashboard.setIngresosEstimados(calcularIngresos(todasLasCitas));
        
        dashboard.setNotificaciones(generarNotificaciones(todasLasCitas));
        
        return dashboard;
    }

    @Transactional(readOnly = true)
    public DashboardDTO obtenerEstadisticasConRango(LocalDate fechaInicio, LocalDate fechaFin) {
        DashboardDTO dashboard = new DashboardDTO();
        
        dashboard.setTotalPacientes(pacienteRepository.count());
        dashboard.setTotalMedicos(medicoRepository.count());
        dashboard.setTotalEspecialidades(especialidadRepository.count());
        
        LocalDate hoy = LocalDate.now();
        List<Cita> todasLasCitas = citaRepository.findAll();
        
        dashboard.setTotalCitasHoy(
            todasLasCitas.stream()
                .filter(c -> c.getFecha().equals(hoy))
                .count()
        );
        
        dashboard.setCitasPendientes(
            todasLasCitas.stream()
                .filter(c -> c.getEstado() == EstadoCita.pendiente)
                .count()
        );
        
        dashboard.setCitasConfirmadas(
            todasLasCitas.stream()
                .filter(c -> c.getEstado() == EstadoCita.confirmada)
                .count()
        );
        
        dashboard.setCitasAtendidas(
            todasLasCitas.stream()
                .filter(c -> c.getEstado() == EstadoCita.atendida)
                .count()
        );
        
        dashboard.setCitasCanceladas(
            todasLasCitas.stream()
                .filter(c -> c.getEstado() == EstadoCita.cancelada)
                .count()
        );
        
        dashboard.setCitasNoPresentadas(
            todasLasCitas.stream()
                .filter(c -> c.getEstado() == EstadoCita.no_presentado)
                .count()
        );

        Map<String, Long> citasPorEstado = todasLasCitas.stream()
            .collect(Collectors.groupingBy(
                c -> c.getEstado().name(),
                Collectors.counting()
            ));
        dashboard.setCitasPorEstado(citasPorEstado);
        
        dashboard.setTopEspecialidades(obtenerTopEspecialidades(todasLasCitas));
        dashboard.setTopMedicos(obtenerTopMedicos(todasLasCitas));
        
        dashboard.setCitasPorDia(obtenerCitasPorDia(todasLasCitas, fechaFin, fechaInicio));
        
        dashboard.setIngresosEstimados(calcularIngresos(todasLasCitas));
        dashboard.setNotificaciones(generarNotificaciones(todasLasCitas));
        
        return dashboard;
    }

    private List<DashboardDTO.EspecialidadStats> obtenerTopEspecialidades(List<Cita> citas) {
        Map<Long, Long> citasPorEspecialidad = citas.stream()
            .collect(Collectors.groupingBy(
                Cita::getIdEspecialidad,
                Collectors.counting()
            ));
        
        return citasPorEspecialidad.entrySet().stream()
            .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
            .limit(5)
            .map(entry -> {
                Especialidad esp = especialidadRepository.findById(entry.getKey()).orElse(null);
                if (esp == null) return null;
                
                BigDecimal ingresos = citas.stream()
                    .filter(c -> c.getIdEspecialidad().equals(entry.getKey()) 
                              && (c.getEstado() == EstadoCita.atendida || c.getEstado() == EstadoCita.confirmada))
                    .map(c -> esp.getPrecio())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                
                return new DashboardDTO.EspecialidadStats(
                    esp.getNombre(),
                    entry.getValue(),
                    ingresos
                );
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    private List<DashboardDTO.MedicoStats> obtenerTopMedicos(List<Cita> citas) {
        Map<Long, Long> citasPorMedico = citas.stream()
            .collect(Collectors.groupingBy(
                Cita::getIdMedico,
                Collectors.counting()
            ));
        
        return citasPorMedico.entrySet().stream()
            .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
            .limit(5)
            .map(entry -> {
                Medico medico = medicoRepository.findById(entry.getKey()).orElse(null);
                if (medico == null) return null;
                
                String nombreCompleto = medico.getNombres() + " " + medico.getApellidos();
                
                Map<Long, Long> especialidades = citas.stream()
                    .filter(c -> c.getIdMedico().equals(entry.getKey()))
                    .collect(Collectors.groupingBy(
                        Cita::getIdEspecialidad,
                        Collectors.counting()
                    ));
                
                String especialidadPrincipal = "N/A";
                if (!especialidades.isEmpty()) {
                    Long idEspPrincipal = especialidades.entrySet().stream()
                        .max(Map.Entry.comparingByValue())
                        .map(Map.Entry::getKey)
                        .orElse(null);
                    
                    if (idEspPrincipal != null) {
                        Especialidad esp = especialidadRepository.findById(idEspPrincipal).orElse(null);
                        if (esp != null) {
                            especialidadPrincipal = esp.getNombre();
                        }
                    }
                }
                
                return new DashboardDTO.MedicoStats(
                    nombreCompleto,
                    entry.getValue(),
                    especialidadPrincipal
                );
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    private List<DashboardDTO.CitasPorDia> obtenerCitasPorDia(List<Cita> citas, LocalDate fechaFin, LocalDate fechaInicio) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");
        List<DashboardDTO.CitasPorDia> resultado = new ArrayList<>();
        
        LocalDate fechaActual = fechaInicio;
        while (!fechaActual.isAfter(fechaFin)) {
            final LocalDate fecha = fechaActual;
            long count = citas.stream()
                .filter(c -> c.getFecha().equals(fecha))
                .count();
        
            System.out.println("Fecha: " + fecha.format(formatter) + " - Citas: " + count);
            
            resultado.add(new DashboardDTO.CitasPorDia(
                fecha.format(formatter),
                count
            ));
            
            fechaActual = fechaActual.plusDays(1);
        }
        
        return resultado;
    }

    private BigDecimal calcularIngresos(List<Cita> citas) {
        
        return citas.stream()
            .filter(c -> c.getEstado() == EstadoCita.atendida || c.getEstado() == EstadoCita.confirmada)
            .map(cita -> {
                Especialidad esp = especialidadRepository
                    .findById(cita.getIdEspecialidad())
                    .orElse(null);
                return esp != null ? esp.getPrecio() : BigDecimal.ZERO;
            })
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<DashboardDTO.Notificacion> generarNotificaciones(List<Cita> citas) {
        List<DashboardDTO.Notificacion> notificaciones = new ArrayList<>();
        
        LocalDate inicioSemana = LocalDate.now().minusDays(7);
        long pacientesEstaSemana = pacienteRepository.findAll().stream()
            .filter(p -> p.getFechaRegistro().toLocalDate().isAfter(inicioSemana))
            .count();
        
        if (pacientesEstaSemana < 10) {
            notificaciones.add(new DashboardDTO.Notificacion(
                "warning",
                "Esta semana no se han registrado suficientes pacientes!",
                "ROJO"
            ));
        }
        
        LocalDate inicioMes = LocalDate.now().withDayOfMonth(1);
        BigDecimal ingresosMes = citas.stream()
            .filter(c -> !c.getFecha().isBefore(inicioMes))
            .filter(c -> c.getEstado() == EstadoCita.atendida || c.getEstado() == EstadoCita.confirmada)
            .map(cita -> {
                Especialidad esp = especialidadRepository
                    .findById(cita.getIdEspecialidad())
                    .orElse(null);
                return esp != null ? esp.getPrecio() : BigDecimal.ZERO;
            })
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal metaMensual = new BigDecimal("20000");
        double porcentajeLogrado = ingresosMes.doubleValue() / metaMensual.doubleValue() * 100;
        
        if (porcentajeLogrado >= 80 && porcentajeLogrado < 100) {
            notificaciones.add(new DashboardDTO.Notificacion(
                "success",
                String.format("Falta poco para llegar a la meta financiera del mes (%.2f%%), ánimo!", porcentajeLogrado),
                "VERDE"
            ));
        }
        
        return notificaciones;
    }
}
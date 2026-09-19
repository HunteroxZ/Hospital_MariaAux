package com.mariaaux.hospital_backend.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mariaaux.hospital_backend.dto.Reportes.AdicionalPorEspecialidadDTO;
import com.mariaaux.hospital_backend.dto.Reportes.AdicionalPorMedicoDTO;
import com.mariaaux.hospital_backend.dto.Reportes.ReporteAdicionalesDTO;
import com.mariaaux.hospital_backend.dto.Reportes.ReporteCitasDTO;
import com.mariaaux.hospital_backend.dto.Reportes.ReporteIngresoEspecialidadDTO;
import com.mariaaux.hospital_backend.dto.Reportes.ReporteIngresoMedicoDTO;
import com.mariaaux.hospital_backend.dto.Reportes.ReporteIngresosGeneralDTO;
import com.mariaaux.hospital_backend.dto.Reportes.ReporteMedicoDTO;
import com.mariaaux.hospital_backend.dto.Reportes.ReportePacienteDTO;
import com.mariaaux.hospital_backend.model.Cita;
import com.mariaaux.hospital_backend.model.Especialidad;
import com.mariaaux.hospital_backend.model.EstadoCita;
import com.mariaaux.hospital_backend.model.Medico;
import com.mariaaux.hospital_backend.model.Paciente;
import com.mariaaux.hospital_backend.repository.CitaRepository;
import com.mariaaux.hospital_backend.repository.EspecialidadRepository;
import com.mariaaux.hospital_backend.repository.MedicoEspecialidadRepository;
import com.mariaaux.hospital_backend.repository.MedicoRepository;
import com.mariaaux.hospital_backend.repository.PacienteRepository;

@Service
public class ReporteService {

    @Autowired
    private CitaRepository citaRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private MedicoRepository medicoRepository;

    @Autowired
    private EspecialidadRepository especialidadRepository;

    @Autowired
    private MedicoEspecialidadRepository medicoEspecialidadRepository;


    @Transactional(readOnly = true)
    public List<ReporteCitasDTO> generarReporteCitas(
            LocalDate fechaInicio, 
            LocalDate fechaFin,
            Long idEspecialidad,
            Long idMedico,
            String estado) {
        
        List<Cita> citas = citaRepository.findAll().stream()
            .filter(c -> !c.getFecha().isBefore(fechaInicio) && !c.getFecha().isAfter(fechaFin))
            .filter(c -> idEspecialidad == null || c.getIdEspecialidad().equals(idEspecialidad))
            .filter(c -> idMedico == null || c.getIdMedico().equals(idMedico))
            .filter(c -> estado == null || c.getEstado().name().equalsIgnoreCase(estado))
            .sorted(Comparator.comparing(Cita::getFecha).reversed()
                    .thenComparing(Cita::getHora).reversed())
            .collect(Collectors.toList());

        return citas.stream()
            .map(this::convertirACitaReporte)
            .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public ReporteIngresosGeneralDTO generarReporteIngresos(
            LocalDate fechaInicio,
            LocalDate fechaFin) {
        
        List<Cita> citasEnRango = citaRepository.findAll().stream()
            .filter(c -> !c.getFecha().isBefore(fechaInicio) && !c.getFecha().isAfter(fechaFin))
            .collect(Collectors.toList());

        long totalCitas = citasEnRango.size();
        long citasAtendidas = citasEnRango.stream()
            .filter(c -> c.getEstado() == EstadoCita.atendida)
            .count();

        List<ReporteIngresoEspecialidadDTO> ingresosPorEsp = calcularIngresosPorEspecialidad(citasEnRango);
        

        List<ReporteIngresoMedicoDTO> ingresosPorMed = calcularIngresosPorMedico(citasEnRango);


        BigDecimal ingresoTotal = ingresosPorEsp.stream()
            .map(ReporteIngresoEspecialidadDTO::getIngresoTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new ReporteIngresosGeneralDTO(
            fechaInicio,
            fechaFin,
            totalCitas,
            citasAtendidas,
            ingresoTotal,
            ingresosPorEsp,
            ingresosPorMed
        );
    }

    @Transactional(readOnly = true)
    public List<ReportePacienteDTO> generarReportePacientes() {
        List<Paciente> pacientes = pacienteRepository.findAll();
        
        return pacientes.stream()
            .map(p -> {
                Long totalCitas = citaRepository.findByIdPacienteOrderByFechaDescHoraDesc(p.getIdPaciente())
                    .stream()
                    .count();
                
                return new ReportePacienteDTO(
                    p.getIdPaciente(),
                    p.getNombres() + " " + p.getApellidos(),
                    p.getDni(),
                    p.getSexo().name(),
                    p.getCorreo(),
                    p.getTelefono() != null ? p.getTelefono() : "Sin teléfono",
                    totalCitas,
                    p.getFechaRegistro().toLocalDate()
                );
            })
            .sorted(Comparator.comparing(ReportePacienteDTO::getTotalCitas).reversed())
            .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public List<ReporteMedicoDTO> generarReporteMedicos() {
        List<Medico> medicos = medicoRepository.findAll();
        
        return medicos.stream()
            .map(m -> {
                Long totalCitas = citaRepository.findAll().stream()
                    .filter(c -> c.getIdMedico().equals(m.getIdMedico()))
                    .count();
                
                List<String> especialidades = medicoEspecialidadRepository
                    .findById_IdMedico(m.getIdMedico()).stream()
                    .map(me -> me.getEspecialidad().getNombre())
                    .collect(Collectors.toList());
                
                return new ReporteMedicoDTO(
                    m.getIdMedico(),
                    m.getNombres() + " " + m.getApellidos(),
                    m.getDni(),
                    m.getCodigoColegiatura(),
                    m.getCorreo(),
                    m.getTelefono() != null ? m.getTelefono() : "Sin teléfono",
                    m.getEstado().name(),
                    totalCitas,
                    especialidades
                );
            })
            .sorted(Comparator.comparing(ReporteMedicoDTO::getTotalCitas).reversed())
            .collect(Collectors.toList());
    }



    private ReporteCitasDTO convertirACitaReporte(Cita cita) {
        Paciente paciente = pacienteRepository.findById(cita.getIdPaciente()).orElse(null);
        Medico medico = medicoRepository.findById(cita.getIdMedico()).orElse(null);
        Especialidad especialidad = especialidadRepository.findById(cita.getIdEspecialidad()).orElse(null);

        String nombrePaciente = paciente != null ? 
            paciente.getNombres() + " " + paciente.getApellidos() : "Desconocido";
        String dniPaciente = paciente != null ? paciente.getDni() : "N/A";
        String nombreMedico = medico != null ? 
            medico.getNombres() + " " + medico.getApellidos() : "Desconocido";
        String nombreEsp = especialidad != null ? especialidad.getNombre() : "Desconocida";
        BigDecimal precio = especialidad != null ? especialidad.getPrecio() : BigDecimal.ZERO;

        return new ReporteCitasDTO(
            cita.getIdCita(),
            cita.getFecha(),
            cita.getHora(),
            nombrePaciente,
            dniPaciente,
            nombreMedico,
            nombreEsp,
            cita.getEstado().name(),
            cita.getMotivoConsulta(),
            precio
        );
    }

    private List<ReporteIngresoEspecialidadDTO> calcularIngresosPorEspecialidad(List<Cita> citas) {
        Map<Long, List<Cita>> citasPorEsp = citas.stream()
            .collect(Collectors.groupingBy(Cita::getIdEspecialidad));

        return citasPorEsp.entrySet().stream()
            .map(entry -> {
                Especialidad esp = especialidadRepository.findById(entry.getKey()).orElse(null);
                if (esp == null) return null;

                long totalCitas = entry.getValue().size();
                long citasAtendidas = entry.getValue().stream()
                    .filter(c -> c.getEstado() == EstadoCita.atendida)
                    .count();
                
                BigDecimal ingresoTotal = esp.getPrecio()
                    .multiply(BigDecimal.valueOf(citasAtendidas));

                return new ReporteIngresoEspecialidadDTO(
                    esp.getNombre(),
                    totalCitas,
                    citasAtendidas,
                    esp.getPrecio(),
                    ingresoTotal
                );
            })
            .filter(Objects::nonNull)
            .sorted(Comparator.comparing(ReporteIngresoEspecialidadDTO::getIngresoTotal).reversed())
            .collect(Collectors.toList());
    }

    private List<ReporteIngresoMedicoDTO> calcularIngresosPorMedico(List<Cita> citas) {
        Map<Long, List<Cita>> citasPorMedico = citas.stream()
            .collect(Collectors.groupingBy(Cita::getIdMedico));

        return citasPorMedico.entrySet().stream()
            .map(entry -> {
                Medico medico = medicoRepository.findById(entry.getKey()).orElse(null);
                if (medico == null) return null;

                long totalCitas = entry.getValue().size();
                long citasAtendidas = entry.getValue().stream()
                    .filter(c -> c.getEstado() == EstadoCita.atendida)
                    .count();

                BigDecimal ingresoTotal = entry.getValue().stream()
                    .filter(c -> c.getEstado() == EstadoCita.atendida)
                    .map(c -> {
                        Especialidad esp = especialidadRepository
                            .findById(c.getIdEspecialidad()).orElse(null);
                        return esp != null ? esp.getPrecio() : BigDecimal.ZERO;
                    })
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

                List<String> especialidades = medicoEspecialidadRepository
                    .findById_IdMedico(entry.getKey()).stream()
                    .map(me -> me.getEspecialidad().getNombre())
                    .collect(Collectors.toList());

                return new ReporteIngresoMedicoDTO(
                    medico.getNombres() + " " + medico.getApellidos(),
                    medico.getDni(),
                    totalCitas,
                    citasAtendidas,
                    ingresoTotal,
                    especialidades
                );
            })
            .filter(Objects::nonNull)
            .sorted(Comparator.comparing(ReporteIngresoMedicoDTO::getIngresoTotal).reversed())
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ReporteAdicionalesDTO generarReporteAdicionales(LocalDate desde, LocalDate hasta, LocalDate fechaCorte) {
        if (desde.isAfter(hasta)) {
            throw new RuntimeException("La fecha de inicio no puede ser mayor que la fecha fin.");
        }
        List<Cita> citas = citaRepository.findByFechaBetween(desde, hasta);
        LocalDate corte = citas.stream()
            .filter(c -> Boolean.TRUE.equals(c.getEsAdicional()))
            .map(Cita::getFecha)
            .min(LocalDate::compareTo)
            .orElse(null);
        if (corte == null) {
            corte = (fechaCorte != null) ? fechaCorte
                : desde.plusDays(java.time.temporal.ChronoUnit.DAYS.between(desde, hasta) / 2);
        }
        if (corte.isBefore(desde)) corte = desde;
        if (corte.isAfter(hasta)) corte = hasta;
        final LocalDate corteFinal = corte;

        long diasTotales = Math.max(1, java.time.temporal.ChronoUnit.DAYS.between(desde, hasta) + 1);

        Map<Long, java.math.BigDecimal> precioPorEsp = new java.util.HashMap<>();
        java.util.function.Function<Long, java.math.BigDecimal> precioDe = idEsp ->
            precioPorEsp.computeIfAbsent(idEsp, id -> {
                Especialidad esp = especialidadRepository.findById(id).orElse(null);
                return esp != null && esp.getPrecio() != null ? esp.getPrecio() : java.math.BigDecimal.ZERO;
            });

        Map<Long, List<Cita>> porMedico = citas.stream()
            .collect(Collectors.groupingBy(Cita::getIdMedico));
        List<AdicionalPorMedicoDTO> medicos = porMedico.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .map(entry -> {
                long total = entry.getValue().size();
                long adic = entry.getValue().stream().filter(c -> Boolean.TRUE.equals(c.getEsAdicional())).count();
                java.math.BigDecimal ingAdic = entry.getValue().stream()
                    .filter(c -> c.getEstado() == EstadoCita.atendida && Boolean.TRUE.equals(c.getEsAdicional()))
                    .map(c -> precioDe.apply(c.getIdEspecialidad()))
                    .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
                java.math.BigDecimal ingNorm = entry.getValue().stream()
                    .filter(c -> c.getEstado() == EstadoCita.atendida && !Boolean.TRUE.equals(c.getEsAdicional()))
                    .map(c -> precioDe.apply(c.getIdEspecialidad()))
                    .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
                java.math.BigDecimal ingTotal = ingNorm.add(ingAdic);
                double porc = ingTotal.compareTo(java.math.BigDecimal.ZERO) > 0
                    ? redondear(ingAdic.doubleValue() / ingTotal.doubleValue() * 100) : 0.0;
                return new AdicionalPorMedicoDTO(
                    entry.getKey(), total, total - adic, adic,
                    redondear((double) adic / diasTotales),
                    ingNorm, ingAdic, ingTotal, porc
                );
            })
            .collect(Collectors.toList());

        Map<Long, List<Cita>> porEsp = citas.stream()
            .collect(Collectors.groupingBy(Cita::getIdEspecialidad));
        List<AdicionalPorEspecialidadDTO> especialidades = porEsp.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .map(entry -> {
                Especialidad esp = especialidadRepository.findById(entry.getKey()).orElse(null);
                long total = entry.getValue().size();
                long adic = entry.getValue().stream().filter(c -> Boolean.TRUE.equals(c.getEsAdicional())).count();
                java.math.BigDecimal ingAdic = entry.getValue().stream()
                    .filter(c -> c.getEstado() == EstadoCita.atendida && Boolean.TRUE.equals(c.getEsAdicional()))
                    .map(c -> precioDe.apply(c.getIdEspecialidad()))
                    .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
                java.math.BigDecimal ingNorm = entry.getValue().stream()
                    .filter(c -> c.getEstado() == EstadoCita.atendida && !Boolean.TRUE.equals(c.getEsAdicional()))
                    .map(c -> precioDe.apply(c.getIdEspecialidad()))
                    .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
                java.math.BigDecimal ingTotal = ingNorm.add(ingAdic);
                double porc = ingTotal.compareTo(java.math.BigDecimal.ZERO) > 0
                    ? redondear(ingAdic.doubleValue() / ingTotal.doubleValue() * 100) : 0.0;
                return new AdicionalPorEspecialidadDTO(
                    entry.getKey(),
                    esp != null ? esp.getNombre() : "Desconocida",
                    total, total - adic, adic,
                    redondear((double) adic / diasTotales),
                    ingNorm, ingAdic, ingTotal, porc
                );
            })
            .collect(Collectors.toList());

        LocalDate inicioAntes = corteFinal.minusDays(7).isBefore(desde) ? desde : corteFinal.minusDays(7);
        LocalDate finAntes = corteFinal.minusDays(1);
        LocalDate inicioDespues = corteFinal;
        LocalDate finDespues = corteFinal.plusDays(6).isAfter(hasta) ? hasta : corteFinal.plusDays(6);
        long diasAntes = finAntes.isBefore(inicioAntes) ? 0
            : java.time.temporal.ChronoUnit.DAYS.between(inicioAntes, finAntes) + 1;
        long diasDespues = finDespues.isBefore(inicioDespues) ? 0
            : java.time.temporal.ChronoUnit.DAYS.between(inicioDespues, finDespues) + 1;
        long citasAntes = citas.stream()
            .filter(c -> !c.getFecha().isBefore(inicioAntes) && !c.getFecha().isAfter(finAntes)).count();
        long citasDespues = citas.stream()
            .filter(c -> !c.getFecha().isBefore(inicioDespues) && !c.getFecha().isAfter(finDespues)).count();
        double promAntes = diasAntes > 0 ? redondear((double) citasAntes / diasAntes) : 0.0;
        double promDespues = diasDespues > 0 ? redondear((double) citasDespues / diasDespues) : 0.0;
        double incremento = promAntes > 0 ? redondear((promDespues - promAntes) / promAntes * 100)
            : (promDespues > 0 ? 100.0 : 0.0);

        long totalAdic = citas.stream().filter(c -> Boolean.TRUE.equals(c.getEsAdicional())).count();
        double porcCitasAdic = !citas.isEmpty()
            ? redondear((double) totalAdic / citas.size() * 100) : 0.0;
        java.math.BigDecimal ingTotalAdic = citas.stream()
            .filter(c -> c.getEstado() == EstadoCita.atendida && Boolean.TRUE.equals(c.getEsAdicional()))
            .map(c -> precioDe.apply(c.getIdEspecialidad()))
            .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        java.math.BigDecimal ingTotalNorm = citas.stream()
            .filter(c -> c.getEstado() == EstadoCita.atendida && !Boolean.TRUE.equals(c.getEsAdicional()))
            .map(c -> precioDe.apply(c.getIdEspecialidad()))
            .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        java.math.BigDecimal ingTotal = ingTotalNorm.add(ingTotalAdic);
        double porcIngresoAdic = ingTotal.compareTo(java.math.BigDecimal.ZERO) > 0
            ? redondear(ingTotalAdic.doubleValue() / ingTotal.doubleValue() * 100) : 0.0;
        return new ReporteAdicionalesDTO(
            desde, hasta, corteFinal,
            (long) citas.size(), (long) citas.size() - totalAdic, totalAdic,
            promAntes, promDespues, incremento, porcCitasAdic,
            ingTotalNorm, ingTotalAdic, ingTotal, porcIngresoAdic,
            medicos, especialidades
        );
    }

    private double redondear(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
}

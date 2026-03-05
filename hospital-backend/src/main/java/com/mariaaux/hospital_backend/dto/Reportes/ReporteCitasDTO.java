package com.mariaaux.hospital_backend.dto.Reportes;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReporteCitasDTO {
    private Long idCita;
    private LocalDate fecha;
    private LocalTime hora;
    private String nombrePaciente;
    private String dniPaciente;
    private String nombreMedico;
    private String especialidad;
    private String estado;
    private String motivoConsulta;
    private BigDecimal precio;
}

package com.mariaaux.hospital_backend.dto;

import com.mariaaux.hospital_backend.model.EstadoCita;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CitaDetalleDTO {
    private Long idCita;
    private LocalDate fecha;
    private LocalTime hora;
    private String motivoConsulta;
    private String sintomas;
    private EstadoCita estado;
    private LocalDateTime fechaCreacion;
    private Long idPaciente;
    private Long idMedico;
    private Long idEspecialidad;
    private String nombreMedico; 
    private String nombreEspecialidad; 
}
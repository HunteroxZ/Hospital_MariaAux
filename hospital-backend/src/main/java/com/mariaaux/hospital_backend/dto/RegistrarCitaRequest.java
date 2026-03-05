package com.mariaaux.hospital_backend.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.FutureOrPresent;

@Data
public class RegistrarCitaRequest {

    @NotNull(message = "El ID del paciente es obligatorio")
    private Long idPaciente;

    @NotNull(message = "El ID del médico es obligatorio")
    private Long idMedico;

    @NotNull(message = "El ID de la especialidad es obligatorio")
    private Long idEspecialidad;

    @NotNull(message = "La fecha de la cita es obligatoria")
    @FutureOrPresent(message = "La fecha de la cita no puede ser en el pasado")
    private LocalDate fecha;

    @NotNull(message = "La hora de la cita es obligatoria")
    private LocalTime hora;

    @NotNull(message = "El motivo de la consulta es obligatorio")
    private String motivoConsulta;

    @NotNull(message = "Los síntomas son obligatorios")
    private String sintomas;
}
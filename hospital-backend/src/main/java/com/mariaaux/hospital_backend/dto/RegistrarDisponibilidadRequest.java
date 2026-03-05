package com.mariaaux.hospital_backend.dto;

import com.mariaaux.hospital_backend.model.DiaSemana;
import lombok.Data;
import java.time.LocalTime;
import jakarta.validation.constraints.NotNull;

@Data
public class RegistrarDisponibilidadRequest {

    @NotNull(message = "El ID de la especialidad es obligatorio")
    private Long idEspecialidad;

    @NotNull(message = "El día de la semana es obligatorio")
    private DiaSemana diaSemana;

    @NotNull(message = "La hora de inicio es obligatoria")
    private LocalTime horaInicio;

    @NotNull(message = "La hora de fin es obligatoria")
    private LocalTime horaFin;
}
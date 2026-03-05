package com.mariaaux.hospital_backend.dto;

import com.mariaaux.hospital_backend.model.EstadoCita;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalTime;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class CitaMedicoDTO {
    private Long idCita;
    private LocalTime hora;
    private String nombrePaciente; 
    private String motivoConsulta;
    private EstadoCita estado;

    private Long idPaciente;
    private Long idEspecialidad;
}
package com.mariaaux.hospital_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HistoriaMedicaDTO {

    private Long idHistoriaMedica;
    private Long idCita;
    private LocalDate fechaConsulta;
    private String diagnostico;
    private String sintomas;
    private String nombreMedico;
    private String nombrePaciente; 
    private String especialidad;
    private Long idEspecialidad;
    

    private String antecedentes;
    private String antecedentesPersonales;
    private String antecedentesFamiliares;
    private String historiaEnfermedadActual;
    private String historiaPsicosocial;
}
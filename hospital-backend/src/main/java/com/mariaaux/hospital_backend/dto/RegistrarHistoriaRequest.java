package com.mariaaux.hospital_backend.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;

@Data
public class RegistrarHistoriaRequest {


    @NotNull
    private Long idPaciente;
    @NotNull
    private Long idCita;
    @NotNull
    private Long idMedico; 


    @NotEmpty(message = "Los síntomas son obligatorios.")
    private String sintomas;

    @NotEmpty(message = "El diagnóstico es obligatorio.")
    private String diagnostico;

    private String antecedentes;
    private String antecedentesPersonales;
    private String antecedentesFamiliares;
    private String historiaEnfermedadActual;
    private String historiaPsicosocial;
}
package com.mariaaux.hospital_backend.dto.Recepcionista;

import lombok.Data;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CitaPacienteRecepcionistaDTO {
    private Long idCita;
    private String nombrePaciente;
    private String dniPaciente;
    private String nombreMedico;
    private String especialidad;
    private String fecha;
    private String hora;
    private String motivoConsulta;
    private String estado;
    private BigDecimal precio;
    private Boolean tieneSIS;
}

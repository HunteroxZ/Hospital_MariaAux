package com.mariaaux.hospital_backend.dto;

import com.mariaaux.hospital_backend.model.EstadoMedico;
import lombok.Data;
import java.time.LocalDate;

@Data
public class ActualizarMedicoRequest {
    private String nombres;
    private String apellidos;
    private String correo;
    private String clave;
    private String direccion;
    private String telefono;
    private LocalDate fechaNacimiento;
    private EstadoMedico estado;
}
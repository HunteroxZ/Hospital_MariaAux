package com.mariaaux.hospital_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PacienteInfoDTO {
    private Long idPaciente;
    private String nombre;           
    private String dni;
    private String correoElectronico;
    private String telefono;
    private Long numeroCitas;       
}

package com.mariaaux.hospital_backend.dto; 

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {
    private Long idPaciente;
    private String nombres;
    private String apellidos;
    private String correo;
    private String sexo;
}
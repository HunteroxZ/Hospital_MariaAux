package com.mariaaux.hospital_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponseMedico {
    private Long idMedico;
    private String nombres;
    private String apellidos;
    private String correo;

}

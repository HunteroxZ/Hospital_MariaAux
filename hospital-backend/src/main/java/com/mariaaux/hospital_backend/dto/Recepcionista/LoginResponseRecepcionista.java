package com.mariaaux.hospital_backend.dto.Recepcionista;

import lombok.Data;
import lombok.AllArgsConstructor;


@Data
@AllArgsConstructor
public class LoginResponseRecepcionista {
    private Long idRecepcionista;
    private String nombres;
    private String apellidos;
    private String correo;
}

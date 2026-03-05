package com.mariaaux.hospital_backend.dto.Recepcionista;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistrarRecepcionistaRequest {
    
    @NotEmpty(message = "Los nombres son obligatorios.")
    private String nombres;

    @NotEmpty(message = "Los apellidos son obligatorios.")
    private String apellidos;

    @NotEmpty(message = "El DNI es obligatorio.")
    @Size(min = 8, max = 8, message = "El DNI debe tener 8 dígitos.")
    @Pattern(regexp = "[0-9]+", message = "El DNI debe contener solo números.")
    private String dni;

    @NotEmpty(message = "El correo es obligatorio.")
    @Email(message = "El formato del correo no es válido.")
    private String correo;

    @NotEmpty(message = "La clave es obligatoria.")
    private String clave;

    private String direccion;
    private String telefono;
}

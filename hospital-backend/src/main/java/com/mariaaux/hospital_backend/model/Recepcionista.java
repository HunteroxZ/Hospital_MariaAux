package com.mariaaux.hospital_backend.model;

import jakarta.persistence.*;
import lombok.Data;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Data
@Entity
@Table(name = "Recepcionista")
public class Recepcionista {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idRecepcionista;

    @NotEmpty(message = "Los nombres son obligatorios.")
    @Column(nullable = false, length = 100)
    private String nombres;

    @NotEmpty(message = "Los apellidos son obligatorios.")
    @Column(nullable = false, length = 100)
    private String apellidos;

    @NotEmpty(message = "El DNI es obligatorio.")
    @Size(min = 8, max = 8, message = "El DNI debe tener 8 dígitos.")
    @Pattern(regexp = "[0-9]+", message = "El DNI debe contener solo números.")
    @Column(nullable = false, unique = true, length = 8)
    private String dni;

    @NotEmpty(message = "El correo es obligatorio.")
    @Email(message = "El formato del correo no es válido.")
    @Column(nullable = false, unique = true, length = 100)
    private String correo;

    @JsonIgnore
    @NotEmpty(message = "La clave es obligatoria.")
    @Column(nullable = false, length = 255)
    private String clave;

    @Column(length = 200)
    private String direccion;

    @Column(length = 20)
    private String telefono;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private EstadoRecepcionista estado = EstadoRecepcionista.ACTIVO;
}
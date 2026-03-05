package com.mariaaux.hospital_backend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;

@Data
@Entity
@Table(name = "Especialidad")
public class Especialidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idEspecialidad;

    @NotEmpty(message = "El nombre de la especialidad no puede estar vacío.")
    @Column(nullable = false, unique = true, length = 100)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @NotNull(message = "El precio es obligatorio.")
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio debe ser mayor que cero.")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;
}
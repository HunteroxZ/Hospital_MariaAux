package com.mariaaux.hospital_backend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Entity
@Table(name = "CupoAdicional")
public class CupoAdicional {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCupoAdicional;

    @Column(nullable = false)
    private Long idMedico;

    @Column(nullable = false)
    private Long idEspecialidad;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false)
    private LocalTime horaInicio;

    @Column(nullable = false)
    private LocalTime horaFin;

    @Column(nullable = false)
    private Boolean disponible = true;
}

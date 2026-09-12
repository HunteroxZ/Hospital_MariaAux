package com.mariaaux.hospital_backend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;

@Data
@Entity
@Table(name = "Cita")
public class Cita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCita;

    @NotNull(message = "El ID del paciente es obligatorio")
    @Column(nullable = false)
    private Long idPaciente; 

    @NotNull(message = "El ID del médico es obligatorio")
    @Column(nullable = false)
    private Long idMedico; 

    @NotNull(message = "El ID de la especialidad es obligatorio")
    @Column(nullable = false)
    private Long idEspecialidad; 

    @NotNull(message = "La fecha de la cita es obligatoria")
    @Column(nullable = false)
    private LocalDate fecha;

    @NotNull(message = "La hora de la cita es obligatoria")
    @Column(nullable = false)
    private LocalTime hora;

    @NotNull(message = "El motivo de la consulta es obligatorio")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String motivoConsulta;

    @NotNull(message = "Los síntomas son obligatorios")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String sintomas;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private EstadoCita estado = EstadoCita.pendiente; 

    @Column(nullable = false)
    private Boolean esAdicional = false;

    @Column(name = "hora_fin_real")
    private LocalTime horaFinReal;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime fechaCreacion;

}
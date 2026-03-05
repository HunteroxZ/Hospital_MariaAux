package com.mariaaux.hospital_backend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalTime;
import jakarta.validation.constraints.NotNull;
import com.fasterxml.jackson.annotation.JsonIgnore; 

@Data
@Entity
@Table(name = "DisponibilidadMedico")
public class DisponibilidadMedico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idDisponibilidad;

    @NotNull(message = "El ID del médico es obligatorio")
    @Column(nullable = false)
    private Long idMedico;

    @NotNull(message = "El ID de la especialidad es obligatorio")
    @Column(nullable = false)
    private Long idEspecialidad;

    @NotNull(message = "El día de la semana es obligatorio")
    @Enumerated(EnumType.STRING) 
    @Column(nullable = false)
    private DiaSemana diaSemana; 

    @NotNull(message = "La hora de inicio es obligatoria")
    @Column(nullable = false)
    private LocalTime horaInicio; 

    @NotNull(message = "La hora de fin es obligatoria")
    @Column(nullable = false)
    private LocalTime horaFin; 

    @JsonIgnore 
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idMedico", insertable = false, updatable = false)
    private Medico medico;

    @JsonIgnore 
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idEspecialidad", insertable = false, updatable = false)
    private Especialidad especialidad;
}
package com.mariaaux.hospital_backend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;

@Data
@Entity
@Table(name = "HistoriaMedica") 
public class HistoriaMedica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idHistoriaMedica;

    @NotNull(message = "ID de Paciente no puede ser nulo")
    @Column(nullable = false)
    private Long idPaciente;

    @NotNull(message = "ID de Cita no puede ser nulo")
    @Column(nullable = false, unique = true) 
    private Long idCita;

    @NotNull(message = "ID de Médico no puede ser nulo")
    @Column(nullable = false)
    private Long idMedico;

    @NotEmpty(message = "Los síntomas son obligatorios.")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String sintomas;

    @NotEmpty(message = "El diagnóstico es obligatorio.")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String diagnostico;


    @Column(columnDefinition = "TEXT")
    private String antecedentes;

    @NotNull(message = "La fecha de consulta es obligatoria.")
    @Column(name = "fecha_consulta", nullable = false)
    private LocalDate fechaConsulta;


    @Column(name = "antecedentes_personales", columnDefinition = "TEXT")
    private String antecedentesPersonales;

    @Column(name = "antecedentes_familiares", columnDefinition = "TEXT")
    private String antecedentesFamiliares;

    @Column(name = "historia_enfermedad_actual", columnDefinition = "TEXT")
    private String historiaEnfermedadActual;

    @Column(name = "historia_psicosocial", columnDefinition = "TEXT")
    private String historiaPsicosocial;
}
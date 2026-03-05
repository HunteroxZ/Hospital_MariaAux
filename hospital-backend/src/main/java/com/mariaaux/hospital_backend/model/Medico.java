package com.mariaaux.hospital_backend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.Period;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Data
@Entity
@Table(name = "Medico")
public class Medico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idMedico;

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

    @NotEmpty(message = "El código de colegiatura es obligatorio.")
    @Column(nullable = false, unique = true, length = 20)
    private String codigoColegiatura;

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

    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private EstadoMedico estado = EstadoMedico.ACTIVO;


    @Transient
    public Integer getEdad() {
        if (this.fechaNacimiento == null) {
            return null;
        }
        
        LocalDate hoy = LocalDate.now();
        Period periodo = Period.between(this.fechaNacimiento, hoy);
        return periodo.getYears();
    }
    
}
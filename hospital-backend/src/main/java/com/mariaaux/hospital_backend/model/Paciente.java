package com.mariaaux.hospital_backend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.math.BigDecimal;
import org.hibernate.annotations.CreationTimestamp; 
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;

@Data
@Entity
@Table(name = "Paciente")
public class Paciente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idPaciente;

    @Column(unique = true, nullable = false, length = 8)
    private String dni;

    @Column(length = 11)
    private String ruc;

    @Column(nullable = false, length = 100)
    private String nombres;

    @Column(nullable = false, length = 100)
    private String apellidos;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Sexo sexo;

    @Email(message = "El formato del correo no es válido") 
    @NotEmpty(message = "El correo no puede estar vacío")
    @Column(unique = true, nullable = false, length = 100)
    private String correo;

    @Column(nullable = false, length = 255)
    private String clave;

    @Column(length = 200)
    private String direccion;

    @Column(length = 20)
    private String telefono;

    @Column(name = "fecha_nacimiento", nullable = false)
    private LocalDate fechaNacimiento;

    @Column(precision = 4, scale = 2)
    private BigDecimal talla; 

    @Column(precision = 5, scale = 2)
    private BigDecimal peso; 

    @Enumerated(EnumType.STRING) 
    @Column(length = 20, name = "estado_civil") 
    private EstadoCivil estadoCivil;

    @CreationTimestamp 
    @Column(name = "fecha_registro", updatable = false) 
    private LocalDateTime fechaRegistro;

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
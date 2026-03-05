package com.mariaaux.hospital_backend.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EspecialidadDTO {
    private Long idEspecialidad;
    private String nombre;
    private String descripcion;
    private BigDecimal precio;
}

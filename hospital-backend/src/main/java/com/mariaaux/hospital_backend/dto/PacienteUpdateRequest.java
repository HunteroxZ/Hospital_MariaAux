package com.mariaaux.hospital_backend.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class PacienteUpdateRequest {
    private String telefono;
    private String direccion;
    private BigDecimal talla;
    private BigDecimal peso;
    private String clave;
}
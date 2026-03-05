package com.mariaaux.hospital_backend.dto.Recepcionista;

import lombok.Data;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PagoRequest {
    private Long idCita;
    private String metodoPago;
    private BigDecimal montoPagado; 
    private String ruc;
    private String razonSocial;
    private String direccion;
    
    private Integer numeroCuotas;
    private String tipoTarjeta;
    
    private Boolean tieneSIS;
}

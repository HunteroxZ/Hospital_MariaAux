package com.mariaaux.hospital_backend.dto.Recepcionista;

import lombok.Data;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class PagoResponse {
    private boolean exito;
    private String mensaje;
    private String numeroComprobante;
    private java.time.LocalDateTime fechaHoraPago;
    private BigDecimal vuelto;
    private Boolean esPagoSIS;
}


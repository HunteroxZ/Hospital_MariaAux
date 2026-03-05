package com.mariaaux.hospital_backend.dto.Reportes;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReporteIngresoEspecialidadDTO {
    private String nombreEspecialidad;
    private Long totalCitas;
    private Long citasAtendidas;
    private BigDecimal precioUnitario;
    private BigDecimal ingresoTotal;
}

package com.mariaaux.hospital_backend.dto.Reportes;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReporteIngresoMedicoDTO {
    private String nombreMedico;
    private String dni;
    private Long totalCitas;
    private Long citasAtendidas;
    private BigDecimal ingresoTotal;
    private List<String> especialidades;
}

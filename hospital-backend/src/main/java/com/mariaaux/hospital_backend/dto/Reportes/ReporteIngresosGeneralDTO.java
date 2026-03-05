package com.mariaaux.hospital_backend.dto.Reportes;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReporteIngresosGeneralDTO {
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private Long totalCitas;
    private Long citasAtendidas;
    private BigDecimal ingresoTotal;
    private List<ReporteIngresoEspecialidadDTO> ingresosPorEspecialidad;
    private List<ReporteIngresoMedicoDTO> ingresosPorMedico;
}

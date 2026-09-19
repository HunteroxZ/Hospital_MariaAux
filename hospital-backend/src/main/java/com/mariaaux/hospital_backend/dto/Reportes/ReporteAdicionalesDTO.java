package com.mariaaux.hospital_backend.dto.Reportes;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReporteAdicionalesDTO {
    private LocalDate desde;
    private LocalDate hasta;
    private LocalDate fechaCorte;
    private Long totalCitas;
    private Long totalNormales;
    private Long totalAdicionales;
    private Double promedioDiarioAntes;
    private Double promedioDiarioDespues;
    private Double incrementoPorcentual;
    private Double porcentajeCitasAdicionales;
    private BigDecimal ingresoTotalNormales;
    private BigDecimal ingresoTotalAdicionales;
    private BigDecimal ingresoTotal;
    private Double porcentajeIngresoAdicional;
    private List<AdicionalPorMedicoDTO> porMedico;
    private List<AdicionalPorEspecialidadDTO> porEspecialidad;
}

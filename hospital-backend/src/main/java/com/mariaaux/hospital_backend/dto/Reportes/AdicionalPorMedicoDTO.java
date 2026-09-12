package com.mariaaux.hospital_backend.dto.Reportes;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdicionalPorMedicoDTO {
    private Long idMedico;
    private Long totalCitas;
    private Long citasNormales;
    private Long citasAdicionales;
    private Double promedioDiarioAdicionales;
}

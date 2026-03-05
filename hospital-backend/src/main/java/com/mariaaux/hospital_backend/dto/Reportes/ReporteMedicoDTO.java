package com.mariaaux.hospital_backend.dto.Reportes;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReporteMedicoDTO {
    private Long idMedico;
    private String nombreCompleto;
    private String dni;
    private String codigoColegiatura;
    private String correo;
    private String telefono;
    private String estado;
    private Long totalCitas;
    private List<String> especialidades;
}

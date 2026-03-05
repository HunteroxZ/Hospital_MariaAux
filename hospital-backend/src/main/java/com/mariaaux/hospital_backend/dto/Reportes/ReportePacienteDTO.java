package com.mariaaux.hospital_backend.dto.Reportes;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;



@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReportePacienteDTO {
    private Long idPaciente;
    private String nombreCompleto;
    private String dni;
    private String sexo;
    private String correo;
    private String telefono;
    private Long totalCitas;
    private LocalDate fechaRegistro;
}

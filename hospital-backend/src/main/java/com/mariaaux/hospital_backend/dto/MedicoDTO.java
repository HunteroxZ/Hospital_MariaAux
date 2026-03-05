package com.mariaaux.hospital_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data 
@NoArgsConstructor 
@AllArgsConstructor 
public class MedicoDTO {
    private Long idMedico;
    private String nombres;
    private String apellidos;
}
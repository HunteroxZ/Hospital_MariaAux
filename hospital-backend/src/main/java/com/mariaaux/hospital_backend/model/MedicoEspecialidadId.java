package com.mariaaux.hospital_backend.model;

import jakarta.persistence.Embeddable;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.util.Objects;

@Data 
@NoArgsConstructor 
@AllArgsConstructor 
@Embeddable 
public class MedicoEspecialidadId implements Serializable { 

    private Long idMedico;
    private Long idEspecialidad;
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MedicoEspecialidadId that = (MedicoEspecialidadId) o;
        return Objects.equals(idMedico, that.idMedico) &&
               Objects.equals(idEspecialidad, that.idEspecialidad);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idMedico, idEspecialidad);
    }
}
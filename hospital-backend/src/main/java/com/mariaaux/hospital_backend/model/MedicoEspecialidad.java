package com.mariaaux.hospital_backend.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "medico_especialidad")
public class MedicoEspecialidad {

    @EmbeddedId
    private MedicoEspecialidadId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idMedico")
    @JoinColumn(name = "id_medico")
    private Medico medico;


    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idEspecialidad")
    @JoinColumn(name = "id_especialidad")
    private Especialidad especialidad;

    public MedicoEspecialidad(Medico medico, Especialidad especialidad) {
        this.medico = medico;
        this.especialidad = especialidad;
        this.id = new MedicoEspecialidadId(medico.getIdMedico(), especialidad.getIdEspecialidad());
    }

    public MedicoEspecialidad() {}
}
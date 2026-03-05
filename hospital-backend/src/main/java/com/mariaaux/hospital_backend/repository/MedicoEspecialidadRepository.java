package com.mariaaux.hospital_backend.repository;

import com.mariaaux.hospital_backend.model.MedicoEspecialidad;
import com.mariaaux.hospital_backend.model.MedicoEspecialidadId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicoEspecialidadRepository extends JpaRepository<MedicoEspecialidad, MedicoEspecialidadId> {


    List<MedicoEspecialidad> findById_IdMedico(Long idMedico);


    @Query("SELECT me FROM MedicoEspecialidad me WHERE me.id.idMedico = :idMedico")
    List<MedicoEspecialidad> findByMedicoId(@Param("idMedico") Long idMedico);


    List<MedicoEspecialidad> findById_IdEspecialidad(Long idEspecialidad);
}
package com.mariaaux.hospital_backend.repository;

import com.mariaaux.hospital_backend.model.DisponibilidadMedico;
import com.mariaaux.hospital_backend.model.DiaSemana;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface DisponibilidadMedicoRepository extends JpaRepository<DisponibilidadMedico, Long> {


    List<DisponibilidadMedico> findByIdMedicoOrderByDiaSemanaAscHoraInicioAsc(Long idMedico);


    List<DisponibilidadMedico> findByIdMedicoAndIdEspecialidadOrderByDiaSemanaAscHoraInicioAsc(Long idMedico, Long idEspecialidad);


    void deleteByIdMedicoAndIdEspecialidad(Long idMedico, Long idEspecialidad);

    @Query("SELECT COUNT(d) > 0 FROM DisponibilidadMedico d " +
           "WHERE d.idMedico = :idMedico " +
           "AND d.diaSemana = :diaSemana " +
           "AND d.horaInicio < :horaFin " + 
           "AND d.horaFin > :horaInicio") 
    boolean existsOverlappingDisponibilidad(
            @Param("idMedico") Long idMedico,
            @Param("diaSemana") DiaSemana diaSemana,
            @Param("horaInicio") LocalTime horaInicio,
            @Param("horaFin") LocalTime horaFin
    );
}
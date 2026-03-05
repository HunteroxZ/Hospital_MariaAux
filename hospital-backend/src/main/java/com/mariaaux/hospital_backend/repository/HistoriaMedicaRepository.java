package com.mariaaux.hospital_backend.repository;

import com.mariaaux.hospital_backend.model.HistoriaMedica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface HistoriaMedicaRepository extends JpaRepository<HistoriaMedica, Long> {


    List<HistoriaMedica> findByIdPacienteOrderByFechaConsultaDesc(Long idPaciente);


    Optional<HistoriaMedica> findByIdCita(Long idCita);

    @Query("SELECT hm FROM HistoriaMedica hm JOIN Paciente p ON hm.idPaciente = p.idPaciente " +
       "WHERE p.dni = :dniPaciente AND hm.idMedico = :idMedico " +
       "ORDER BY hm.fechaConsulta DESC")
    List<HistoriaMedica> findHistorialByDniPacienteAndIdMedico(
        @Param("dniPaciente") String dniPaciente,
        @Param("idMedico") Long idMedico
);
}
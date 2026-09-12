package com.mariaaux.hospital_backend.repository;

import com.mariaaux.hospital_backend.model.CupoAdicional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface CupoAdicionalRepository extends JpaRepository<CupoAdicional, Long> {

    List<CupoAdicional> findByIdMedicoAndIdEspecialidadAndFechaAndDisponibleTrue(
        Long idMedico, Long idEspecialidad, LocalDate fecha);

    List<CupoAdicional> findByIdMedicoAndFecha(Long idMedico, LocalDate fecha);

    boolean existsByIdMedicoAndIdEspecialidadAndFechaAndHoraInicioAndDisponibleTrue(
        Long idMedico, Long idEspecialidad, LocalDate fecha, java.time.LocalTime horaInicio);
}

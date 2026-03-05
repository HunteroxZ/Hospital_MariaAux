package com.mariaaux.hospital_backend.repository;

import com.mariaaux.hospital_backend.model.Cita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {


    boolean existsByIdMedicoAndFechaAndHora(Long idMedico, LocalDate fecha, LocalTime hora);
    boolean existsByIdPacienteAndFechaAndHora(Long idPaciente, LocalDate fecha, LocalTime hora);

    List<Cita> findByIdPacienteOrderByFechaDescHoraDesc(Long idPaciente);
    List<Cita> findByIdMedicoAndFecha(Long idMedico, LocalDate fecha);

    boolean existsByIdPacienteAndFecha(Long idPaciente, LocalDate fecha);
}
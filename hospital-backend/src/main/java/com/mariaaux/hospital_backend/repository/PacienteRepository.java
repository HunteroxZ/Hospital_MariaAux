package com.mariaaux.hospital_backend.repository;

import com.mariaaux.hospital_backend.model.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PacienteRepository extends JpaRepository<Paciente, Long> {

    boolean existsByDni(String dni);

    boolean existsByCorreo(String correo);
    
    Optional<Paciente> findByDniAndClave(String dni, String clave);
    
    Optional<Paciente> findByDni(String dni);


    List<Paciente> findByDniStartingWith(String dni);
}
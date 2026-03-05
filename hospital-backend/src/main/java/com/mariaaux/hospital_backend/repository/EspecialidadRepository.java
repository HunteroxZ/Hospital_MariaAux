package com.mariaaux.hospital_backend.repository;

import com.mariaaux.hospital_backend.model.Especialidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EspecialidadRepository extends JpaRepository<Especialidad, Long> {
    boolean existsByNombre(String nombre);
}
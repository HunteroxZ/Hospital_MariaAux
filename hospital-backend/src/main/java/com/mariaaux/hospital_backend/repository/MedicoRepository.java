package com.mariaaux.hospital_backend.repository;

import com.mariaaux.hospital_backend.model.Medico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface MedicoRepository extends JpaRepository<Medico, Long> {


    boolean existsByDni(String dni);
    boolean existsByCodigoColegiatura(String codigoColegiatura);
    boolean existsByCorreo(String correo);


    Optional<Medico> findByDni(String dni);
}
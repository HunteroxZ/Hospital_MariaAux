package com.mariaaux.hospital_backend.repository;

import com.mariaaux.hospital_backend.model.Recepcionista;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface RecepcionistaRepository extends JpaRepository<Recepcionista, Long> {
    
    boolean existsByDni(String dni);
    
    boolean existsByCorreo(String correo);
    
    Optional<Recepcionista> findByDni(String dni);
}

package com.mariaaux.hospital_backend.repository;

import com.mariaaux.hospital_backend.model.Administrador;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdministradorRepository extends JpaRepository<Administrador, Long> {
    Optional<Administrador> findByDni(String dni);
}
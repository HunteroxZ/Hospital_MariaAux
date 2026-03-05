package com.mariaaux.hospital_backend.service;

import com.mariaaux.hospital_backend.model.Especialidad;
import com.mariaaux.hospital_backend.repository.EspecialidadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class EspecialidadService {

    @Autowired
    private EspecialidadRepository especialidadRepository;

    /**
     * 
     * @return
     */
    @Transactional(readOnly = true)
    public List<Especialidad> obtenerTodasLasEspecialidades() {
        return especialidadRepository.findAll();
    }

    /**
     * 
     * @param especialidad Los datos de la especialidad a crear.
     * @return La especialidad guardada con su nuevo ID.
     */
    @Transactional
    public Especialidad crearEspecialidad(Especialidad especialidad) {
        if (especialidadRepository.existsByNombre(especialidad.getNombre())) {
            throw new RuntimeException("Ya existe una especialidad con el nombre: " + especialidad.getNombre());
        }
        return especialidadRepository.save(especialidad);
    }
    
    /**
     * 
     * @param id
     * @return
     */
    @Transactional(readOnly = true)
    public Optional<Especialidad> obtenerEspecialidadPorId(Long id) {
        return especialidadRepository.findById(id);
    }

    /**
     * 
     * @param id 
     * @param datosActualizados
     * @return
     */
    @Transactional
    public Especialidad actualizarEspecialidad(Long id, Especialidad datosActualizados) {
        Especialidad existente = especialidadRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Especialidad no encontrada con ID: " + id));

        
        if (!existente.getNombre().equalsIgnoreCase(datosActualizados.getNombre()) &&
            especialidadRepository.existsByNombre(datosActualizados.getNombre())) {
             throw new RuntimeException("Ya existe otra especialidad con el nombre: " + datosActualizados.getNombre());
        }

        existente.setNombre(datosActualizados.getNombre());
        existente.setDescripcion(datosActualizados.getDescripcion());
        existente.setPrecio(datosActualizados.getPrecio());

        return especialidadRepository.save(existente);
    }

    /**
     * 
     * @param id 
     */
    @Transactional
    public void eliminarEspecialidad(Long id) {
        if (!especialidadRepository.existsById(id)) {
            throw new RuntimeException("Especialidad no encontrada con ID: " + id);
        }
        especialidadRepository.deleteById(id);
    }
}
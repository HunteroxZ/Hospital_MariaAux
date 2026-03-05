package com.mariaaux.hospital_backend.Controller;

import com.mariaaux.hospital_backend.dto.DashboardDTO;
import com.mariaaux.hospital_backend.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/dashboard")
@CrossOrigin(origins = "*")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/estadisticas")
    public ResponseEntity<?> obtenerEstadisticas() {
        try {
            DashboardDTO estadisticas = dashboardService.obtenerEstadisticas();
            return ResponseEntity.ok(estadisticas);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error al obtener estadísticas: " + e.getMessage()));
        }
    }

    @GetMapping("/estadisticas/rango")
    public ResponseEntity<?> obtenerEstadisticasConRango(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        try {
            
            if (fechaInicio.isAfter(fechaFin)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "La fecha de inicio no puede ser posterior a la fecha fin"));
            }
            
            
            if (fechaInicio.plusDays(30).isBefore(fechaFin)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "El rango de fechas no puede ser mayor a 30 días"));
            }
            
            DashboardDTO estadisticas = dashboardService.obtenerEstadisticasConRango(fechaInicio, fechaFin);
            return ResponseEntity.ok(estadisticas);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error al obtener estadísticas: " + e.getMessage()));
        }
    }
}

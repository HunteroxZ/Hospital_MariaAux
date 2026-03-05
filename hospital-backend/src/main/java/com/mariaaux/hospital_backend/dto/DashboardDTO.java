package com.mariaaux.hospital_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DashboardDTO {
    
    private Long totalPacientes;
    private Long totalMedicos;
    private Long totalEspecialidades;
    private Long totalCitasHoy;

    private Long citasPendientes;
    private Long citasConfirmadas;
    private Long citasAtendidas;
    private Long citasCanceladas;
    private Long citasNoPresentadas;
    
    private List<EspecialidadStats> topEspecialidades;
    private List<MedicoStats> topMedicos;
    private Map<String, Long> citasPorEstado;
    private List<CitasPorDia> citasPorDia;
    private BigDecimal ingresosEstimados;
    
    private List<Notificacion> notificaciones;
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class EspecialidadStats {
        private String nombreEspecialidad;
        private Long numeroCitas;
        private BigDecimal ingresoTotal;
    }
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MedicoStats {
        private String nombreCompleto;
        private Long numeroCitas;
        private String especialidadPrincipal;
    }
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CitasPorDia {
        private String fecha;
        private Long numeroCitas;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Notificacion {
        private String tipo;
        private String mensaje;
        private String color; 
    }
}

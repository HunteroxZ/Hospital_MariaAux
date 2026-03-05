package com.mariaaux.hospital_backend.Controller;

import com.mariaaux.hospital_backend.dto.Reportes.*;
import com.mariaaux.hospital_backend.service.ReporteService;
import com.mariaaux.hospital_backend.service.PDFService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/reportes")
@CrossOrigin(origins = "*")
public class ReporteController {

    @Autowired
    private ReporteService reporteService;

    @Autowired
    private PDFService pdfService;

    @GetMapping("/citas")
    public ResponseEntity<?> obtenerReporteCitas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam(required = false) Long idEspecialidad,
            @RequestParam(required = false) Long idMedico,
            @RequestParam(required = false) String estado) {
        try {
            List<ReporteCitasDTO> reporte = reporteService.generarReporteCitas(
                fechaInicio, fechaFin, idEspecialidad, idMedico, estado);
            return ResponseEntity.ok(reporte);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error al generar reporte: " + e.getMessage()));
        }
    }

    @GetMapping("/citas/pdf")
    public ResponseEntity<byte[]> exportarReporteCitasPDF(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam(required = false) Long idEspecialidad,
            @RequestParam(required = false) Long idMedico,
            @RequestParam(required = false) String estado) {
        try {
            List<ReporteCitasDTO> datos = reporteService.generarReporteCitas(
                fechaInicio, fechaFin, idEspecialidad, idMedico, estado);
            
            byte[] pdfBytes = pdfService.generarPDFReporteCitas(datos, fechaInicio, fechaFin);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(ContentDisposition.builder("attachment")
                .filename("reporte_citas_" + LocalDate.now() + ".pdf")
                .build());
            
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/ingresos")
    public ResponseEntity<?> obtenerReporteIngresos(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        try {
            ReporteIngresosGeneralDTO reporte = reporteService.generarReporteIngresos(
                fechaInicio, fechaFin);
            return ResponseEntity.ok(reporte);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error al generar reporte: " + e.getMessage()));
        }
    }


    @GetMapping("/ingresos/pdf")
    public ResponseEntity<byte[]> exportarReporteIngresosPDF(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        try {
            ReporteIngresosGeneralDTO datos = reporteService.generarReporteIngresos(
                fechaInicio, fechaFin);
            
            byte[] pdfBytes = pdfService.generarPDFReporteIngresos(datos);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(ContentDisposition.builder("attachment")
                .filename("reporte_ingresos_" + LocalDate.now() + ".pdf")
                .build());
            
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/pacientes")
    public ResponseEntity<?> obtenerReportePacientes() {
        try {
            List<ReportePacienteDTO> reporte = reporteService.generarReportePacientes();
            return ResponseEntity.ok(reporte);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error al generar reporte: " + e.getMessage()));
        }
    }

    @GetMapping("/pacientes/pdf")
    public ResponseEntity<byte[]> exportarReportePacientesPDF() {
        try {
            List<ReportePacienteDTO> datos = reporteService.generarReportePacientes();
            byte[] pdfBytes = pdfService.generarPDFReportePacientes(datos);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(ContentDisposition.builder("attachment")
                .filename("reporte_pacientes_" + LocalDate.now() + ".pdf")
                .build());
            
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/medicos")
    public ResponseEntity<?> obtenerReporteMedicos() {
        try {
            List<ReporteMedicoDTO> reporte = reporteService.generarReporteMedicos();
            return ResponseEntity.ok(reporte);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error al generar reporte: " + e.getMessage()));
        }
    }

    @GetMapping("/medicos/pdf")
    public ResponseEntity<byte[]> exportarReporteMedicosPDF() {
        try {
            List<ReporteMedicoDTO> datos = reporteService.generarReporteMedicos();
            byte[] pdfBytes = pdfService.generarPDFReporteMedicos(datos);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(ContentDisposition.builder("attachment")
                .filename("reporte_medicos_" + LocalDate.now() + ".pdf")
                .build());
            
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
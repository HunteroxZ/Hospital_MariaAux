package com.mariaaux.hospital_backend.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.mariaaux.hospital_backend.dto.Reportes.ReporteCitasDTO;
import com.mariaaux.hospital_backend.dto.Reportes.ReporteIngresoEspecialidadDTO;
import com.mariaaux.hospital_backend.dto.Reportes.ReporteIngresoMedicoDTO;
import com.mariaaux.hospital_backend.dto.Reportes.ReporteIngresosGeneralDTO;
import com.mariaaux.hospital_backend.dto.Reportes.ReporteMedicoDTO;
import com.mariaaux.hospital_backend.dto.Reportes.ReportePacienteDTO;

import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class PDFService {

    private static final Font FONT_TITLE = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
    private static final Font FONT_SUBTITLE = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
    private static final Font FONT_NORMAL = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL);
    private static final Font FONT_SMALL = new Font(Font.FontFamily.HELVETICA, 8, Font.NORMAL);


    public byte[] generarPDFReporteCitas(List<ReporteCitasDTO> citas, LocalDate fechaInicio, LocalDate fechaFin) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4.rotate());
        PdfWriter.getInstance(document, baos);
        
        document.open();
        

        Paragraph title = new Paragraph("REPORTE DE CITAS", FONT_TITLE);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(10);
        document.add(title);
        

        Paragraph subtitle = new Paragraph(
            "Desde: " + fechaInicio.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) +
            " - Hasta: " + fechaFin.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
            FONT_SUBTITLE
        );
        subtitle.setAlignment(Element.ALIGN_CENTER);
        subtitle.setSpacingAfter(15);
        document.add(subtitle);
        

        PdfPTable table = new PdfPTable(9);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1, 1.5f, 2f, 1.5f, 2f, 2f, 1.5f, 2f, 1.5f});
        

        addTableHeader(table, "ID", "Fecha", "Paciente", "DNI", "Médico", "Especialidad", "Estado", "Motivo", "Precio");
        

        for (ReporteCitasDTO cita : citas) {
            addTableCell(table, cita.getIdCita().toString());
            addTableCell(table, cita.getFecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            addTableCell(table, cita.getNombrePaciente());
            addTableCell(table, cita.getDniPaciente());
            addTableCell(table, cita.getNombreMedico());
            addTableCell(table, cita.getEspecialidad());
            addTableCell(table, cita.getEstado());
            addTableCell(table, truncate(cita.getMotivoConsulta(), 30));
            addTableCell(table, "S/ " + cita.getPrecio());
        }
        
        document.add(table);
        

        Paragraph total = new Paragraph("Total de citas: " + citas.size(), FONT_SUBTITLE);
        total.setSpacingBefore(10);
        document.add(total);
        
        document.close();
        return baos.toByteArray();
    }

    public byte[] generarPDFReporteIngresos(ReporteIngresosGeneralDTO datos) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, baos);
        
        document.open();
        

        Paragraph title = new Paragraph("REPORTE DE INGRESOS", FONT_TITLE);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(10);
        document.add(title);
        

        Paragraph subtitle = new Paragraph(
            "Desde: " + datos.getFechaInicio().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) +
            " - Hasta: " + datos.getFechaFin().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
            FONT_SUBTITLE
        );
        subtitle.setAlignment(Element.ALIGN_CENTER);
        subtitle.setSpacingAfter(15);
        document.add(subtitle);
        

        Paragraph resumen = new Paragraph("RESUMEN GENERAL", FONT_SUBTITLE);
        resumen.setSpacingBefore(10);
        resumen.setSpacingAfter(5);
        document.add(resumen);
        
        document.add(new Paragraph("Total de citas: " + datos.getTotalCitas(), FONT_NORMAL));
        document.add(new Paragraph("Citas atendidas: " + datos.getCitasAtendidas(), FONT_NORMAL));
        document.add(new Paragraph("Ingreso total: S/ " + datos.getIngresoTotal(), FONT_NORMAL));
        

        Paragraph tituloEsp = new Paragraph("INGRESOS POR ESPECIALIDAD", FONT_SUBTITLE);
        tituloEsp.setSpacingBefore(15);
        tituloEsp.setSpacingAfter(10);
        document.add(tituloEsp);
        
        PdfPTable tableEsp = new PdfPTable(5);
        tableEsp.setWidthPercentage(100);
        addTableHeader(tableEsp, "Especialidad", "Total Citas", "Atendidas", "Precio Unit.", "Ingreso Total");
        
        for (ReporteIngresoEspecialidadDTO esp : datos.getIngresosPorEspecialidad()) {
            addTableCell(tableEsp, esp.getNombreEspecialidad());
            addTableCell(tableEsp, esp.getTotalCitas().toString());
            addTableCell(tableEsp, esp.getCitasAtendidas().toString());
            addTableCell(tableEsp, "S/ " + esp.getPrecioUnitario());
            addTableCell(tableEsp, "S/ " + esp.getIngresoTotal());
        }
        
        document.add(tableEsp);
        

        Paragraph tituloMed = new Paragraph("INGRESOS POR MÉDICO", FONT_SUBTITLE);
        tituloMed.setSpacingBefore(15);
        tituloMed.setSpacingAfter(10);
        document.add(tituloMed);
        
        PdfPTable tableMed = new PdfPTable(5);
        tableMed.setWidthPercentage(100);
        addTableHeader(tableMed, "Médico", "DNI", "Total Citas", "Atendidas", "Ingreso Total");
        
        for (ReporteIngresoMedicoDTO med : datos.getIngresosPorMedico()) {
            addTableCell(tableMed, med.getNombreMedico());
            addTableCell(tableMed, med.getDni());
            addTableCell(tableMed, med.getTotalCitas().toString());
            addTableCell(tableMed, med.getCitasAtendidas().toString());
            addTableCell(tableMed, "S/ " + med.getIngresoTotal());
        }
        
        document.add(tableMed);
        
        document.close();
        return baos.toByteArray();
    }

    public byte[] generarPDFReportePacientes(List<ReportePacienteDTO> pacientes) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4.rotate());
        PdfWriter.getInstance(document, baos);
        
        document.open();
        
        Paragraph title = new Paragraph("REPORTE DE PACIENTES", FONT_TITLE);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(15);
        document.add(title);
        
        PdfPTable table = new PdfPTable(8);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1, 3f, 1.5f, 1, 1, 2f, 1.5f, 1.5f});
        
        addTableHeader(table, "ID", "Nombre", "DNI", "Sexo", "Correo", "Teléfono", "Total Citas");
        
        for (ReportePacienteDTO p : pacientes) {
            addTableCell(table, p.getIdPaciente().toString());
            addTableCell(table, p.getNombreCompleto());
            addTableCell(table, p.getDni());
            addTableCell(table, p.getSexo());
            addTableCell(table, p.getCorreo());
            addTableCell(table, p.getTelefono());
            addTableCell(table, p.getTotalCitas().toString());
        }
        
        document.add(table);
        
        Paragraph total = new Paragraph("Total de pacientes: " + pacientes.size(), FONT_SUBTITLE);
        total.setSpacingBefore(10);
        document.add(total);
        
        document.close();
        return baos.toByteArray();
    }


    public byte[] generarPDFReporteMedicos(List<ReporteMedicoDTO> medicos) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4.rotate());
        PdfWriter.getInstance(document, baos);
        
        document.open();
        
        Paragraph title = new Paragraph("REPORTE DE MÉDICOS", FONT_TITLE);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(15);
        document.add(title);
        
        PdfPTable table = new PdfPTable(7);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1, 3f, 1.5f, 1.5f, 2.5f, 1.5f, 3f});
        
        addTableHeader(table, "ID", "Nombre", "DNI", "CMP", "Correo", "Total Citas", "Especialidades");
        
        for (ReporteMedicoDTO m : medicos) {
            addTableCell(table, m.getIdMedico().toString());
            addTableCell(table, m.getNombreCompleto());
            addTableCell(table, m.getDni());
            addTableCell(table, m.getCodigoColegiatura());
            addTableCell(table, m.getCorreo());
            addTableCell(table, m.getTotalCitas().toString());
            addTableCell(table, String.join(", ", m.getEspecialidades()));
        }
        
        document.add(table);
        
        Paragraph total = new Paragraph("Total de médicos: " + medicos.size(), FONT_SUBTITLE);
        total.setSpacingBefore(10);
        document.add(total);
        
        document.close();
        return baos.toByteArray();
    }


    private void addTableHeader(PdfPTable table, String... headers) {
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, FONT_SUBTITLE));
            cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(5);
            table.addCell(cell);
        }
    }
    
    private void addTableCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, FONT_SMALL));
        cell.setPadding(3);
        table.addCell(cell);
    }
    
    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        return text.length() > maxLength ? text.substring(0, maxLength) + "..." : text;
    }
}

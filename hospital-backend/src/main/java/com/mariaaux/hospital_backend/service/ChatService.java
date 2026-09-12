package com.mariaaux.hospital_backend.service;

import com.mariaaux.hospital_backend.dto.ChatResponse;
import com.mariaaux.hospital_backend.dto.RegistrarCitaRequest;
import com.mariaaux.hospital_backend.model.*;
import com.mariaaux.hospital_backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.text.Normalizer;

@Service
public class ChatService {

    @Autowired private EspecialidadService especialidadService;
    @Autowired private PacienteRepository pacienteRepository;
    @Autowired private MedicoRepository medicoRepository;
    @Autowired private DisponibilidadMedicoRepository disponibilidadMedicoRepository;
    @Autowired private CitaService citaService;
    @Autowired private CitaRepository citaRepository;
    @Autowired private CupoAdicionalRepository cupoAdicionalRepository;

    @Value("${groq.api.key:}") private String groqApiKey;
    @Value("${groq.api.url:https://api.groq.com/openai/v1/chat/completions}") private String groqApiUrl;
    private final RestTemplate restTemplate = new RestTemplate();

    private final Map<Long, Map<String, Object>> conversaciones = new ConcurrentHashMap<>();
    private static final String SALIR = "\n\nEscriba \"salir\" si desea acabar la conversacion.";

    private enum Paso {
        INICIO, ESPERANDO_OPCION, SELECCIONANDO_ESPECIALIDAD,
        SELECCIONANDO_MEDICO, SELECCIONANDO_FECHA, SELECCIONANDO_HORA,
        INGRESANDO_MOTIVO, INGRESANDO_SINTOMAS, CONFIRMANDO, VER_CITAS, EXPLICANDO_PAGOS,
        SUGERIENDO_ESPECIALIDAD, DIAGNOSTICANDO, ESPERANDO_DECISION_DIAGNOSTICO
    }

    private static final Map<String, String> SINTOMAS_A_ESPECIALIDAD = new HashMap<>();
    static {
        // Neumología
        SINTOMAS_A_ESPECIALIDAD.put("asma", "Neumología");
        SINTOMAS_A_ESPECIALIDAD.put("tos", "Neumología");
        SINTOMAS_A_ESPECIALIDAD.put("dificultad para respirar", "Neumología");
        SINTOMAS_A_ESPECIALIDAD.put("dificultad respirar", "Neumología");
        SINTOMAS_A_ESPECIALIDAD.put("le cuesta respirar", "Neumología");
        SINTOMAS_A_ESPECIALIDAD.put("no puede respirar", "Neumología");
        SINTOMAS_A_ESPECIALIDAD.put("falta de aire", "Neumología");
        SINTOMAS_A_ESPECIALIDAD.put("le falta el aire", "Neumología");
        SINTOMAS_A_ESPECIALIDAD.put("silbido", "Neumología");
        SINTOMAS_A_ESPECIALIDAD.put("bronquitis", "Neumología");
        SINTOMAS_A_ESPECIALIDAD.put("neumonía", "Neumología");
        SINTOMAS_A_ESPECIALIDAD.put("neumonia", "Neumología");
        SINTOMAS_A_ESPECIALIDAD.put("pulmón", "Neumología");
        SINTOMAS_A_ESPECIALIDAD.put("pulmon", "Neumología");
        SINTOMAS_A_ESPECIALIDAD.put("respirar", "Neumología");

        // Cardiología
        SINTOMAS_A_ESPECIALIDAD.put("dolor en el pecho", "Cardiología");
        SINTOMAS_A_ESPECIALIDAD.put("dolor de pecho", "Cardiología");
        SINTOMAS_A_ESPECIALIDAD.put("dolor pecho", "Cardiología");
        SINTOMAS_A_ESPECIALIDAD.put("dolor torácico", "Cardiología");
        SINTOMAS_A_ESPECIALIDAD.put("dolor toracico", "Cardiología");
        SINTOMAS_A_ESPECIALIDAD.put("palpitaciones", "Cardiología");
        SINTOMAS_A_ESPECIALIDAD.put("palpitacion", "Cardiología");
        SINTOMAS_A_ESPECIALIDAD.put("corazón", "Cardiología");
        SINTOMAS_A_ESPECIALIDAD.put("corazon", "Cardiología");
        SINTOMAS_A_ESPECIALIDAD.put("presión alta", "Cardiología");
        SINTOMAS_A_ESPECIALIDAD.put("presion alta", "Cardiología");
        SINTOMAS_A_ESPECIALIDAD.put("hipertensión", "Cardiología");
        SINTOMAS_A_ESPECIALIDAD.put("hipertension", "Cardiología");
        SINTOMAS_A_ESPECIALIDAD.put("taquicardia", "Cardiología");
        SINTOMAS_A_ESPECIALIDAD.put("arritmia", "Cardiología");
        SINTOMAS_A_ESPECIALIDAD.put("mareo", "Cardiología");
        SINTOMAS_A_ESPECIALIDAD.put("mareos", "Cardiología");
        SINTOMAS_A_ESPECIALIDAD.put("desmayo", "Cardiología");
        SINTOMAS_A_ESPECIALIDAD.put("se desmaya", "Cardiología");

        // Neurología
        SINTOMAS_A_ESPECIALIDAD.put("dolor de cabeza", "Neurología");
        SINTOMAS_A_ESPECIALIDAD.put("dolor cabeza", "Neurología");
        SINTOMAS_A_ESPECIALIDAD.put("dolor en la cabeza", "Neurología");
        SINTOMAS_A_ESPECIALIDAD.put("dolor de la cabeza", "Neurología");
        SINTOMAS_A_ESPECIALIDAD.put("me duele la cabeza", "Neurología");
        SINTOMAS_A_ESPECIALIDAD.put("migraña", "Neurología");
        SINTOMAS_A_ESPECIALIDAD.put("migrana", "Neurología");
        SINTOMAS_A_ESPECIALIDAD.put("convulsión", "Neurología");
        SINTOMAS_A_ESPECIALIDAD.put("convulsion", "Neurología");
        SINTOMAS_A_ESPECIALIDAD.put("epilepsia", "Neurología");
        SINTOMAS_A_ESPECIALIDAD.put("adormecimiento", "Neurología");
        SINTOMAS_A_ESPECIALIDAD.put("se duerme", "Neurología");
        SINTOMAS_A_ESPECIALIDAD.put("hormigueo", "Neurología");
        SINTOMAS_A_ESPECIALIDAD.put("vértigo", "Neurología");
        SINTOMAS_A_ESPECIALIDAD.put("vertigo", "Neurología");
        SINTOMAS_A_ESPECIALIDAD.put("temblor", "Neurología");
        SINTOMAS_A_ESPECIALIDAD.put("tiembla", "Neurología");
        SINTOMAS_A_ESPECIALIDAD.put("parálisis", "Neurología");
        SINTOMAS_A_ESPECIALIDAD.put("paralisis", "Neurología");

        // Gastroenterología
        SINTOMAS_A_ESPECIALIDAD.put("dolor abdominal", "Gastroenterología");
        SINTOMAS_A_ESPECIALIDAD.put("dolor en el abdomen", "Gastroenterología");
        SINTOMAS_A_ESPECIALIDAD.put("dolor de barriga", "Gastroenterología");
        SINTOMAS_A_ESPECIALIDAD.put("dolor barriga", "Gastroenterología");
        SINTOMAS_A_ESPECIALIDAD.put("me duele la barriga", "Gastroenterología");
        SINTOMAS_A_ESPECIALIDAD.put("estómago", "Gastroenterología");
        SINTOMAS_A_ESPECIALIDAD.put("estomago", "Gastroenterología");
        SINTOMAS_A_ESPECIALIDAD.put("dolor de estómago", "Gastroenterología");
        SINTOMAS_A_ESPECIALIDAD.put("dolor de estomago", "Gastroenterología");
        SINTOMAS_A_ESPECIALIDAD.put("dolor en el estómago", "Gastroenterología");
        SINTOMAS_A_ESPECIALIDAD.put("dolor en el estomago", "Gastroenterología");
        SINTOMAS_A_ESPECIALIDAD.put("me duele el estómago", "Gastroenterología");
        SINTOMAS_A_ESPECIALIDAD.put("me duele el estomago", "Gastroenterología");
        SINTOMAS_A_ESPECIALIDAD.put("náusea", "Gastroenterología");
        SINTOMAS_A_ESPECIALIDAD.put("nausea", "Gastroenterología");
        SINTOMAS_A_ESPECIALIDAD.put("náuseas", "Gastroenterología");
        SINTOMAS_A_ESPECIALIDAD.put("nauseas", "Gastroenterología");
        SINTOMAS_A_ESPECIALIDAD.put("vómito", "Gastroenterología");
        SINTOMAS_A_ESPECIALIDAD.put("vomito", "Gastroenterología");
        SINTOMAS_A_ESPECIALIDAD.put("vomita", "Gastroenterología");
        SINTOMAS_A_ESPECIALIDAD.put("diarrea", "Gastroenterología");
        SINTOMAS_A_ESPECIALIDAD.put("estreñimiento", "Gastroenterología");
        SINTOMAS_A_ESPECIALIDAD.put("estrenimiento", "Gastroenterología");
        SINTOMAS_A_ESPECIALIDAD.put("acidez", "Gastroenterología");
        SINTOMAS_A_ESPECIALIDAD.put("reflujo", "Gastroenterología");
        SINTOMAS_A_ESPECIALIDAD.put("hígado", "Gastroenterología");
        SINTOMAS_A_ESPECIALIDAD.put("higado", "Gastroenterología");
        SINTOMAS_A_ESPECIALIDAD.put("vesícula", "Gastroenterología");
        SINTOMAS_A_ESPECIALIDAD.put("vesicula", "Gastroenterología");

        // Traumatología
        SINTOMAS_A_ESPECIALIDAD.put("fractura", "Traumatología");
        SINTOMAS_A_ESPECIALIDAD.put("hueso", "Traumatología");
        SINTOMAS_A_ESPECIALIDAD.put("espalda", "Traumatología");
        SINTOMAS_A_ESPECIALIDAD.put("dolor de espalda", "Traumatología");
        SINTOMAS_A_ESPECIALIDAD.put("dolor en la espalda", "Traumatología");
        SINTOMAS_A_ESPECIALIDAD.put("me duele la espalda", "Traumatología");
        SINTOMAS_A_ESPECIALIDAD.put("columna", "Traumatología");
        SINTOMAS_A_ESPECIALIDAD.put("rodilla", "Traumatología");
        SINTOMAS_A_ESPECIALIDAD.put("dolor de rodilla", "Traumatología");
        SINTOMAS_A_ESPECIALIDAD.put("dolor en la rodilla", "Traumatología");
        SINTOMAS_A_ESPECIALIDAD.put("me duele la rodilla", "Traumatología");
        SINTOMAS_A_ESPECIALIDAD.put("articulación", "Traumatología");
        SINTOMAS_A_ESPECIALIDAD.put("articulacion", "Traumatología");
        SINTOMAS_A_ESPECIALIDAD.put("artritis", "Traumatología");
        SINTOMAS_A_ESPECIALIDAD.put("lumbalgia", "Traumatología");
        SINTOMAS_A_ESPECIALIDAD.put("cervicalgia", "Traumatología");
        SINTOMAS_A_ESPECIALIDAD.put("muscular", "Traumatología");
        SINTOMAS_A_ESPECIALIDAD.put("musculo", "Traumatología");
        SINTOMAS_A_ESPECIALIDAD.put("músculo", "Traumatología");

        // Dermatología
        SINTOMAS_A_ESPECIALIDAD.put("piel", "Dermatología");
        SINTOMAS_A_ESPECIALIDAD.put("roncha", "Dermatología");
        SINTOMAS_A_ESPECIALIDAD.put("ronchas", "Dermatología");
        SINTOMAS_A_ESPECIALIDAD.put("picazón", "Dermatología");
        SINTOMAS_A_ESPECIALIDAD.put("picazon", "Dermatología");
        SINTOMAS_A_ESPECIALIDAD.put("me pica", "Dermatología");
        SINTOMAS_A_ESPECIALIDAD.put("acné", "Dermatología");
        SINTOMAS_A_ESPECIALIDAD.put("acne", "Dermatología");
        SINTOMAS_A_ESPECIALIDAD.put("eccema", "Dermatología");
        SINTOMAS_A_ESPECIALIDAD.put("dermatitis", "Dermatología");
        SINTOMAS_A_ESPECIALIDAD.put("úlcera", "Dermatología");
        SINTOMAS_A_ESPECIALIDAD.put("ulcera", "Dermatología");
        SINTOMAS_A_ESPECIALIDAD.put("herida", "Dermatología");

        // Oftalmología
        SINTOMAS_A_ESPECIALIDAD.put("ojo", "Oftalmología");
        SINTOMAS_A_ESPECIALIDAD.put("ojos", "Oftalmología");
        SINTOMAS_A_ESPECIALIDAD.put("dolor de ojo", "Oftalmología");
        SINTOMAS_A_ESPECIALIDAD.put("dolor en el ojo", "Oftalmología");
        SINTOMAS_A_ESPECIALIDAD.put("me duele el ojo", "Oftalmología");
        SINTOMAS_A_ESPECIALIDAD.put("vista", "Oftalmología");
        SINTOMAS_A_ESPECIALIDAD.put("visión", "Oftalmología");
        SINTOMAS_A_ESPECIALIDAD.put("vision", "Oftalmología");
        SINTOMAS_A_ESPECIALIDAD.put("no veo bien", "Oftalmología");
        SINTOMAS_A_ESPECIALIDAD.put("ceguera", "Oftalmología");
        SINTOMAS_A_ESPECIALIDAD.put("conjuntivitis", "Oftalmología");
        SINTOMAS_A_ESPECIALIDAD.put("glaucoma", "Oftalmología");
        SINTOMAS_A_ESPECIALIDAD.put("catarata", "Oftalmología");

        // Otorrinolaringología
        SINTOMAS_A_ESPECIALIDAD.put("garganta", "Otorrinolaringología");
        SINTOMAS_A_ESPECIALIDAD.put("dolor de garganta", "Otorrinolaringología");
        SINTOMAS_A_ESPECIALIDAD.put("dolor en la garganta", "Otorrinolaringología");
        SINTOMAS_A_ESPECIALIDAD.put("me duele la garganta", "Otorrinolaringología");
        SINTOMAS_A_ESPECIALIDAD.put("nariz", "Otorrinolaringología");
        SINTOMAS_A_ESPECIALIDAD.put("oído", "Otorrinolaringología");
        SINTOMAS_A_ESPECIALIDAD.put("oido", "Otorrinolaringología");
        SINTOMAS_A_ESPECIALIDAD.put("dolor de oído", "Otorrinolaringología");
        SINTOMAS_A_ESPECIALIDAD.put("dolor de oido", "Otorrinolaringología");
        SINTOMAS_A_ESPECIALIDAD.put("dolor en el oído", "Otorrinolaringología");
        SINTOMAS_A_ESPECIALIDAD.put("dolor en el oido", "Otorrinolaringología");
        SINTOMAS_A_ESPECIALIDAD.put("me duele el oído", "Otorrinolaringología");
        SINTOMAS_A_ESPECIALIDAD.put("me duele el oido", "Otorrinolaringología");
        SINTOMAS_A_ESPECIALIDAD.put("audición", "Otorrinolaringología");
        SINTOMAS_A_ESPECIALIDAD.put("audicion", "Otorrinolaringología");
        SINTOMAS_A_ESPECIALIDAD.put("no escucha bien", "Otorrinolaringología");
        SINTOMAS_A_ESPECIALIDAD.put("amigdalitis", "Otorrinolaringología");
        SINTOMAS_A_ESPECIALIDAD.put("sinusitis", "Otorrinolaringología");
        SINTOMAS_A_ESPECIALIDAD.put("ronquera", "Otorrinolaringología");
        SINTOMAS_A_ESPECIALIDAD.put("voz ronca", "Otorrinolaringología");

        // Urología
        SINTOMAS_A_ESPECIALIDAD.put("riñón", "Urología");
        SINTOMAS_A_ESPECIALIDAD.put("riñon", "Urología");
        SINTOMAS_A_ESPECIALIDAD.put("dolor de riñón", "Urología");
        SINTOMAS_A_ESPECIALIDAD.put("dolor de riñon", "Urología");
        SINTOMAS_A_ESPECIALIDAD.put("dolor en el riñón", "Urología");
        SINTOMAS_A_ESPECIALIDAD.put("dolor en el riñon", "Urología");
        SINTOMAS_A_ESPECIALIDAD.put("orina", "Urología");
        SINTOMAS_A_ESPECIALIDAD.put("orinar", "Urología");
        SINTOMAS_A_ESPECIALIDAD.put("vejiga", "Urología");
        SINTOMAS_A_ESPECIALIDAD.put("próstata", "Urología");
        SINTOMAS_A_ESPECIALIDAD.put("prostata", "Urología");
        SINTOMAS_A_ESPECIALIDAD.put("renal", "Urología");
        SINTOMAS_A_ESPECIALIDAD.put("infección urinaria", "Urología");
        SINTOMAS_A_ESPECIALIDAD.put("infeccion urinaria", "Urología");

        // Ginecología
        SINTOMAS_A_ESPECIALIDAD.put("menstruación", "Ginecología");
        SINTOMAS_A_ESPECIALIDAD.put("menstruacion", "Ginecología");
        SINTOMAS_A_ESPECIALIDAD.put("periodo", "Ginecología");
        SINTOMAS_A_ESPECIALIDAD.put("embarazo", "Ginecología");
        SINTOMAS_A_ESPECIALIDAD.put("ovario", "Ginecología");
        SINTOMAS_A_ESPECIALIDAD.put("útero", "Ginecología");
        SINTOMAS_A_ESPECIALIDAD.put("utero", "Ginecología");
        SINTOMAS_A_ESPECIALIDAD.put("vaginal", "Ginecología");

        // Pediatría
        SINTOMAS_A_ESPECIALIDAD.put("niño", "Pediatría");
        SINTOMAS_A_ESPECIALIDAD.put("niña", "Pediatría");
        SINTOMAS_A_ESPECIALIDAD.put("bebé", "Pediatría");
        SINTOMAS_A_ESPECIALIDAD.put("bebe", "Pediatría");
        SINTOMAS_A_ESPECIALIDAD.put("infantil", "Pediatría");
        SINTOMAS_A_ESPECIALIDAD.put("mi hijo", "Pediatría");
        SINTOMAS_A_ESPECIALIDAD.put("mi hija", "Pediatría");

        // Psiquiatría
        SINTOMAS_A_ESPECIALIDAD.put("ansiedad", "Psiquiatría");
        SINTOMAS_A_ESPECIALIDAD.put("ansioso", "Psiquiatría");
        SINTOMAS_A_ESPECIALIDAD.put("ansiosa", "Psiquiatría");
        SINTOMAS_A_ESPECIALIDAD.put("depresión", "Psiquiatría");
        SINTOMAS_A_ESPECIALIDAD.put("depresion", "Psiquiatría");
        SINTOMAS_A_ESPECIALIDAD.put("deprimido", "Psiquiatría");
        SINTOMAS_A_ESPECIALIDAD.put("deprimida", "Psiquiatría");
        SINTOMAS_A_ESPECIALIDAD.put("insomnio", "Psiquiatría");
        SINTOMAS_A_ESPECIALIDAD.put("no puedo dormir", "Psiquiatría");
        SINTOMAS_A_ESPECIALIDAD.put("estrés", "Psiquiatría");
        SINTOMAS_A_ESPECIALIDAD.put("estres", "Psiquiatría");
        SINTOMAS_A_ESPECIALIDAD.put("nervios", "Psiquiatría");
        SINTOMAS_A_ESPECIALIDAD.put("nervioso", "Psiquiatría");
        SINTOMAS_A_ESPECIALIDAD.put("nerviosa", "Psiquiatría");
        SINTOMAS_A_ESPECIALIDAD.put("pánico", "Psiquiatría");
        SINTOMAS_A_ESPECIALIDAD.put("panico", "Psiquiatría");
        SINTOMAS_A_ESPECIALIDAD.put("ataque de pánico", "Psiquiatría");
        SINTOMAS_A_ESPECIALIDAD.put("ataque de panico", "Psiquiatría");

        // Genéricos → Medicina General
        SINTOMAS_A_ESPECIALIDAD.put("fiebre", "Medicina General");
        SINTOMAS_A_ESPECIALIDAD.put("temperatura", "Medicina General");
        SINTOMAS_A_ESPECIALIDAD.put("calentura", "Medicina General");
        SINTOMAS_A_ESPECIALIDAD.put("malestar", "Medicina General");
        SINTOMAS_A_ESPECIALIDAD.put("cansancio", "Medicina General");
        SINTOMAS_A_ESPECIALIDAD.put("cansado", "Medicina General");
        SINTOMAS_A_ESPECIALIDAD.put("cansada", "Medicina General");
        SINTOMAS_A_ESPECIALIDAD.put("debilidad", "Medicina General");
        SINTOMAS_A_ESPECIALIDAD.put("débil", "Medicina General");
        SINTOMAS_A_ESPECIALIDAD.put("inflamación", "Medicina General");
        SINTOMAS_A_ESPECIALIDAD.put("inflamacion", "Medicina General");
        SINTOMAS_A_ESPECIALIDAD.put("inflamado", "Medicina General");
        SINTOMAS_A_ESPECIALIDAD.put("inflamada", "Medicina General");
        SINTOMAS_A_ESPECIALIDAD.put("sangrado", "Medicina General");
        SINTOMAS_A_ESPECIALIDAD.put("sangre", "Medicina General");
        SINTOMAS_A_ESPECIALIDAD.put("mareos", "Medicina General");
        SINTOMAS_A_ESPECIALIDAD.put("perdida de peso", "Medicina General");
        SINTOMAS_A_ESPECIALIDAD.put("pérdida de peso", "Medicina General");
        SINTOMAS_A_ESPECIALIDAD.put("baje de peso", "Medicina General");
        SINTOMAS_A_ESPECIALIDAD.put("falta de apetito", "Medicina General");
        SINTOMAS_A_ESPECIALIDAD.put("no tengo hambre", "Medicina General");
        SINTOMAS_A_ESPECIALIDAD.put("no quiero comer", "Medicina General");
    }

    public ChatResponse procesarMensaje(Long idPaciente, String mensaje) {
        try {
            return procesarMensajeInterno(idPaciente, mensaje);
        } catch (Exception e) {
            conversaciones.remove(idPaciente);
            return new ChatResponse("Ocurrio un error. Por favor, intente de nuevo.\n\n1. Reservar cita\n2. Ver mis citas\n3. Informacion de especialidades\n4. Informacion de pagos" + SALIR, "error", null);
        }
    }

    private ChatResponse procesarMensajeInterno(Long idPaciente, String mensaje) {
        Map<String, Object> estado = conversaciones.computeIfAbsent(idPaciente, k -> {
            Map<String, Object> e = new HashMap<>();
            e.put("paso", Paso.INICIO);
            e.put("datos", new HashMap<String, Object>());
            return e;
        });

        Paciente paciente = pacienteRepository.findById(idPaciente).orElse(null);
        String nombre = (paciente != null) ? paciente.getNombres() : "Usuario";
        Paso paso = (Paso) estado.get("paso");
        @SuppressWarnings("unchecked")
        Map<String, Object> datos = (Map<String, Object>) estado.get("datos");
        String msg = mensaje.toLowerCase().trim();

        if (msg.contains("salir")) {
            estado.put("paso", Paso.INICIO);
            estado.put("datos", new HashMap<>());
            return new ChatResponse("Hasta luego! Que tenga un buen dia.", "salir", null);
        }
        if (msg.contains("cancelar")) {
            estado.put("paso", Paso.INICIO);
            estado.put("datos", new HashMap<>());
            return menu("Operacion cancelada.");
        }
        if (esSaludo(msg)) {
            estado.put("paso", Paso.ESPERANDO_OPCION);
            estado.put("datos", new HashMap<>());
            return new ChatResponse("Hola " + nombre + "! Bienvenido al Sistema de Gestion Hospitalaria.\n\n1. Reservar cita\n2. Ver mis citas\n3. Informacion de especialidades\n4. Informacion de pagos" + SALIR, "saludo", null);
        }
        if (msg.contains("iniciar") || msg.contains("menu") || msg.contains("menú")) {
            estado.put("paso", Paso.ESPERANDO_OPCION);
            return new ChatResponse("Hola " + nombre + "! Bienvenido al Sistema de Gestion Hospitalaria.\n\n1. Reservar cita\n2. Ver mis citas\n3. Informacion de especialidades\n4. Informacion de pagos" + SALIR, "saludo", null);
        }

        switch (paso) {
            case INICIO:
                estado.put("paso", Paso.ESPERANDO_OPCION);
                return new ChatResponse("Hola " + nombre + "! Bienvenido al Sistema de Gestion Hospitalaria.\n\n1. Reservar cita\n2. Ver mis citas\n3. Informacion de especialidades\n4. Informacion de pagos" + SALIR, "saludo", null);
            case ESPERANDO_OPCION:
                return opcion(msg, idPaciente, estado);
            case SELECCIONANDO_ESPECIALIDAD:
                return especialidad(msg, datos, estado);
            case SELECCIONANDO_MEDICO:
                return medico(msg, datos, estado);
            case SELECCIONANDO_FECHA:
                return fecha(msg, datos, estado);
            case SELECCIONANDO_HORA:
                return hora(msg, datos, estado);
            case INGRESANDO_MOTIVO:
                datos.put("motivoConsulta", mensaje);
                Boolean conDiagnostico = (Boolean) datos.get("conDiagnostico");
                if (conDiagnostico != null && conDiagnostico) {
                    estado.put("paso", Paso.CONFIRMANDO);
                    return mostrarResumenCita(datos, estado);
                } else {
                    estado.put("paso", Paso.INGRESANDO_SINTOMAS);
                    return new ChatResponse("Describa sus sintomas." + SALIR, "solicitar_sintomas", null);
                }
            case INGRESANDO_SINTOMAS:
                return sintomas(mensaje, datos, estado);
            case SUGERIENDO_ESPECIALIDAD:
                return sugerirEspecialidad(msg, datos, estado);
            case ESPERANDO_DECISION_DIAGNOSTICO:
                return decisionDiagnostico(msg, datos, estado);
            case DIAGNOSTICANDO:
                return diagnostico(mensaje, datos, estado);
            case CONFIRMANDO:
                return confirmar(msg, datos, estado, idPaciente);
            case VER_CITAS:
                return verCitas(idPaciente, estado);
            case EXPLICANDO_PAGOS:
                estado.put("paso", Paso.ESPERANDO_OPCION);
                return pagos(estado);
            default:
                estado.put("paso", Paso.INICIO);
                return new ChatResponse("Hola " + nombre + "! Bienvenido.\n\n1. Reservar cita\n2. Ver mis citas\n3. Informacion de especialidades\n4. Informacion de pagos" + SALIR, "saludo", null);
        }
    }

    private boolean esSaludo(String msg) {
        String normalizado = normalizarTexto(msg);
        return normalizado.matches(".*\\b(hola|buenas|buenos dias|buenas tardes|buenas noches|buen dia|que tal|como estas|saludos|hey|alo|holi)\\b.*");
    }

    private ChatResponse menu(String titulo) {
        return new ChatResponse(titulo + "\n\n1. Reservar cita\n2. Ver mis citas\n3. Informacion de especialidades\n4. Informacion de pagos" + SALIR, "menu", null);
    }

    private String menuTxt() {
        return "\n\n1. Reservar cita\n2. Ver mis citas\n3. Informacion de especialidades\n4. Informacion de pagos" + SALIR;
    }

    private ChatResponse opcion(String msg, Long idPaciente, Map<String, Object> estado) {
        if (msg.contains("1") || msg.contains("reservar")) {
            estado.put("paso", Paso.ESPERANDO_DECISION_DIAGNOSTICO);
            return new ChatResponse("¿Desea que le haga un diagnóstico para recomendarle la especialidad adecuada?\n\n1. Si, quiero un diagnóstico\n2. No, quiero elegir la especialidad yo mismo" + SALIR, "preguntar_diagnostico", null);
        } else if (msg.contains("2") || msg.contains("citas")) {
            estado.put("paso", Paso.ESPERANDO_OPCION);
            return verCitas(idPaciente, estado);
        } else if (msg.contains("3") || msg.contains("especialidad")) {
            List<Especialidad> especialidades = especialidadService.obtenerTodasLasEspecialidades();
            if (especialidades.isEmpty()) return menu("No hay especialidades.");
            StringBuilder sb = new StringBuilder("Nuestras especialidades:\n\n");
            for (Especialidad e : especialidades) {
                sb.append(String.format("- %s\n  %s\n  Precio: S/ %s\n\n", e.getNombre(), e.getDescripcion(), e.getPrecio()));
            }
            estado.put("paso", Paso.ESPERANDO_OPCION);
            return new ChatResponse(sb.toString() + "Desea reservar cita? Escriba \"reservar cita\"." + menuTxt(), "listar_esp_info", null);
        } else if (msg.contains("4") || msg.contains("pago")) {
            estado.put("paso", Paso.ESPERANDO_OPCION);
            return pagos(estado);
        } else {
            return llamarIA(msg, "Menu: 1=reservar, 2=citas, 3=especialidades, 4=pagos.");
        }
    }

    private ChatResponse decisionDiagnostico(String msg, Map<String, Object> datos, Map<String, Object> estado) {
        if (msg.contains("1") || msg.contains("si") || msg.contains("diagnostico") || msg.contains("diagnóstico")) {
            datos.put("conDiagnostico", true);
            estado.put("paso", Paso.DIAGNOSTICANDO);
            return new ChatResponse("Describa sus síntomas para recomendarle la especialidad adecuada." + SALIR, "solicitar_sintomas_diagnostico", null);
        } else if (msg.contains("2") || msg.contains("no") || msg.contains("elegir") || msg.contains("escoger")) {
            datos.put("conDiagnostico", false);
            List<Especialidad> especialidades = especialidadService.obtenerTodasLasEspecialidades();
            if (especialidades.isEmpty()) return menu("No hay especialidades.");
            StringBuilder sb = new StringBuilder("Especialidades disponibles:\n\n");
            for (int i = 0; i < especialidades.size(); i++) {
                Especialidad e = especialidades.get(i);
                sb.append(String.format("%d. %s - S/ %s\n", i + 1, e.getNombre(), e.getPrecio()));
            }
            sb.append("\nSeleccione una especialidad (numero)." + SALIR);
            estado.put("paso", Paso.SELECCIONANDO_ESPECIALIDAD);
            estado.put("especialidades", especialidades);
            return new ChatResponse(sb.toString(), "listar_especialidades", null);
        } else {
            return new ChatResponse("Por favor responda 1 (diagnóstico) o 2 (elegir especialidad)." + SALIR, "invalida", null);
        }
    }

    private static final List<String> SINTOMAS_GENERICOS = Arrays.asList(
        "dolor", "me duele", "malestar", "cansancio", "cansado", "cansada",
        "debilidad", "débil", "fiebre", "temperatura", "calentura",
        "inflamación", "inflamacion", "inflamado", "inflamada",
        "mareos", "sangrado", "sangre"
    );

    private boolean esSintomaGenerico(String sintomas) {
        String sintomasNormalizado = normalizarTexto(sintomas).trim();
        for (String generico : SINTOMAS_GENERICOS) {
            if (sintomasNormalizado.equals(normalizarTexto(generico))) {
                return true;
            }
        }
        return false;
    }

    private ChatResponse diagnostico(String msg, Map<String, Object> datos, Map<String, Object> estado) {
        datos.put("sintomas", msg);

        if (esSintomaGenerico(msg)) {
            return new ChatResponse("Por favor, sea más específico con sus síntomas.\n\nEjemplo: \"dolor de cabeza y náuseas\" en lugar de solo \"dolor\"." + SALIR, "sintomas_genericos", null);
        }

        String especialidadSugerida = analizarSintomas(msg);

        if (especialidadSugerida == null) {
            return new ChatResponse("No pude determinar una especialidad basada en esos síntomas. Por favor, seleccione la especialidad manualmente." + SALIR, "sin_sugerencia", null);
        }

        List<Especialidad> especialidades = especialidadService.obtenerTodasLasEspecialidades();
        Especialidad espEncontrada = null;
        String especialidadNormalizada = normalizarTexto(especialidadSugerida);
        for (Especialidad e : especialidades) {
            if (normalizarTexto(e.getNombre()).equals(especialidadNormalizada)) {
                espEncontrada = e;
                break;
            }
        }

        if (espEncontrada == null) {
            return new ChatResponse("La especialidad " + especialidadSugerida + " no está disponible. Por favor, seleccione manualmente." + SALIR, "esp_no_disponible", null);
        }

        Long idEsp = espEncontrada.getIdEspecialidad();
        datos.put("idEspecialidad", idEsp);
        datos.put("nombreEspecialidad", espEncontrada.getNombre());
        datos.put("precio", espEncontrada.getPrecio());

        List<Medico> medicos = medicoRepository.findAll();
        List<Medico> medicosOk = new ArrayList<>();
        for (Medico m : medicos) {
            List<DisponibilidadMedico> dlist = disponibilidadMedicoRepository.findByIdMedicoOrderByDiaSemanaAscHoraInicioAsc(m.getIdMedico());
            for (DisponibilidadMedico d : dlist) {
                if (d.getIdEspecialidad() != null && d.getIdEspecialidad().longValue() == idEsp.longValue()) {
                    medicosOk.add(m);
                    break;
                }
            }
        }

        if (medicosOk.isEmpty()) {
            estado.put("paso", Paso.ESPERANDO_OPCION);
            return menu("No hay médicos disponibles para " + especialidadSugerida + ".");
        }

        StringBuilder sb = new StringBuilder("Basado en sus síntomas, le recomiendo la especialidad de **" + especialidadSugerida + "**.\n\n");
        sb.append("Medicos disponibles:\n\n");
        for (int i = 0; i < medicosOk.size(); i++) {
            Medico m = medicosOk.get(i);
            List<DisponibilidadMedico> dlist = disponibilidadMedicoRepository.findByIdMedicoOrderByDiaSemanaAscHoraInicioAsc(m.getIdMedico());
            Set<String> diasSet = new LinkedHashSet<>();
            for (DisponibilidadMedico d : dlist) {
                if (d.getIdEspecialidad() != null && d.getIdEspecialidad().longValue() == idEsp.longValue()) {
                    diasSet.add(d.getDiaSemana().name().toLowerCase());
                }
            }
            String dias = diasSet.stream().map(d -> d.substring(0, 1).toUpperCase() + d.substring(1)).collect(Collectors.joining(", "));
            sb.append(String.format("%d. Dr(a). %s %s - Dias: %s\n", i + 1, m.getNombres(), m.getApellidos(), dias));
        }
        sb.append("\nSeleccione un medico (numero)." + SALIR);
        estado.put("paso", Paso.SELECCIONANDO_MEDICO);
        estado.put("medicos", medicosOk);
        return new ChatResponse(sb.toString(), "listar_medicos_diagnostico", null);
    }

    private ChatResponse especialidad(String msg, Map<String, Object> datos, Map<String, Object> estado) {
        @SuppressWarnings("unchecked")
        List<Especialidad> especialidades = (List<Especialidad>) estado.get("especialidades");
        if (especialidades == null) {
            especialidades = especialidadService.obtenerTodasLasEspecialidades();
        }
        int indice = -1;
        try {
            indice = Integer.parseInt(msg.replaceAll("[^0-9]", "")) - 1;
        } catch (NumberFormatException e) {
            for (int i = 0; i < especialidades.size(); i++) {
                if (especialidades.get(i).getNombre().toLowerCase().contains(msg)) { indice = i; break; }
            }
        }
        if (indice < 0 || indice >= especialidades.size()) {
            return new ChatResponse("Opcion no valida. Ingrese el numero." + SALIR, "invalida", null);
        }
        Especialidad esp = especialidades.get(indice);
        Long idEsp = esp.getIdEspecialidad();
        datos.put("idEspecialidad", idEsp);
        datos.put("nombreEspecialidad", esp.getNombre());
        datos.put("precio", esp.getPrecio());

        List<Medico> medicos = medicoRepository.findAll();
        List<Medico> medicosOk = new ArrayList<>();
        for (Medico m : medicos) {
            List<DisponibilidadMedico> dlist = disponibilidadMedicoRepository.findByIdMedicoOrderByDiaSemanaAscHoraInicioAsc(m.getIdMedico());
            for (DisponibilidadMedico d : dlist) {
                if (d.getIdEspecialidad() != null && d.getIdEspecialidad().longValue() == idEsp.longValue()) {
                    medicosOk.add(m);
                    break;
                }
            }
        }
        if (medicosOk.isEmpty()) {
            estado.put("paso", Paso.ESPERANDO_OPCION);
            return menu("No hay medicos para " + esp.getNombre() + ".");
        }

        StringBuilder sb = new StringBuilder("Medicos para " + esp.getNombre() + ":\n\n");
        for (int i = 0; i < medicosOk.size(); i++) {
            Medico m = medicosOk.get(i);
            List<DisponibilidadMedico> dlist = disponibilidadMedicoRepository.findByIdMedicoOrderByDiaSemanaAscHoraInicioAsc(m.getIdMedico());
            Set<String> diasSet = new LinkedHashSet<>();
            for (DisponibilidadMedico d : dlist) {
                if (d.getIdEspecialidad() != null && d.getIdEspecialidad().longValue() == idEsp.longValue()) {
                    diasSet.add(d.getDiaSemana().name().toLowerCase());
                }
            }
            String dias = diasSet.stream().map(d -> d.substring(0, 1).toUpperCase() + d.substring(1)).collect(Collectors.joining(", "));
            sb.append(String.format("%d. Dr(a). %s %s - Dias: %s\n", i + 1, m.getNombres(), m.getApellidos(), dias));
        }
        sb.append("\nSeleccione un medico (numero)." + SALIR);
        estado.put("paso", Paso.SELECCIONANDO_MEDICO);
        estado.put("medicos", medicosOk);
        return new ChatResponse(sb.toString(), "listar_medicos", null);
    }

    private ChatResponse medico(String msg, Map<String, Object> datos, Map<String, Object> estado) {
        @SuppressWarnings("unchecked")
        List<Medico> medicos = (List<Medico>) estado.get("medicos");
        if (medicos == null || medicos.isEmpty()) {
            estado.put("paso", Paso.INICIO);
            return menu("Error: no hay medicos disponibles.");
        }
        int indice = -1;
        try {
            indice = Integer.parseInt(msg.replaceAll("[^0-9]", "")) - 1;
        } catch (NumberFormatException e) {
            return new ChatResponse("Ingrese un numero valido." + SALIR, "invalido", null);
        }
        if (indice < 0 || indice >= medicos.size()) {
            return new ChatResponse("Opcion no valida. Ingrese el numero del medico." + SALIR, "invalido", null);
        }
        Medico medico = medicos.get(indice);
        Long idMedico = medico.getIdMedico();
        Long idEsp = (Long) datos.get("idEspecialidad");
        datos.put("idMedico", idMedico);
        datos.put("nombreMedico", medico.getNombres() + " " + medico.getApellidos());

        List<DisponibilidadMedico> dlist = disponibilidadMedicoRepository.findByIdMedicoOrderByDiaSemanaAscHoraInicioAsc(idMedico);
        Set<String> diasSet = new LinkedHashSet<>();
        for (DisponibilidadMedico d : dlist) {
            if (d.getIdEspecialidad() != null && d.getIdEspecialidad().longValue() == idEsp.longValue()) {
                diasSet.add(d.getDiaSemana().name().toLowerCase());
            }
        }

        String[] orden = {"lunes", "martes", "miercoles", "jueves", "viernes", "sabado"};
        StringBuilder sb = new StringBuilder("Dr(a). " + medico.getNombres() + " " + medico.getApellidos() + " atiende los:\n\n");
        int num = 1;
        for (String dia : orden) {
            if (diasSet.contains(dia)) {
                sb.append(String.format("%d. %s\n", num, dia.substring(0, 1).toUpperCase() + dia.substring(1)));
                num++;
            }
        }
        sb.append("\nQue dia desea? Escriba el nombre del dia." + SALIR);
        datos.put("diasTrabajo", new ArrayList<>(diasSet));
        estado.put("paso", Paso.SELECCIONANDO_FECHA);
        return new ChatResponse(sb.toString(), "solicitar_fecha", null);
    }

    private ChatResponse fecha(String msg, Map<String, Object> datos, Map<String, Object> estado) {
        @SuppressWarnings("unchecked")
        List<String> diasTrabajo = (List<String>) datos.get("diasTrabajo");
        String[] diasSemana = {"lunes", "martes", "miercoles", "jueves", "viernes", "sabado"};
        int diaNum = -1;
        for (int i = 0; i < diasSemana.length; i++) {
            if (msg.contains(diasSemana[i])) { diaNum = i + 1; break; }
        }
        if (msg.contains("manana")) {
            LocalDate m = LocalDate.now().plusDays(1);
            while (m.getDayOfWeek() == DayOfWeek.SUNDAY) m = m.plusDays(1);
            diaNum = m.getDayOfWeek().getValue();
        }
        if (diaNum == -1) {
            return new ChatResponse("No entendi. Escriba: lunes, martes, miercoles, jueves o viernes." + SALIR, "invalida", null);
        }
        String diaNombre = diasSemana[diaNum - 1];
        if (diasTrabajo == null || !diasTrabajo.contains(diaNombre)) {
            String disponibles = diasTrabajo != null ? diasTrabajo.stream().map(d -> d.substring(0, 1).toUpperCase() + d.substring(1)).collect(Collectors.joining(", ")) : "";
            return new ChatResponse("Ese dia no esta disponible. Dias: " + disponibles + SALIR, "invalida", null);
        }
        LocalDate fecha = LocalDate.now().plusDays(1);
        while (fecha.getDayOfWeek().getValue() != diaNum) fecha = fecha.plusDays(1);

        Long idMedico = (Long) datos.get("idMedico");
        Long idEsp = (Long) datos.get("idEspecialidad");

        List<String> reservadas = citaRepository.findByIdMedicoAndFecha(idMedico, fecha)
            .stream().map(c -> c.getHora().toString().substring(0, 5)).collect(Collectors.toList());

        List<DisponibilidadMedico> dlist = disponibilidadMedicoRepository.findByIdMedicoOrderByDiaSemanaAscHoraInicioAsc(idMedico);
        List<String> horas = new ArrayList<>();
        for (DisponibilidadMedico d : dlist) {
            if (d.getIdEspecialidad() == null || d.getIdEspecialidad().longValue() != idEsp.longValue()) continue;
            if (!d.getDiaSemana().name().toLowerCase().equals(diaNombre)) continue;
            LocalTime act = d.getHoraInicio();
            while (act.isBefore(d.getHoraFin())) {
                String h = act.toString().substring(0, 5);
                if (!reservadas.contains(h)) horas.add(h);
                act = act.plusMinutes(20);
            }
        }
        java.util.Set<String> horasAdicionales = new java.util.HashSet<>();
        for (CupoAdicional cupo : cupoAdicionalRepository.findByIdMedicoAndIdEspecialidadAndFechaAndDisponibleTrue(idMedico, idEsp, fecha)) {
            String h = cupo.getHoraInicio().toString().substring(0, 5);
            if (!reservadas.contains(h)) {
                horas.remove(h);
                horas.add(h);
                horasAdicionales.add(h);
            }
        }
        Collections.sort(horas);
        if (fecha.equals(LocalDate.now())) {
            String ahora = LocalTime.now().toString().substring(0, 5);
            java.util.Set<String> quitar = new java.util.HashSet<>();
            for (String h : horas) {
                if (h.compareTo(ahora) <= 0) quitar.add(h);
            }
            horas.removeAll(quitar);
            horasAdicionales.removeAll(quitar);
        }
        if (horas.isEmpty()) {
            return new ChatResponse("No hay horarios para ese dia." + SALIR, "sin_horarios", null);
        }
        StringBuilder sb = new StringBuilder("Horarios - " + diaNombre.substring(0, 1).toUpperCase() + diaNombre.substring(1) + " " + fecha.format(DateTimeFormatter.ofPattern("dd/MM")) + ":\n\n");
        for (int i = 0; i < horas.size(); i++) {
            String marca = horasAdicionales.contains(horas.get(i)) ? " (adicional)" : "";
            sb.append(String.format("%d. %s%s\n", i + 1, horas.get(i), marca));
        }
        sb.append("\nSeleccione una hora (numero)." + SALIR);
        estado.put("paso", Paso.SELECCIONANDO_HORA);
        datos.put("fecha", fecha);
        datos.put("horasDisponibles", horas);
        return new ChatResponse(sb.toString(), "listar_horas", null);
    }

    private ChatResponse hora(String msg, Map<String, Object> datos, Map<String, Object> estado) {
        @SuppressWarnings("unchecked")
        List<String> horas = (List<String>) datos.get("horasDisponibles");
        int indice = -1;
        try { indice = Integer.parseInt(msg.replaceAll("[^0-9]", "")) - 1; } catch (NumberFormatException e) {
            return new ChatResponse("Ingrese un numero valido." + SALIR, "invalido", null);
        }
        if (indice < 0 || indice >= horas.size()) {
            return new ChatResponse("Opcion no valida." + SALIR, "invalido", null);
        }
        String h = horas.get(indice);
        datos.put("hora", LocalTime.of(Integer.parseInt(h.substring(0, 2)), Integer.parseInt(h.substring(3))));

        Boolean conDiagnostico = (Boolean) datos.get("conDiagnostico");
        if (conDiagnostico != null && conDiagnostico) {
            datos.put("motivoConsulta", "Consulta por diagnóstico");
            estado.put("paso", Paso.CONFIRMANDO);
            return mostrarResumenCita(datos, estado);
        } else {
            estado.put("paso", Paso.INGRESANDO_MOTIVO);
            return new ChatResponse("Cual es el motivo de su consulta?" + SALIR, "solicitar_motivo", null);
        }
    }

    private String normalizarTexto(String texto) {
        String normalized = Normalizer.normalize(texto.toLowerCase(), Normalizer.Form.NFD);
        return normalized.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
    }

    private String analizarSintomas(String sintomas) {
        String sintomasNormalizado = normalizarTexto(sintomas);
        Map<String, Integer> conteoEspecialidades = new HashMap<>();

        for (Map.Entry<String, String> entry : SINTOMAS_A_ESPECIALIDAD.entrySet()) {
            String claveNormalizada = normalizarTexto(entry.getKey());
            if (sintomasNormalizado.contains(claveNormalizada)) {
                String especialidad = entry.getValue();
                conteoEspecialidades.put(especialidad, conteoEspecialidades.getOrDefault(especialidad, 0) + 1);
            }
        }

        if (conteoEspecialidades.isEmpty()) {
            return null;
        }

        String resultado;
        if (conteoEspecialidades.size() > 1) {
            resultado = "Medicina General";
        } else {
            resultado = conteoEspecialidades.keySet().iterator().next();
        }

        return validarEspecialidadDisponible(resultado);
    }

    private String validarEspecialidadDisponible(String nombre) {
        List<Especialidad> especialidades = especialidadService.obtenerTodasLasEspecialidades();
        String normalizado = normalizarTexto(nombre);
        for (Especialidad e : especialidades) {
            if (normalizarTexto(e.getNombre()).equals(normalizado)) {
                return e.getNombre();
            }
        }
        for (Especialidad e : especialidades) {
            if (normalizarTexto(e.getNombre()).equals("medicina general")) {
                return e.getNombre();
            }
        }
        return null;
    }

    private ChatResponse sintomas(String msg, Map<String, Object> datos, Map<String, Object> estado) {
        datos.put("sintomas", msg);
        String especialidadSugerida = analizarSintomas(msg);

        if (especialidadSugerida != null) {
            String especialidadActual = (String) datos.get("nombreEspecialidad");
            if (!especialidadSugerida.equals(especialidadActual)) {
                datos.put("especialidadSugerida", especialidadSugerida);
                estado.put("paso", Paso.SUGERIENDO_ESPECIALIDAD);
                StringBuilder sb = new StringBuilder();
                sb.append("Basado en sus síntomas, le recomiendo consultar con la especialidad de **").append(especialidadSugerida).append("**.\n\n");
                sb.append("Actualmente está seleccionando: ").append(especialidadActual).append("\n\n");
                sb.append("¿Desea cambiar a la especialidad de ").append(especialidadSugerida).append("? (si/no)\n");
                sb.append("O escriba \"continuar\" para mantener la especialidad actual." + SALIR);
                return new ChatResponse(sb.toString(), "sugerir_especialidad", null);
            }
        }

        return mostrarResumenCita(datos, estado);
    }

    private ChatResponse sugerirEspecialidad(String msg, Map<String, Object> datos, Map<String, Object> estado) {
        String especialidadSugerida = (String) datos.get("especialidadSugerida");

        if (msg.contains("si") || msg.contains("cambiar")) {
            List<Especialidad> especialidades = especialidadService.obtenerTodasLasEspecialidades();
            Especialidad nuevaEspecialidad = null;
            String especialidadNormalizada = normalizarTexto(especialidadSugerida);
            for (Especialidad e : especialidades) {
                if (normalizarTexto(e.getNombre()).equals(especialidadNormalizada)) {
                    nuevaEspecialidad = e;
                    break;
                }
            }

            if (nuevaEspecialidad != null) {
                Long idNuevaEsp = nuevaEspecialidad.getIdEspecialidad();
                Long idEspActual = (Long) datos.get("idEspecialidad");

                if (!idNuevaEsp.equals(idEspActual)) {
                    datos.put("idEspecialidad", idNuevaEsp);
                    datos.put("nombreEspecialidad", nuevaEspecialidad.getNombre());
                    datos.put("precio", nuevaEspecialidad.getPrecio());

                    List<Medico> medicos = medicoRepository.findAll();
                    List<Medico> medicosOk = new ArrayList<>();
                    for (Medico m : medicos) {
                        List<DisponibilidadMedico> dlist = disponibilidadMedicoRepository.findByIdMedicoOrderByDiaSemanaAscHoraInicioAsc(m.getIdMedico());
                        for (DisponibilidadMedico d : dlist) {
                            if (d.getIdEspecialidad() != null && d.getIdEspecialidad().longValue() == idNuevaEsp.longValue()) {
                                medicosOk.add(m);
                                break;
                            }
                        }
                    }

                    if (!medicosOk.isEmpty()) {
                        StringBuilder sb = new StringBuilder("Cambiando a " + especialidadSugerida + ".\n\n");
                        sb.append("Medicos disponibles:\n\n");
                        for (int i = 0; i < medicosOk.size(); i++) {
                            Medico m = medicosOk.get(i);
                            List<DisponibilidadMedico> dlist = disponibilidadMedicoRepository.findByIdMedicoOrderByDiaSemanaAscHoraInicioAsc(m.getIdMedico());
                            Set<String> diasSet = new LinkedHashSet<>();
                            for (DisponibilidadMedico d : dlist) {
                                if (d.getIdEspecialidad() != null && d.getIdEspecialidad().longValue() == idNuevaEsp.longValue()) {
                                    diasSet.add(d.getDiaSemana().name().toLowerCase());
                                }
                            }
                            String dias = diasSet.stream().map(d -> d.substring(0, 1).toUpperCase() + d.substring(1)).collect(Collectors.joining(", "));
                            sb.append(String.format("%d. Dr(a). %s %s - Dias: %s\n", i + 1, m.getNombres(), m.getApellidos(), dias));
                        }
                        sb.append("\nSeleccione un medico (numero)." + SALIR);
                        estado.put("paso", Paso.SELECCIONANDO_MEDICO);
                        estado.put("medicos", medicosOk);
                        return new ChatResponse(sb.toString(), "listar_medicos", null);
                    } else {
                        estado.put("paso", Paso.SELECCIONANDO_ESPECIALIDAD);
                        return new ChatResponse("No hay medicos disponibles para " + especialidadSugerida + ". Seleccione otra especialidad." + SALIR, "sin_medicos", null);
                    }
                }
            }
        }

        estado.put("paso", Paso.CONFIRMANDO);
        return mostrarResumenCita(datos, estado);
    }

    private ChatResponse mostrarResumenCita(Map<String, Object> datos, Map<String, Object> estado) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        StringBuilder sb = new StringBuilder("Resumen de su cita:\n\n");
        sb.append("Fecha: ").append(((LocalDate) datos.get("fecha")).format(fmt)).append("\n");
        sb.append("Hora: ").append(datos.get("hora").toString().substring(0, 5)).append("\n");
        sb.append("Medico: Dr(a). ").append(datos.get("nombreMedico")).append("\n");
        sb.append("Especialidad: ").append(datos.get("nombreEspecialidad")).append("\n");
        sb.append("Precio: S/ ").append(datos.get("precio")).append("\n");
        sb.append("Motivo: ").append(datos.get("motivoConsulta")).append("\n");
        sb.append("Sintomas: ").append(datos.get("sintomas")).append("\n\n");
        sb.append("Confirma la cita? (si/no)" + SALIR);
        estado.put("paso", Paso.CONFIRMANDO);
        return new ChatResponse(sb.toString(), "confirmar_cita", null);
    }

    private ChatResponse confirmar(String msg, Map<String, Object> datos, Map<String, Object> estado, Long idPaciente) {
        if (msg.contains("si") || msg.contains("confirmo") || msg.contains("confirmar")) {
            try {
                LocalDate fecha = (LocalDate) datos.get("fecha");
                Long idEsp = (Long) datos.get("idEspecialidad");
                List<Cita> existentes = citaRepository.findByIdPacienteOrderByFechaDescHoraDesc(idPaciente);
                boolean dup = existentes.stream()
                    .filter(c -> c.getIdEspecialidad() != null && c.getIdEspecialidad().equals(idEsp))
                    .filter(c -> c.getFecha().equals(fecha))
                    .filter(c -> !c.getEstado().equals(EstadoCita.cancelada))
                    .anyMatch(c -> true);
                if (dup) {
                    estado.put("paso", Paso.INICIO); estado.put("datos", new HashMap<>());
                    return menu("Ya tiene una cita en esta especialidad para ese dia.");
                }
                RegistrarCitaRequest req = new RegistrarCitaRequest();
                req.setIdPaciente(idPaciente);
                req.setIdMedico((Long) datos.get("idMedico"));
                req.setIdEspecialidad(idEsp);
                req.setFecha(fecha);
                req.setHora((LocalTime) datos.get("hora"));
                req.setMotivoConsulta((String) datos.get("motivoConsulta"));
                req.setSintomas((String) datos.get("sintomas"));
                Cita cita = citaService.registrarCita(req);

                StringBuilder sb = new StringBuilder("Cita registrada!\n\n");
                sb.append("Fecha: ").append(fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))).append(" a las ").append(datos.get("hora").toString().substring(0, 5)).append("\n");
                sb.append("Medico: Dr(a). ").append(datos.get("nombreMedico")).append("\n");
                sb.append("Especialidad: ").append(datos.get("nombreEspecialidad")).append("\n");
                sb.append("Precio: S/ ").append(datos.get("precio")).append("\n\n");
                sb.append("Pasos al llegar:\n");
                sb.append("1. Dirijase a recepcion\n");
                sb.append("2. Presente su DNI\n");
                sb.append("3. Pague S/ ").append(datos.get("precio")).append(" en efectivo o tarjeta de debito");
                sb.append(", o hagale saber al recepcionista si aplica su SIS\n");
                sb.append("4. Espere ser llamado");
                estado.put("paso", Paso.INICIO); estado.put("datos", new HashMap<>());
                return new ChatResponse(sb.toString(), "cita_registrada", null);
            } catch (Exception e) {
                estado.put("paso", Paso.INICIO); estado.put("datos", new HashMap<>());
                return menu("Usted ya esta registrado para este dia.");
            }
        } else if (msg.contains("no")) {
            estado.put("paso", Paso.INICIO); estado.put("datos", new HashMap<>());
            return menu("Cita cancelada.");
        } else {
            return new ChatResponse("Responda \"si\" o \"no\"." + SALIR, "invalido", null);
        }
    }

    private ChatResponse verCitas(Long idPaciente, Map<String, Object> estado) {
        List<Cita> citas = citaRepository.findByIdPacienteOrderByFechaDescHoraDesc(idPaciente);
        List<Cita> proximas = citas.stream()
            .filter(c -> !c.getEstado().equals(EstadoCita.cancelada) && !c.getEstado().equals(EstadoCita.no_presentado))
            .filter(c -> !c.getFecha().isBefore(LocalDate.now()))
            .sorted(Comparator.comparing(Cita::getFecha).thenComparing(Cita::getHora))
            .limit(5).collect(Collectors.toList());
        if (proximas.isEmpty()) {
            if (estado != null) estado.put("paso", Paso.ESPERANDO_OPCION);
            return new ChatResponse("No tiene citas proximas." + menuTxt(), "sin_citas", null);
        }
        StringBuilder sb = new StringBuilder("Sus proximas citas:\n\n");
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        for (Cita c : proximas) {
            sb.append(String.format("- %s a las %s\n  Estado: %s\n\n", c.getFecha().format(fmt), c.getHora().toString().substring(0, 5), c.getEstado()));
        }
        if (estado != null) estado.put("paso", Paso.ESPERANDO_OPCION);
        return new ChatResponse(sb.toString() + menuTxt(), "ver_citas", null);
    }

    private ChatResponse pagos(Map<String, Object> estado) {
        if (estado != null) estado.put("paso", Paso.ESPERANDO_OPCION);
        StringBuilder sb = new StringBuilder("Informacion de pagos:\n\n");
        for (Especialidad e : especialidadService.obtenerTodasLasEspecialidades()) {
            sb.append(String.format("- %s: S/ %s\n", e.getNombre(), e.getPrecio()));
        }
        sb.append("\nFormas de pago:\n- Efectivo\n- Tarjeta de debito\n\n");
        sb.append("Si tiene seguro SIS, hagalo saber en recepcion.\n");
        sb.append("Llegue 15 minutos antes con su DNI.");
        return new ChatResponse(sb.toString() + menuTxt(), "pagos", null);
    }

    private ChatResponse llamarIA(String msg, String contexto) {
        if (groqApiKey == null || groqApiKey.isEmpty()) return menu("No entendi su mensaje.");
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(groqApiKey);
            String sys = "Eres el asistente del Sistema de Gestion Hospitalaria. Responde breve en español. Contexto: " + contexto;
            Map<String, Object> body = new HashMap<>();
            body.put("model", "llama3-8b-8192");
            body.put("messages", List.of(Map.of("role", "system", "content", sys), Map.of("role", "user", "content", msg)));
            body.put("temperature", 0.7);
            body.put("max_tokens", 300);
            HttpEntity<Map<String, Object>> req = new HttpEntity<>(body, headers);
            ResponseEntity<Map> resp = restTemplate.postForEntity(groqApiUrl, req, Map.class);
            if (resp.getBody() != null && resp.getBody().containsKey("choices")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> choices = (List<Map<String, Object>>) resp.getBody().get("choices");
                if (!choices.isEmpty()) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    return new ChatResponse((String) message.get("content") + menuTxt(), "ia", null);
                }
            }
        } catch (Exception e) { }
        return menu("No entendi su mensaje.");
    }

    public void limpiarConversacion(Long idPaciente) {
        conversaciones.remove(idPaciente);
    }
}

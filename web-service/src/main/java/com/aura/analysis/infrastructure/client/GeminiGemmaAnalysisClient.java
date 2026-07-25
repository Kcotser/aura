package com.aura.analysis.infrastructure.client;

import com.aura.analysis.domain.model.AnalysisResult;
import com.aura.analysis.domain.repository.GemmaAnalysisClient;
import com.aura.evidence.application.dto.EvidenceDownloadReference;
import com.aura.evidence.application.service.EvidenceQueryService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ACL de analisis multimodal en dos etapas contra Google AI Studio.
 *
 * <ol>
 *   <li><b>Gemini</b> recibe los binarios reales (audio ambiental y video de ambas camaras) y
 *       devuelve la transcripcion literal y la descripcion visual.</li>
 *   <li><b>Gemma 4</b> recibe ese texto y decide la entidad de derivacion y el nivel de amenaza.</li>
 * </ol>
 *
 * <p>El reparto no es arbitrario: los modelos Gemma rechazan audio con
 * {@code "Audio input modality is not enabled for this model"}, asi que la transcripcion solo
 * la puede hacer Gemini. Gemma si razona bien sobre texto, que es la etapa de triage.
 *
 * <p>Los binarios viajan como {@code inline_data} en base64. La API limita el request completo a
 * 20 MB y base64 infla los bytes crudos en 4/3, de ahi {@link #INLINE_BUDGET_BYTES}. Las
 * evidencias se cargan por prioridad — el audio primero, que es lo que se transcribe — y lo que
 * no entra queda registrado en el log en vez de romper el analisis.
 */
@Component
public class GeminiGemmaAnalysisClient implements GemmaAnalysisClient {

    private static final Logger log = LoggerFactory.getLogger(GeminiGemmaAnalysisClient.class);

    /** Tope de bytes crudos por request; en base64 quedan ~14.7 MB, bajo el limite de 20 MB. */
    private static final long INLINE_BUDGET_BYTES = 11L * 1024 * 1024;

    /** El audio va primero: es la evidencia que se transcribe y la que no puede faltar. */
    private static final List<String> PRIORITY = List.of("AMBIENT_AUDIO", "FRONT_CAMERA", "BACK_CAMERA");

    /**
     * Gemma razona en voz alta y entierra el JSON al final de la respuesta pese a que se le pida
     * lo contrario, asi que se extrae el ultimo objeto de primer nivel en vez de parsear todo.
     */
    private static final Pattern JSON_OBJECT = Pattern.compile("\\{[^{}]*\\}");

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final EvidenceQueryService evidenceQueryService;
    private final String geminiUrl;
    private final String gemmaUrl;
    private final String apiKey;

    public GeminiGemmaAnalysisClient(
            @Value("${aura.gemini.api-url}") String geminiUrl,
            @Value("${aura.gemma.api-url}") String gemmaUrl,
            @Value("${aura.gemma.api-key}") String apiKey,
            ObjectMapper objectMapper,
            EvidenceQueryService evidenceQueryService) {
        this.restTemplate = new RestTemplate();
        this.geminiUrl = withGenerateContent(geminiUrl);
        this.gemmaUrl = withGenerateContent(gemmaUrl);
        this.apiKey = apiKey;
        this.objectMapper = objectMapper;
        this.evidenceQueryService = evidenceQueryService;
    }

    private static String withGenerateContent(String url) {
        if (url == null || url.isBlank()) {
            return url;
        }
        return url.contains(":generateContent") ? url : url + ":generateContent";
    }

    @Override
    public AnalysisResult analyzeIncident(String incidentId, List<EvidenceDownloadReference> evidenceFiles) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("GEMMA_API_KEY no configurada: no se puede analizar el incidente " + incidentId);
        }

        // Etapa 1 — Gemini: percepcion sobre los binarios reales.
        JsonNode perception = runGeminiPerception(incidentId, evidenceFiles);
        String transcript = text(perception, "audioTranscriptSummary", "Audio inaudible");
        String visual = text(perception, "visualContextDescription", "Sin evidencia visual adjunta");

        // Etapa 2 — Gemma 4: triage sobre el texto que produjo Gemini.
        // Los centinelas son explicitos a proposito: pasar null aqui hace que el constructor
        // compacto de AnalysisResult rellene "comisaria-mujer"/"HIGH", y un triage caido
        // terminaba guardado como si fuera una clasificacion real.
        JsonNode triage = runGemmaTriage(incidentId, transcript, visual);
        String entityCode = text(triage, "suggestedEntityCode", "sin-clasificar");
        String threatLevel = text(triage, "threatLevel", "UNKNOWN");

        log.info("Analisis completado incidentId={}, threatLevel={}, entityCode={}, transcriptChars={}",
                incidentId, threatLevel, entityCode, transcript.length());

        return new AnalysisResult(transcript, visual, entityCode, threatLevel);
    }

    // ---------------------------------------------------------------- etapa 1: Gemini multimodal

    private JsonNode runGeminiPerception(String incidentId, List<EvidenceDownloadReference> evidenceFiles) {
        List<Map<String, Object>> mediaParts = buildMediaParts(incidentId, evidenceFiles);
        if (mediaParts.isEmpty()) {
            throw new IllegalStateException("No hay evidencia legible para el incidente " + incidentId);
        }

        List<Map<String, Object>> parts = new ArrayList<>();
        parts.add(Map.of("text", perceptionPrompt(incidentId)));
        parts.addAll(mediaParts);

        Map<String, Object> body = Map.of(
                "contents", List.of(Map.of("parts", parts)),
                "generationConfig", Map.of("responseMimeType", "application/json")
        );

        return extractJson(post(geminiUrl, body), "Gemini");
    }

    /**
     * Carga los binarios que entran en el presupuesto inline, en orden de prioridad.
     */
    private List<Map<String, Object>> buildMediaParts(String incidentId, List<EvidenceDownloadReference> evidenceFiles) {
        List<Map<String, Object>> parts = new ArrayList<>();
        long used = 0;

        List<EvidenceDownloadReference> ordered = evidenceFiles.stream()
                .sorted(Comparator.comparingInt(e -> {
                    int index = PRIORITY.indexOf(e.mediaType());
                    return index < 0 ? PRIORITY.size() : index;
                }))
                .toList();

        for (EvidenceDownloadReference evidence : ordered) {
            if (used + evidence.sizeBytes() > INLINE_BUDGET_BYTES) {
                log.warn("Evidencia omitida por presupuesto inline: incidentId={}, type={}, sizeBytes={}",
                        incidentId, evidence.mediaType(), evidence.sizeBytes());
                continue;
            }

            Optional<byte[]> content = evidenceQueryService.loadContent(evidence.evidenceId());
            if (content.isEmpty()) {
                continue;
            }
            used += content.get().length;

            // Etiqueta de texto antes de cada binario para que el modelo sepa que camara es cual.
            parts.add(Map.of("text", "Evidencia " + evidence.mediaType() + ":"));

            Map<String, Object> inlineData = new LinkedHashMap<>();
            inlineData.put("mime_type", resolveMimeType(evidence));
            inlineData.put("data", Base64.getEncoder().encodeToString(content.get()));
            parts.add(Map.of("inline_data", inlineData));

            log.info("Evidencia adjuntada: incidentId={}, type={}, mimeType={}, bytes={}",
                    incidentId, evidence.mediaType(), resolveMimeType(evidence), content.get().length);
        }

        return parts;
    }

    /**
     * El movil sube el audio como {@code audio/mp4} (.m4a) y el video como {@code video/mp4};
     * ambos los acepta la API. Si el contentType llega vacio se deduce del tipo de evidencia.
     */
    private String resolveMimeType(EvidenceDownloadReference evidence) {
        String contentType = evidence.contentType();
        if (contentType != null && !contentType.isBlank() && !"application/octet-stream".equals(contentType)) {
            return contentType;
        }
        return "AMBIENT_AUDIO".equals(evidence.mediaType()) ? "audio/mp4" : "video/mp4";
    }

    /**
     * Las prohibiciones son necesarias: sin ellas el modelo describe una escena visual plausible
     * aunque solo se le haya mandado audio.
     */
    private String perceptionPrompt(String incidentId) {
        return """
                Eres un analista de evidencia de la plataforma de emergencia Aura.
                Analiza UNICAMENTE los archivos adjuntos del incidente %s.

                Reglas estrictas:
                - Transcribe el audio literalmente, palabra por palabra, en su idioma original.
                - Describe solo lo que realmente se ve en los videos adjuntos.
                - Si no se adjunto video, escribe exactamente "Sin evidencia visual adjunta".
                - Si el audio es inaudible o esta vacio, escribe exactamente "Audio inaudible".
                - No inventes ni completes detalles que no esten en la evidencia.

                Responde solo con este JSON:
                {"audioTranscriptSummary": "...", "visualContextDescription": "..."}
                """.formatted(incidentId);
    }

    // -------------------------------------------------------------------- etapa 2: Gemma triage

    private JsonNode runGemmaTriage(String incidentId, String transcript, String visual) {
        Map<String, Object> body = Map.of(
                "contents", List.of(Map.of("parts", List.of(
                        Map.of("text", triagePrompt(transcript, visual))
                )))
        );

        try {
            return extractJson(post(gemmaUrl, body), "Gemma");
        } catch (Exception e) {
            // El triage es derivable de la transcripcion, que ya se guardo. Degradar aqui conserva
            // la evidencia real en vez de perder todo el analisis por la segunda llamada.
            log.warn("Triage de Gemma fallo para incidentId={}: {}. Se guarda la percepcion sin clasificar.",
                    incidentId, e.getMessage());
            return objectMapper.createObjectNode();
        }
    }

    private String triagePrompt(String transcript, String visual) {
        return """
                Eres un despachador de emergencias en Peru. Clasifica el incidente a partir de la
                evidencia ya procesada.

                Transcripcion del audio: "%s"
                Contexto visual: "%s"

                Entidades disponibles:
                - comisaria-mujer: violencia de genero o agresor conocido.
                - linea-100: apoyo psicologico del MIMP, sin peligro fisico inmediato.
                - 911-emergencias: peligro fisico inmediato.

                Niveles de amenaza: LOW, MEDIUM, HIGH, CRITICAL.

                Termina tu respuesta con este JSON y nada despues:
                {"suggestedEntityCode": "...", "threatLevel": "..."}
                """.formatted(transcript, visual);
    }

    // ------------------------------------------------------------------------------- utilidades

    private String post(String url, Map<String, Object> body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String requestUrl = url + (url.contains("?") ? "&" : "?") + "key=" + apiKey;
        return restTemplate.postForObject(requestUrl, new HttpEntity<>(body, headers), String.class);
    }

    /**
     * Saca el JSON util de una respuesta {@code generateContent}. Si el modelo lo envolvio en
     * markdown o lo dejo al final de un razonamiento, toma el ultimo objeto que aparezca.
     */
    JsonNode extractJson(String rawResponse, String modelLabel) {
        try {
            JsonNode root = objectMapper.readTree(rawResponse);
            String text = answerText(root.path("candidates").get(0).path("content").path("parts"));

            Matcher matcher = JSON_OBJECT.matcher(text);
            String lastObject = null;
            while (matcher.find()) {
                lastObject = matcher.group();
            }
            if (lastObject == null) {
                throw new IllegalStateException("la respuesta no contiene ningun objeto JSON");
            }
            return objectMapper.readTree(lastObject);
        } catch (Exception e) {
            // Se propaga: un fallo aqui deja el job en FAILED en vez de simular un analisis.
            throw new IllegalStateException(
                    "No se pudo interpretar la respuesta de " + modelLabel + ": " + e.getMessage(), e);
        }
    }

    /**
     * Junta el texto de la respuesta descartando las partes de razonamiento.
     *
     * <p>Estos modelos devuelven varias {@code parts}: las intermedias vienen marcadas con
     * {@code "thought": true} y solo la ultima es la respuesta. Leer {@code parts[0]} daba el
     * razonamiento, que o no trae JSON — y el analisis quedaba sin clasificar — o trae un JSON
     * tentativo que el modelo despues descarta, que es peor porque se guarda como si fuera bueno.
     */
    private String answerText(JsonNode parts) {
        StringBuilder answer = new StringBuilder();
        for (JsonNode part : parts) {
            if (part.path("thought").asBoolean(false)) {
                continue;
            }
            answer.append(part.path("text").asText(""));
        }
        return answer.toString().trim();
    }

    private String text(JsonNode node, String field, String fallback) {
        String value = node.path(field).asText(null);
        return (value == null || value.isBlank()) ? fallback : value;
    }

    /**
     * Compatibilidad con el contrato anterior: parsea una respuesta completa de percepcion.
     */
    public AnalysisResult parseModelResponse(String rawJson) {
        JsonNode parsed = extractJson(rawJson, "Gemini");
        return new AnalysisResult(
                parsed.path("audioTranscriptSummary").asText(null),
                parsed.path("visualContextDescription").asText(null),
                parsed.path("suggestedEntityCode").asText(null),
                parsed.path("threatLevel").asText(null)
        );
    }
}

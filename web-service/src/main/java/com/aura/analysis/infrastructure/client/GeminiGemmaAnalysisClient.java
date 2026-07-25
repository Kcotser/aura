package com.aura.analysis.infrastructure.client;

import com.aura.analysis.domain.model.AnalysisResult;
import com.aura.analysis.domain.model.EvidenceMediaPayload;
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
 * ACL de análisis multimodal en dos etapas contra Google AI Studio.
 *
 * <ol>
 *   <li><b>Gemini</b> recibe los binarios reales (audio ambiental y video de ambas cámaras) y
 *       devuelve la transcripción literal y la descripción visual.</li>
 *   <li><b>Gemma 4</b> recibe ese texto y decide la entidad de derivación y el nivel de amenaza.</li>
 * </ol>
 */
@Component
public class GeminiGemmaAnalysisClient implements GemmaAnalysisClient {

    private static final Logger log = LoggerFactory.getLogger(GeminiGemmaAnalysisClient.class);

    private static final long INLINE_BUDGET_BYTES = 11L * 1024 * 1024;
    private static final List<String> PRIORITY = List.of("AMBIENT_AUDIO", "FRONT_CAMERA", "BACK_CAMERA");
    private static final Pattern JSON_OBJECT = Pattern.compile("\\{[^{}]*\\}");

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final EvidenceQueryService evidenceQueryService;

    private final String geminiUrl;
    private final String gemmaUrl;
    private final String apiKey;

    public GeminiGemmaAnalysisClient(
            @Value("${aura.gemini.api-url:https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash}") String geminiUrl,
            @Value("${aura.gemma.api-url:https://generativelanguage.googleapis.com/v1beta/models/gemma-4-31b}") String gemmaUrl,
            @Value("${aura.gemma.api-key:}") String apiKey,
            EvidenceQueryService evidenceQueryService,
            ObjectMapper objectMapper) {
        this.restTemplate = new RestTemplate();
        this.geminiUrl = withGenerateContent(geminiUrl);
        this.gemmaUrl = withGenerateContent(gemmaUrl);
        this.apiKey = apiKey;
        this.evidenceQueryService = evidenceQueryService;
        this.objectMapper = objectMapper;
    }

    private static String withGenerateContent(String url) {
        if (url == null || url.isBlank()) {
            return url;
        }
        return url.contains(":generateContent") ? url : url + ":generateContent";
    }

    @Override
    public AnalysisResult analyzeIncident(String incidentId, List<EvidenceDownloadReference> evidenceFiles) {
        return analyzeIncident(incidentId, evidenceFiles, List.of());
    }

    @Override
    public AnalysisResult analyzeIncident(
            String incidentId,
            List<EvidenceDownloadReference> evidenceFiles,
            List<EvidenceMediaPayload> mediaPayloads
    ) {
        log.info("Iniciando analisis multimodal en 2 etapas para incidentId={}, evidencias={}",
                incidentId, evidenceFiles.size());

        List<EvidenceDownloadReference> sorted = evidenceFiles.stream()
                .sorted(Comparator.comparingInt(ref -> {
                    int idx = PRIORITY.indexOf(ref.mediaType());
                    return idx >= 0 ? idx : PRIORITY.size();
                }))
                .toList();

        List<Map<String, Object>> parts = new ArrayList<>();
        long budgetUsed = 0;

        for (EvidenceDownloadReference ref : sorted) {
            byte[] bytes = null;
            
            // Check if passed in mediaPayloads first
            Optional<EvidenceMediaPayload> provided = mediaPayloads.stream()
                    .filter(p -> p != null && p.hasData() && ref.mediaType().equalsIgnoreCase(p.mediaType()))
                    .findFirst();

            if (provided.isPresent()) {
                bytes = provided.get().data();
            } else {
                Optional<byte[]> loaded = evidenceQueryService.loadContent(ref.evidenceId());
                if (loaded.isPresent()) {
                    bytes = loaded.get();
                }
            }

            if (bytes == null || bytes.length == 0) {
                log.warn("Se omite evidencia {} ({}) por falta de binario", ref.evidenceId(), ref.mediaType());
                continue;
            }

            if (budgetUsed + bytes.length > INLINE_BUDGET_BYTES) {
                log.warn("Se omite evidencia {} ({}, {} bytes) por exceder el presupuesto inline",
                        ref.evidenceId(), ref.mediaType(), bytes.length);
                continue;
            }

            budgetUsed += bytes.length;
            String mimeType = (ref.contentType() != null && !ref.contentType().isBlank())
                    ? ref.contentType() : defaultMime(ref.mediaType());

            parts.add(Map.of(
                    "inline_data", Map.of(
                            "mime_type", mimeType,
                            "data", Base64.getEncoder().encodeToString(bytes)
                    )
            ));
            log.info("Evidencia adjuntada a Gemini: tipo={}, mime={}, bytes={}",
                    ref.mediaType(), mimeType, bytes.length);
        }

        parts.add(Map.of("text", perceptionPrompt(incidentId)));

        JsonNode perceptionJson = runGeminiPerception(incidentId, parts);
        String transcript = text(perceptionJson, "audioTranscriptSummary", "Audio inaudible o sin violencia verbal explícita");
        String visual = text(perceptionJson, "visualContextDescription", "Sin evidencia visual relevante");

        JsonNode triageJson = runGemmaTriage(incidentId, transcript, visual);
        String entity = text(triageJson, "suggestedEntityCode", "comisaria-mujer");
        String threat = text(triageJson, "threatLevel", "HIGH");

        log.info("Analisis completado para incidentId={}: entidad={}, amenaza={}", incidentId, entity, threat);

        return new AnalysisResult(transcript, visual, entity, threat);
    }

    private static String defaultMime(String mediaType) {
        if ("AMBIENT_AUDIO".equalsIgnoreCase(mediaType)) {
            return "audio/m4a";
        }
        return "video/mp4";
    }

    private JsonNode runGeminiPerception(String incidentId, List<Map<String, Object>> parts) {
        Map<String, Object> body = Map.of("contents", List.of(Map.of("parts", parts)));
        return extractJson(post(geminiUrl, body), "Gemini");
    }

    private String perceptionPrompt(String incidentId) {
        return """
                Eres un perito forense analizando evidencia de una emergencia personal en Peru (incidentId: %s).
                Analiza las pistas adjuntas y extrae SOLO los hechos verificables observados y escuchados.

                Reglas estrictas:
                - Transcribe textualmente o resume con alta precision las voces, gritos, amenazas o ruidos del audio.
                - Describe objetivamente lo que muestran las camaras (personas, entorno, agresion, armas o movimientos).
                - Si no se adjunto video, escribe exactamente "Sin evidencia visual adjunta".
                - Si el audio es inaudible o esta vacio, escribe exactamente "Audio inaudible".

                Responde solo con este JSON:
                {"audioTranscriptSummary": "...", "visualContextDescription": "..."}
                """.formatted(incidentId);
    }

    private JsonNode runGemmaTriage(String incidentId, String transcript, String visual) {
        Map<String, Object> body = Map.of(
                "contents", List.of(Map.of("parts", List.of(
                        Map.of("text", triagePrompt(transcript, visual))
                )))
        );

        try {
            return extractJson(post(gemmaUrl, body), "Gemma");
        } catch (Exception e) {
            log.warn("Triage de Gemma fallo para incidentId={}: {}. Se guarda la percepcion sin clasificar.",
                    incidentId, e.getMessage());
            return objectMapper.createObjectNode();
        }
    }

    private String triagePrompt(String transcript, String visual) {
        return """
                Eres un despachador de emergencias en Peru. Clasifica el incidente a partir de la evidencia ya procesada.

                Transcripcion del audio: "%s"
                Contexto visual: "%s"

                Entidades disponibles:
                - comisaria-mujer: violencia de genero o agresor conocido.
                - linea-100: apoyo psicologico del MIMP, sin peligro fisico inmediato.
                - cem: centro emergencia mujer.
                - mininter: ministerio del interior / policia nacional.

                Niveles de amenaza: LOW, MEDIUM, HIGH, CRITICAL.

                Termina tu respuesta con este JSON y nada despues:
                {"suggestedEntityCode": "...", "threatLevel": "..."}
                """.formatted(transcript, visual);
    }

    private String post(String url, Map<String, Object> body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String requestUrl = url + (url.contains("?") ? "&" : "?") + "key=" + apiKey;
        return restTemplate.postForObject(requestUrl, new HttpEntity<>(body, headers), String.class);
    }

    JsonNode extractJson(String rawResponse, String modelLabel) {
        try {
            JsonNode root = objectMapper.readTree(rawResponse);
            String text = root.path("candidates").get(0)
                    .path("content").path("parts").get(0).path("text").asText().trim();

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
            throw new IllegalStateException("No se pudo interpretar la respuesta de " + modelLabel + ": " + e.getMessage(), e);
        }
    }

    private String text(JsonNode node, String field, String fallback) {
        String value = node.path(field).asText(null);
        return (value == null || value.isBlank()) ? fallback : value;
    }

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

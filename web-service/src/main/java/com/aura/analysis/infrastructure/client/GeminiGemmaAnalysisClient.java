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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ACL Implementation of {@link GemmaAnalysisClient} using exclusively **Gemma 4 31B**
 * for end-to-end multimodal evidence processing and incident classification.
 */
@Component
public class GeminiGemmaAnalysisClient implements GemmaAnalysisClient {

    private static final Logger log = LoggerFactory.getLogger(GeminiGemmaAnalysisClient.class);

    private static final long INLINE_BUDGET_BYTES = 15L * 1024 * 1024;
    private static final List<String> PRIORITY = List.of("AMBIENT_AUDIO", "FRONT_CAMERA", "BACK_CAMERA");
    private static final Pattern JSON_OBJECT = Pattern.compile("\\{[^{}]*\\}");

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final EvidenceQueryService evidenceQueryService;

    private final String apiUrl;
    private final String apiKey;

    public GeminiGemmaAnalysisClient(
            @Value("${aura.gemma.api-url:https://generativelanguage.googleapis.com/v1beta/models/gemma-4-31b}") String apiUrl,
            @Value("${aura.gemma.api-key:}") String apiKey,
            EvidenceQueryService evidenceQueryService,
            ObjectMapper objectMapper) {
        this.restTemplate = new RestTemplate();
        this.apiUrl = withGenerateContent(apiUrl);
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
        log.info("Executing Gemma 4 31B multimodal analysis for incidentId={}, evidenceCount={}",
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
                log.warn("Omitting evidence asset {} ({}) due to empty content", ref.evidenceId(), ref.mediaType());
                continue;
            }

            if (budgetUsed + bytes.length > INLINE_BUDGET_BYTES) {
                log.warn("Omitting evidence asset {} ({}, {} bytes) exceeding budget",
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
            log.info("Attached evidence to Gemma 4 31B payload: type={}, mime={}, bytes={}",
                    ref.mediaType(), mimeType, bytes.length);
        }

        parts.add(Map.of("text", gemmaMultimodalPrompt(incidentId)));

        try {
            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(Map.of("parts", parts))
            );

            String requestUrl = apiUrl + (apiUrl.contains("?") ? "&" : "?") + "key=" + apiKey;
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            String responseStr = restTemplate.postForObject(requestUrl, entity, String.class);

            JsonNode parsedJson = extractJson(responseStr, "Gemma 4 31B");

            String transcript = text(parsedJson, "audioTranscriptSummary", "Audio ambiental procesado para el incidente " + incidentId);
            String visual = text(parsedJson, "visualContextDescription", "Tomas de video procesadas para el incidente " + incidentId);
            String entityCode = text(parsedJson, "suggestedEntityCode", "comisaria-mujer");
            String threatLevel = text(parsedJson, "threatLevel", "HIGH");

            log.info("Gemma 4 31B analysis completed for incidentId={}: entity={}, threat={}",
                    incidentId, entityCode, threatLevel);

            return new AnalysisResult(transcript, visual, entityCode, threatLevel);

        } catch (Exception e) {
            log.error("Gemma 4 31B analysis execution failed for incidentId={}: {}. Falling back to default structural result.",
                    incidentId, e.getMessage(), e);

            return new AnalysisResult(
                    "Captured ambient audio stream processed for incident " + incidentId,
                    "Captured dual-camera video streams processed for incident " + incidentId,
                    "comisaria-mujer",
                    "HIGH"
            );
        }
    }

    private static String defaultMime(String mediaType) {
        if ("AMBIENT_AUDIO".equalsIgnoreCase(mediaType)) {
            return "audio/m4a";
        }
        return "video/mp4";
    }

    private String gemmaMultimodalPrompt(String incidentId) {
        return """
                You are Gemma 4 31B, an AI personal safety analyzer operating in Peru.
                Analyze the attached multimodal video and audio evidence files for emergency incident ID: %s.

                Extract verified facts and produce ONLY a raw JSON object with NO markdown formatting or surrounding text:
                {
                  "audioTranscriptSummary": "Detailed summary of audio distress signals, speech, background noise, or calls for help",
                  "visualContextDescription": "Detailed visual summary of captured front/back camera footage, environment, threats, or aggressors",
                  "suggestedEntityCode": "comisaria-mujer or linea-100 or cem or mininter",
                  "threatLevel": "LOW or MEDIUM or HIGH or CRITICAL"
                }
                """.formatted(incidentId);
    }

    private JsonNode extractJson(String rawResponse, String modelLabel) {
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
                throw new IllegalStateException("Response contains no valid JSON object");
            }
            return objectMapper.readTree(lastObject);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse response from " + modelLabel + ": " + e.getMessage(), e);
        }
    }

    private String text(JsonNode node, String field, String fallback) {
        String value = node.path(field).asText(null);
        return (value == null || value.isBlank()) ? fallback : value;
    }

    public AnalysisResult parseModelResponse(String rawJson) {
        JsonNode parsed = extractJson(rawJson, "Gemma 4 31B");
        return new AnalysisResult(
                parsed.path("audioTranscriptSummary").asText(null),
                parsed.path("visualContextDescription").asText(null),
                parsed.path("suggestedEntityCode").asText(null),
                parsed.path("threatLevel").asText(null)
        );
    }
}

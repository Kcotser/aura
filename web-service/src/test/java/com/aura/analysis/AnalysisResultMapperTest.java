package com.aura.analysis;

import com.aura.analysis.domain.model.AnalysisResult;
import com.aura.analysis.infrastructure.client.GeminiGemmaAnalysisClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AnalysisResultMapperTest {

    private GeminiGemmaAnalysisClient client;

    @BeforeEach
    void setUp() {
        client = new GeminiGemmaAnalysisClient(
                "http://localhost:8080/gemini",
                "http://localhost:8080/gemma",
                "dummy-key",
                new ObjectMapper(),
                null
        );
    }

    @Test
    @DisplayName("should parse structured Gemini JSON response into AnalysisResult")
    void shouldParseRawModelResponse() {
        String rawJson = """
                {
                  "candidates": [
                    {
                      "content": {
                        "parts": [
                          {
                            "text": "```json\\n{\\n  \\"audioTranscriptSummary\\": \\"Help voice heard in background\\",\\n  \\"visualContextDescription\\": \\"Car interior night time\\",\\n  \\"suggestedEntityCode\\": \\"comisaria-mujer\\",\\n  \\"threatLevel\\": \\"CRITICAL\\"\\n}\\n```"
                          }
                        ]
                      }
                    }
                  ]
                }
                """;

        AnalysisResult result = client.parseModelResponse(rawJson);

        assertNotNull(result);
        assertEquals("Help voice heard in background", result.audioTranscriptSummary());
        assertEquals("Car interior night time", result.visualContextDescription());
        assertEquals("comisaria-mujer", result.suggestedEntityCode());
        assertEquals("CRITICAL", result.threatLevel());
    }

    @Test
    @DisplayName("should parse a raw JSON response with no markdown fences")
    void shouldParseResponseWithoutMarkdownFences() {
        String rawJson = """
                {
                  "candidates": [
                    {
                      "content": {
                        "parts": [
                          {
                            "text": "{\\"audioTranscriptSummary\\": \\"Auxilio, por favor ayudenme\\", \\"visualContextDescription\\": \\"Sin evidencia visual adjunta\\", \\"suggestedEntityCode\\": \\"linea-100\\", \\"threatLevel\\": \\"HIGH\\"}"
                          }
                        ]
                      }
                    }
                  ]
                }
                """;

        AnalysisResult result = client.parseModelResponse(rawJson);

        assertEquals("Auxilio, por favor ayudenme", result.audioTranscriptSummary());
        assertEquals("Sin evidencia visual adjunta", result.visualContextDescription());
        assertEquals("linea-100", result.suggestedEntityCode());
    }

    @Test
    @DisplayName("should fail loudly instead of fabricating a result when the response is unusable")
    void shouldThrowOnUnparseableResponse() {
        assertThrows(IllegalStateException.class,
                () -> client.parseModelResponse("{\"error\": {\"code\": 404}}"));
    }

    @Test
    @DisplayName("should refuse to analyze when the API key is missing")
    void shouldRejectMissingApiKey() {
        GeminiGemmaAnalysisClient keyless = new GeminiGemmaAnalysisClient(
                "http://localhost:8080/gemini", "http://localhost:8080/gemma", "",
                new ObjectMapper(), null);

        assertThrows(IllegalStateException.class,
                () -> keyless.analyzeIncident("incident-1", List.of()));
    }
}

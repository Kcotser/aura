package com.aura.analysis;

import com.aura.analysis.domain.model.AnalysisResult;
import com.aura.analysis.infrastructure.client.GeminiGemmaAnalysisClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AnalysisResultMapperTest {

    private GeminiGemmaAnalysisClient client;

    @BeforeEach
    void setUp() {
        client = new GeminiGemmaAnalysisClient(
                "http://localhost:8080/dummy",
                "dummy-key",
                new ObjectMapper()
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
}

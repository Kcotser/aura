package com.aura.analysis.domain.model;

/**
 * Value object encapsulating binary media payload data (front/back video streams, ambient audio)
 * for multimodal processing by Gemma 4.
 */
public record EvidenceMediaPayload(
        String evidenceId,
        String mediaType,    // FRONT_CAMERA, BACK_CAMERA, AMBIENT_AUDIO
        String contentType,  // video/mp4, audio/m4a, audio/aac
        byte[] data
) {
    public boolean hasData() {
        return data != null && data.length > 0;
    }
}

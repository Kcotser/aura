package com.aura.emergencyactivation;

import com.aura.emergencyactivation.application.usecase.ActivateIncidentUseCase;
import com.aura.emergencyactivation.application.usecase.GetIncidentUseCase;
import com.aura.emergencyactivation.domain.model.Incident;
import com.aura.emergencyactivation.domain.model.IncidentStatus;
import com.aura.evidence.application.usecase.UploadEvidenceUseCase;
import com.aura.evidence.domain.model.EvidenceAsset;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
@DisplayName("Emergency Activation & Evidence Cross-Context Integration Test")
class EmergencyActivationAndEvidenceIntegrationTest {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0");

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @Autowired
    private ActivateIncidentUseCase activateIncidentUseCase;

    @Autowired
    private UploadEvidenceUseCase uploadEvidenceUseCase;

    @Autowired
    private GetIncidentUseCase getIncidentUseCase;

    @Test
    @DisplayName("Full flow: activate incident -> upload 3 streams -> verify incident transitions automatically via listeners")
    void testEndToEndActivationAndEvidenceUpload() throws Exception {
        String testUserId = "user-integration-test-99";

        // Step 1: Activate incident
        Incident incident = activateIncidentUseCase.execute(testUserId, -12.046374, -77.042793);
        assertNotNull(incident.getId());
        assertEquals(IncidentStatus.ACTIVATED, incident.getStatus());

        // Step 2: Prepare 3 mock evidence files
        MockMultipartFile frontCam = new MockMultipartFile(
                "frontCamera", "front_stream.mp4", "video/mp4", "fake front video stream data".getBytes()
        );
        MockMultipartFile backCam = new MockMultipartFile(
                "backCamera", "back_stream.mp4", "video/mp4", "fake back video stream data".getBytes()
        );
        MockMultipartFile audio = new MockMultipartFile(
                "ambientAudio", "audio_stream.aac", "audio/aac", "fake ambient audio stream data".getBytes()
        );

        // Step 3: Upload evidence files
        List<EvidenceAsset> uploadedAssets = uploadEvidenceUseCase.execute(
                incident.getId().getValue(),
                frontCam,
                backCam,
                audio
        );

        assertEquals(3, uploadedAssets.size());

        // Step 4: Verify Incident state in DB automatically transitioned via listeners
        Incident updatedIncident = getIncidentUseCase.execute(incident.getId().getValue(), testUserId);
        assertNotNull(updatedIncident);
        assertNotEquals(IncidentStatus.ACTIVATED, updatedIncident.getStatus());
    }
}

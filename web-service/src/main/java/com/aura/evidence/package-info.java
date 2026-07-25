/**
 * Evidence bounded context.
 *
 * <p>Handles ingestion of multimedia evidence metadata.
 * Stores only metadata and a reference to external object storage (S3/GCS)—
 * never the binary content directly in MongoDB.
 *
 * TODO: implement after hackathon
 */
@org.springframework.modulith.ApplicationModule(displayName = "Evidence")
package com.aura.evidence;

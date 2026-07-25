package com.aura.evidence.domain.repository;

import com.aura.evidence.domain.model.StorageReference;

import java.io.InputStream;

/**
 * Domain port for storing and retrieving binary media streams.
 *
 * <p>Hackathon decision: Backed by MongoDB GridFS.
 * Production note: For production scale, an object storage service such as AWS S3 or Azure Blob Storage
 * is recommended for cost and performance optimization.
 */
public interface MediaStorageService {

    StorageReference store(String filename, String contentType, InputStream data);

    InputStream load(StorageReference reference);

    void delete(StorageReference reference);
}

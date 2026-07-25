package com.aura.evidence.infrastructure.storage;

import com.aura.evidence.domain.model.StorageReference;
import com.aura.evidence.domain.repository.MediaStorageService;
import com.aura.shared.domain.exception.ResourceNotFoundException;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;

import java.io.InputStream;

/**
 * GridFS implementation of {@link MediaStorageService}.
 *
 * <p><strong>Hackathon Decision:</strong> GridFS is used for media storage in this MVP demo to avoid setting up
 * external S3 credentials under time constraints.
 *
 * <p><strong>Production Recommendation:</strong> In production at scale, migrate to a dedicated object storage
 * service (e.g., AWS S3 or Azure Blob Storage) for improved cost efficiency and lower I/O load on MongoDB.
 */
@Service
public class GridFsMediaStorageService implements MediaStorageService {

    private final GridFsTemplate gridFsTemplate;

    public GridFsMediaStorageService(GridFsTemplate gridFsTemplate) {
        this.gridFsTemplate = gridFsTemplate;
    }

    @Override
    public StorageReference store(String filename, String contentType, InputStream data) {
        ObjectId fileId = gridFsTemplate.store(data, filename, contentType);
        return new StorageReference(fileId.toHexString(), filename);
    }

    @Override
    public InputStream load(StorageReference reference) {
        GridFSFile gridFsFile = gridFsTemplate.findOne(
                new Query(Criteria.where("_id").is(new ObjectId(reference.getGridFsFileId())))
        );
        if (gridFsFile == null) {
            throw new ResourceNotFoundException("GridFSFile", reference.getGridFsFileId());
        }
        GridFsResource resource = gridFsTemplate.getResource(gridFsFile);
        try {
            return resource.getInputStream();
        } catch (Exception e) {
            throw new RuntimeException("Failed to read GridFS stream: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(StorageReference reference) {
        gridFsTemplate.delete(new Query(Criteria.where("_id").is(new ObjectId(reference.getGridFsFileId()))));
    }
}

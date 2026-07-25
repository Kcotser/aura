package com.aura.evidence.domain.model;

import com.aura.shared.domain.exception.BusinessRuleViolationException;

import java.util.Objects;

public class StorageReference {

    private final String gridFsFileId;
    private final String filename;

    public StorageReference(String gridFsFileId, String filename) {
        if (gridFsFileId == null || gridFsFileId.isBlank()) {
            throw new BusinessRuleViolationException("GridFS file ID must not be blank");
        }
        this.gridFsFileId = gridFsFileId.trim();
        this.filename = filename;
    }

    public String getGridFsFileId() {
        return gridFsFileId;
    }

    public String getFilename() {
        return filename;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StorageReference that = (StorageReference) o;
        return Objects.equals(gridFsFileId, that.gridFsFileId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(gridFsFileId);
    }
}

package com.aura.directory.infrastructure.persistence;

import com.aura.directory.domain.model.InstitutionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * MongoDB document representing an institution in the "institutions" collection.
 */
@Document(collection = "institutions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstitutionDocument {

    @Id
    private String id;

    @Indexed(unique = true)
    private String code;

    private String name;
    private InstitutionType type;
    private String phoneNumber;
    private String address;
    private Double latitude;
    private Double longitude;
}

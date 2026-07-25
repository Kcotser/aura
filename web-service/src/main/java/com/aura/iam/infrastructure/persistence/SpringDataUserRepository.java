package com.aura.iam.infrastructure.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

/**
 * Spring Data MongoDB repository interface for {@link UserDocument}.
 * Used internally by {@link MongoUserRepositoryAdapter}.
 */
interface SpringDataUserRepository extends MongoRepository<UserDocument, String> {

    Optional<UserDocument> findByEmail(String email);

    boolean existsByEmail(String email);
}

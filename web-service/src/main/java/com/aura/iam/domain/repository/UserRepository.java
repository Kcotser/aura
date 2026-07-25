package com.aura.iam.domain.repository;

import com.aura.iam.domain.model.User;
import com.aura.iam.domain.model.UserId;
import com.aura.shared.domain.model.Email;

import java.util.Optional;

/**
 * Repository port (interface) for User persistence.
 *
 * <p>This interface lives in the domain layer and defines the contract.
 * The actual MongoDB implementation is in the infrastructure layer.
 * The domain layer has zero knowledge of MongoDB.
 */
public interface UserRepository {

    /** Persists a new or updated User. */
    User save(User user);

    /** Finds a User by their unique identifier. */
    Optional<User> findById(UserId id);

    /** Finds a User by their email address. */
    Optional<User> findByEmail(Email email);

    /** Returns true if any User with the given email already exists. */
    boolean existsByEmail(Email email);
}

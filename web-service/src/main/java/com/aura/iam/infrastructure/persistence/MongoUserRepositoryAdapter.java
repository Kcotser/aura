package com.aura.iam.infrastructure.persistence;

import com.aura.iam.domain.model.User;
import com.aura.iam.domain.model.UserId;
import com.aura.iam.domain.repository.UserRepository;
import com.aura.shared.domain.model.Email;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Adapter implementing the domain {@link UserRepository} port using
 * Spring Data MongoDB.
 *
 * <p>This class bridges the domain layer and MongoDB — it translates
 * between domain objects and MongoDB documents using {@link UserDocumentMapper}.
 */
@Repository
public class MongoUserRepositoryAdapter implements UserRepository {

    private final SpringDataUserRepository springDataRepository;

    public MongoUserRepositoryAdapter(SpringDataUserRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public User save(User user) {
        UserDocument doc = UserDocumentMapper.toDocument(user);
        UserDocument saved = springDataRepository.save(doc);
        return UserDocumentMapper.toDomain(saved);
    }

    @Override
    public Optional<User> findById(UserId id) {
        return springDataRepository.findById(id.value())
                .map(UserDocumentMapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(Email email) {
        return springDataRepository.findByEmail(email.value())
                .map(UserDocumentMapper::toDomain);
    }

    @Override
    public boolean existsByEmail(Email email) {
        return springDataRepository.existsByEmail(email.value());
    }
}

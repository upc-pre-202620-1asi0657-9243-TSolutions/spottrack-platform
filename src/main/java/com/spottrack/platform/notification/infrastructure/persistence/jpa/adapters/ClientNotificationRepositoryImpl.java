package com.spottrack.platform.notification.infrastructure.persistence.jpa.adapters;

import com.spottrack.platform.notification.domain.model.aggregates.ClientNotification;
import com.spottrack.platform.notification.domain.repositories.ClientNotificationRepository;
import com.spottrack.platform.notification.infrastructure.persistence.jpa.assemblers.ClientNotificationPersistenceAssembler;
import com.spottrack.platform.notification.infrastructure.persistence.jpa.repositories.ClientNotificationPersistenceRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ClientNotificationRepositoryImpl implements ClientNotificationRepository {

    private final ClientNotificationPersistenceRepository persistenceRepository;

    public ClientNotificationRepositoryImpl(ClientNotificationPersistenceRepository persistenceRepository) {
        this.persistenceRepository = persistenceRepository;
    }

    @Override
    public Optional<ClientNotification> findById(Long id) {
        return persistenceRepository.findById(id)
                .map(ClientNotificationPersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public List<ClientNotification> findAllByClientId(Long clientId, boolean unreadOnly) {
        var entities = unreadOnly
                ? persistenceRepository.findAllByClientIdAndReadAtIsNullOrderByCreatedAtDesc(clientId)
                : persistenceRepository.findAllByClientIdOrderByCreatedAtDesc(clientId);
        return entities.stream()
                .map(ClientNotificationPersistenceAssembler::toDomainFromPersistence)
                .toList();
    }

    @Override
    public ClientNotification save(ClientNotification notification) {
        var saved = persistenceRepository.save(
                ClientNotificationPersistenceAssembler.toPersistenceFromDomain(notification));
        return ClientNotificationPersistenceAssembler.toDomainFromPersistence(saved);
    }
}

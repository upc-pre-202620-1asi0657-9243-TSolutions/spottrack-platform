package com.spottrack.platform.profiles.infrastructure.persistence.jpa.adapters;

import com.spottrack.platform.profiles.domain.model.aggregates.Client;
import com.spottrack.platform.profiles.domain.model.valueobjects.ClientId;
import com.spottrack.platform.profiles.domain.model.valueobjects.EmailAddress;
import com.spottrack.platform.profiles.domain.repositories.ClientRepository;
import com.spottrack.platform.profiles.infrastructure.persistence.jpa.assemblers.ClientPersistenceAssembler;
import com.spottrack.platform.profiles.infrastructure.persistence.jpa.repositories.ClientPersistenceRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class ClientRepositoryImpl implements ClientRepository {

    private final ClientPersistenceRepository clientPersistenceRepository;
    private final ApplicationEventPublisher eventPublisher;

    public ClientRepositoryImpl(
            ClientPersistenceRepository clientPersistenceRepository,
            ApplicationEventPublisher eventPublisher) {
        this.clientPersistenceRepository = clientPersistenceRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Optional<Client> findById(ClientId clientId) {
        return clientPersistenceRepository.findById(clientId.clientId())
                .map(ClientPersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public Optional<Client> findByUserId(Long userId) {
        return clientPersistenceRepository.findByUserId(userId)
                .map(ClientPersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public Optional<Client> findByEmailAddress(EmailAddress emailAddress) {
        return clientPersistenceRepository.findByEmailAddress(emailAddress)
                .map(ClientPersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public Client save(Client client) {
        boolean isNew = client.getId() == null;
        var savedEntity = clientPersistenceRepository.save(ClientPersistenceAssembler.toPersistenceFromDomain(client));
        var savedClient = ClientPersistenceAssembler.toDomainFromPersistence(savedEntity);
        if (isNew) {
            savedClient.onCreated();
            savedClient.domainEvents().forEach(eventPublisher::publishEvent);
            savedClient.clearDomainEvents();
        }
        return savedClient;
    }

    @Override
    public boolean existsByEmailAddress(EmailAddress emailAddress) {
        return clientPersistenceRepository.countByEmailAddress(emailAddress) > 0;
    }
}

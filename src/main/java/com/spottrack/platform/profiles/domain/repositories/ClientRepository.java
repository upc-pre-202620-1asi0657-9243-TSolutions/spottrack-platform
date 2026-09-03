package com.spottrack.platform.profiles.domain.repositories;

import com.spottrack.platform.profiles.domain.model.aggregates.Client;
import com.spottrack.platform.profiles.domain.model.valueobjects.ClientId;
import com.spottrack.platform.profiles.domain.model.valueobjects.EmailAddress;

import java.util.Optional;

public interface ClientRepository {
    Optional<Client> findById(ClientId clientId);

    Optional<Client> findByUserId(Long userId);

    Optional<Client> findByEmailAddress(EmailAddress emailAddress);

    Client save(Client client);

    boolean existsByEmailAddress(EmailAddress emailAddress);
}
package com.spottrack.platform.profiles.application.internal.queryservices;

import com.spottrack.platform.gym.interfaces.acl.GymContextFacade;
import com.spottrack.platform.profiles.application.queryservices.ClientQueryService;
import com.spottrack.platform.profiles.domain.model.aggregates.Client;
import com.spottrack.platform.profiles.domain.model.entities.ClientGymAssociation;
import com.spottrack.platform.profiles.domain.model.queries.GetClientByEmailQuery;
import com.spottrack.platform.profiles.domain.model.queries.GetClientByIdQuery;
import com.spottrack.platform.profiles.domain.model.queries.GetClientByUserIdQuery;
import com.spottrack.platform.profiles.domain.model.queries.GetClientGymAssociationsQuery;
import com.spottrack.platform.profiles.domain.repositories.ClientRepository;
import com.spottrack.platform.profiles.infrastructure.persistence.jpa.repositories.ClientGymAssociationPersistenceRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ClientQueryServiceImpl implements ClientQueryService {
    private final ClientRepository clientRepository;
    private final ClientGymAssociationPersistenceRepository associationRepository;
    private final GymContextFacade gymContextFacade;

    public ClientQueryServiceImpl(ClientRepository clientRepository,
                                  ClientGymAssociationPersistenceRepository associationRepository,
                                  GymContextFacade gymContextFacade) {
        this.clientRepository = clientRepository;
        this.associationRepository = associationRepository;
        this.gymContextFacade = gymContextFacade;
    }

    @Override
    public Optional<Client> handle(GetClientByIdQuery query) {
        return clientRepository.findById(query.clientId());
    }

    @Override
    public Optional<Client> handle(GetClientByUserIdQuery query) {
        return clientRepository.findByUserId(query.userId());
    }

    @Override
    public Optional<Client> handle(GetClientByEmailQuery query) {
        return clientRepository.findByEmailAddress(query.emailAddress());
    }

    @Override
    public List<ClientGymAssociation> handle(GetClientGymAssociationsQuery query) {
        var clientOpt = clientRepository.findById(new com.spottrack.platform.profiles.domain.model.valueobjects.ClientId(query.clientId()));
        if (clientOpt.isEmpty() || !clientOpt.get().isProfileComplete()) return List.of();
        var dni = clientOpt.get().getPersonInfo().dni().getDNI();
        return associationRepository.findByClientId(query.clientId()).stream()
                .filter(e -> gymContextFacade.isDniWhitelistedForGym(e.getGymId(), dni))
                .map(e -> new ClientGymAssociation(e.getId(), e.getClientId(), e.getGymId(), e.isActive()))
                .toList();
    }
}

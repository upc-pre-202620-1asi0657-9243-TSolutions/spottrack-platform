package com.spottrack.platform.profiles.application.queryservices;

import com.spottrack.platform.profiles.domain.model.aggregates.Client;
import com.spottrack.platform.profiles.domain.model.entities.ClientGymAssociation;
import com.spottrack.platform.profiles.domain.model.queries.GetClientByEmailQuery;
import com.spottrack.platform.profiles.domain.model.queries.GetClientByIdQuery;
import com.spottrack.platform.profiles.domain.model.queries.GetClientByUserIdQuery;
import com.spottrack.platform.profiles.domain.model.queries.GetClientGymAssociationsQuery;

import java.util.List;
import java.util.Optional;

public interface ClientQueryService {
    Optional<Client> handle(GetClientByIdQuery query);
    Optional<Client> handle(GetClientByUserIdQuery query);
    Optional<Client> handle(GetClientByEmailQuery query);
    List<ClientGymAssociation> handle(GetClientGymAssociationsQuery query);
}

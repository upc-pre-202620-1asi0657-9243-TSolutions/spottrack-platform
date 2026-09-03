package com.spottrack.platform.gym.application.queryservices;

import com.spottrack.platform.gym.domain.model.aggregates.Gym;
import com.spottrack.platform.gym.domain.model.entities.Branch;
import com.spottrack.platform.gym.domain.model.entities.GymWhitelistEntry;
import com.spottrack.platform.gym.domain.model.entities.Zone;
import com.spottrack.platform.gym.domain.model.queries.GetAllGymsQuery;
import com.spottrack.platform.gym.domain.model.queries.GetBranchesByGymIdQuery;
import com.spottrack.platform.gym.domain.model.queries.GetGymById;
import com.spottrack.platform.gym.domain.model.queries.GetGymsByAdminUserId;
import com.spottrack.platform.gym.domain.model.queries.GetWhitelistByGymIdQuery;
import com.spottrack.platform.gym.domain.model.queries.GetZonesByGymIdQuery;

import java.util.List;
import java.util.Optional;

public interface GymQueryService {
    Optional<Gym> handle(GetGymById query);
    List<Gym> handle(GetGymsByAdminUserId query);
    List<Gym> handle(GetAllGymsQuery query);
    List<GymWhitelistEntry> handle(GetWhitelistByGymIdQuery query);
    List<Branch> handle(GetBranchesByGymIdQuery query);
    List<Zone> handle(GetZonesByGymIdQuery query);
}

package com.spottrack.platform.shared.interfaces.rest.guards;

import com.spottrack.platform.gym.interfaces.acl.GymContextFacade;
import com.spottrack.platform.membership.interfaces.acl.MembershipContextFacade;
import com.spottrack.platform.profiles.interfaces.acl.ProfilesContextFacade;
import com.spottrack.platform.shared.application.result.ApplicationError;
import com.spottrack.platform.shared.interfaces.rest.transform.ErrorResponseAssembler;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class GymMembershipAccessGuard {

    private final ProfilesContextFacade profilesContextFacade;
    private final GymContextFacade gymContextFacade;
    private final MembershipContextFacade membershipContextFacade;

    public GymMembershipAccessGuard(ProfilesContextFacade profilesContextFacade,
                                    GymContextFacade gymContextFacade,
                                    MembershipContextFacade membershipContextFacade) {
        this.profilesContextFacade = profilesContextFacade;
        this.gymContextFacade = gymContextFacade;
        this.membershipContextFacade = membershipContextFacade;
    }

    public Optional<ResponseEntity<?>> check(Long clientId) {
        var activeGymId = profilesContextFacade.fetchActiveGymIdByClientId(clientId);
        if (activeGymId.isBlank()) {
            return blocked("membership.error.access.noGym");
        }
        var adminUserId = gymContextFacade.fetchAdminUserIdByGymId(activeGymId);
        if (adminUserId == 0L) {
            return blocked("membership.error.access.noGym");
        }
        var accessStatus = membershipContextFacade.fetchMembershipAccessStatus(adminUserId);
        if ("ACTIVE".equals(accessStatus)) return Optional.empty();
        var errorCode = "SUSPENDED".equals(accessStatus)
                ? "membership.error.access.suspended"
                : "membership.error.access.inactive";
        return blocked(errorCode);
    }

    private Optional<ResponseEntity<?>> blocked(String errorCode) {
        return Optional.of(ErrorResponseAssembler.toErrorResponseFromApplicationError(
                ApplicationError.forbidden("Membership", errorCode)));
    }
}

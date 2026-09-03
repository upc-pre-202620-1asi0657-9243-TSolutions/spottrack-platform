package com.spottrack.platform.iam.interfaces.rest.transform;

import com.spottrack.platform.iam.domain.model.aggregates.User;
import com.spottrack.platform.iam.interfaces.rest.resources.AuthenticatedUserResource;
import org.apache.commons.lang3.tuple.ImmutablePair;

public class AuthenticatedUserResourceFromEntityAssembler {

    public static AuthenticatedUserResource toResourceFromEntity(ImmutablePair<User, String> pair) {
        return new AuthenticatedUserResource(
                pair.getLeft().getId(),
                pair.getLeft().getUsername(),
                pair.getRight()
        );
    }
}

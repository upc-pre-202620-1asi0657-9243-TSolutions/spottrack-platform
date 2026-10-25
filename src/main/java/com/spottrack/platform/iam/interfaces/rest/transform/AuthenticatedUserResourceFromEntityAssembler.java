package com.spottrack.platform.iam.interfaces.rest.transform;

import com.spottrack.platform.iam.domain.model.aggregates.User;
import com.spottrack.platform.iam.domain.model.entities.Role;
import com.spottrack.platform.iam.interfaces.rest.resources.AuthenticatedUserResource;
import org.apache.commons.lang3.tuple.ImmutablePair;

public class AuthenticatedUserResourceFromEntityAssembler {

    public static AuthenticatedUserResource toResourceFromEntity(ImmutablePair<User, String> pair) {
        var user = pair.getLeft();
        String role = user.getRoles().stream()
                .findFirst()
                .map(Role::getStringName)
                .orElse(null);
        return new AuthenticatedUserResource(
                user.getId(),
                user.getUsername(),
                role,
                pair.getRight()
        );
    }
}

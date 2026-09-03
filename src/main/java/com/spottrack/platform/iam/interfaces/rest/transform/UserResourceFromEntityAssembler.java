package com.spottrack.platform.iam.interfaces.rest.transform;

import com.spottrack.platform.iam.domain.model.aggregates.User;
import com.spottrack.platform.iam.interfaces.rest.resources.UserResource;

import java.util.List;

public class UserResourceFromEntityAssembler {

    public static UserResource toResourceFromEntity(User user) {
        List<String> roles = user.getRoles().stream()
                .map(role -> role.getStringName())
                .toList();
        return new UserResource(
                user.getId(),
                user.getUsername(),
                roles,
                user.isNotifyOnCritical(),
                user.isNotifyOnWarning(),
                user.getNotificationEmail()
        );
    }
}

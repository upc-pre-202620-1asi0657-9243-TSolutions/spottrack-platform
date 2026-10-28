package com.spottrack.platform.iam.interfaces.rest.transform;

import com.spottrack.platform.iam.domain.model.aggregates.User;
import com.spottrack.platform.iam.interfaces.rest.resources.TechnicianAccountResource;
import org.apache.commons.lang3.tuple.ImmutablePair;

public class TechnicianAccountResourceAssembler {

    public static TechnicianAccountResource toResource(ImmutablePair<User, String> account) {
        var user = account.getLeft();
        return new TechnicianAccountResource(
                user.getId(),
                user.getUsername(),
                user.getRoles().stream().map(role -> role.getStringName()).toList(),
                account.getRight());
    }
}

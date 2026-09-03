package com.spottrack.platform.membership.domain.model.commands;

import com.spottrack.platform.membership.domain.model.valueobjects.MembershipId;

public record ActivateMembershipCommand(MembershipId membershipId) {
    public ActivateMembershipCommand {
        if (membershipId == null) throw new IllegalArgumentException("membership.error.membershipId.notNull");
    }
}

package com.spottrack.platform.membership.domain.model.commands;

import com.spottrack.platform.membership.domain.model.valueobjects.MembershipId;

public record SuspendMembershipCommand(MembershipId membershipId) {
    public SuspendMembershipCommand {
        if (membershipId == null) throw new IllegalArgumentException("membership.error.membershipId.notNull");
    }
}

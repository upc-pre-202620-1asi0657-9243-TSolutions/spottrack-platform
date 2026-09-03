package com.spottrack.platform.membership.domain.model.aggregates;

import com.spottrack.platform.membership.domain.model.commands.GrantBranchAccessCommand;
import com.spottrack.platform.membership.domain.model.events.BranchAccessDeniedEvent;
import com.spottrack.platform.membership.domain.model.events.BranchAccessGrantedEvent;
import com.spottrack.platform.membership.domain.model.valueobjects.BranchAccessId;
import com.spottrack.platform.membership.domain.model.valueobjects.BranchAccessStatus;
import com.spottrack.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import lombok.Getter;
import lombok.Setter;

public class BranchAccess extends AbstractDomainAggregateRoot<BranchAccess> {

    @Getter
    @Setter
    private Long id;

    @Getter
    private BranchAccessId branchAccessId;

    @Getter
    private Long membershipId;

    @Getter
    private Long branchId;

    @Getter
    private BranchAccessStatus status;

    public BranchAccess(Long id, BranchAccessId branchAccessId, Long membershipId, Long branchId, BranchAccessStatus status) {
        this.id = id;
        this.branchAccessId = branchAccessId;
        this.membershipId = membershipId;
        this.branchId = branchId;
        this.status = status;
    }

    public BranchAccess(GrantBranchAccessCommand command, BranchAccessStatus status) {
        this(null, new BranchAccessId(), command.membershipId(), command.branchId(), status);
    }

    public void onCreated() {
        if (this.status == BranchAccessStatus.GRANTED) {
            registerDomainEvent(BranchAccessGrantedEvent.from(this));
        } else {
            registerDomainEvent(BranchAccessDeniedEvent.from(this));
        }
    }
}

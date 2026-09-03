package com.spottrack.platform.membership.domain.model.aggregates;

import com.spottrack.platform.membership.domain.model.commands.CreateMembershipCommand;
import com.spottrack.platform.membership.domain.model.commands.RenewMembershipCommand;
import com.spottrack.platform.membership.domain.model.commands.RequestDowngradePlanCommand;
import com.spottrack.platform.membership.domain.model.commands.UpgradeMembershipPlanCommand;
import com.spottrack.platform.membership.domain.model.events.GymMembershipActivatedEvent;
import com.spottrack.platform.membership.domain.model.events.GymMembershipRenewedEvent;
import com.spottrack.platform.membership.domain.model.events.MembershipCancellationRequestedEvent;
import com.spottrack.platform.membership.domain.model.events.MembershipCancellationUndoneEvent;
import com.spottrack.platform.membership.domain.model.events.MembershipCancelledEvent;
import com.spottrack.platform.membership.domain.model.events.MembershipCreatedEvent;
import com.spottrack.platform.membership.domain.model.events.MembershipDowngradeRequestedEvent;
import com.spottrack.platform.membership.domain.model.events.MembershipExpiredEvent;
import com.spottrack.platform.membership.domain.model.events.MembershipSuspendedEvent;
import com.spottrack.platform.membership.domain.model.events.PlanDowngradedEvent;
import com.spottrack.platform.membership.domain.model.events.PlanUpgradedEvent;
import java.time.LocalDate;
import com.spottrack.platform.membership.domain.model.valueobjects.*;
import com.spottrack.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import com.spottrack.platform.shared.domain.model.valueobjects.Money;
import lombok.Getter;
import lombok.Setter;

public class Membership extends AbstractDomainAggregateRoot<Membership> {

    @Getter
    @Setter
    private Long id;

    @Getter
    private MembershipId membershipId;

    @Getter
    private Long clientId;

    @Getter
    MembershipTier membershipTier;

    @Getter
    private Money price;

    @Getter
    private MembershipPeriod membershipPeriod;

    @Getter
    private MembershipStatus status;

    @Getter
    private boolean cancelAtPeriodEnd;

    @Getter
    private MembershipTier pendingDowngradeTier;

    public Membership(Long id, MembershipId membershipId, Long clientId, MembershipTier membershipTier,
                      Money price, MembershipPeriod membershipPeriod, MembershipStatus status,
                      boolean cancelAtPeriodEnd, MembershipTier pendingDowngradeTier) {
        this.id = id;
        this.membershipId = membershipId;
        this.clientId = clientId;
        this.membershipTier = membershipTier;
        this.price = price;
        this.membershipPeriod = membershipPeriod;
        this.status = status;
        this.cancelAtPeriodEnd = cancelAtPeriodEnd;
        this.pendingDowngradeTier = pendingDowngradeTier;
    }

    public Membership(CreateMembershipCommand command) {
        this(null,
                new MembershipId(),
                command.clientId(),
                command.membershipTier(),
                command.price(),
                new MembershipPeriod(command.startDate(), command.endDate()),
                MembershipStatus.PENDING_ACTIVATION,
                false,
                null);
    }

    public void activate() {
        if (this.status != MembershipStatus.PENDING_ACTIVATION && this.status != MembershipStatus.SUSPENDED) {
            throw new IllegalStateException("membership.error.activate.invalidStatus");
        }
        this.status = MembershipStatus.ACTIVE;
        registerDomainEvent(GymMembershipActivatedEvent.from(this));
    }

    public void cancel() {
        if (this.status != MembershipStatus.ACTIVE) {
            throw new IllegalStateException("membership.error.cancel.notActive");
        }
        if (this.cancelAtPeriodEnd) {
            throw new IllegalStateException("membership.error.cancel.alreadyScheduled");
        }
        this.cancelAtPeriodEnd = true;
        this.pendingDowngradeTier = null;
        registerDomainEvent(MembershipCancellationRequestedEvent.from(this));
    }

    public void undoCancellation() {
        if (this.status != MembershipStatus.ACTIVE) {
            throw new IllegalStateException("membership.error.undoCancellation.notActive");
        }
        if (!this.cancelAtPeriodEnd) {
            throw new IllegalStateException("membership.error.undoCancellation.notScheduled");
        }
        this.cancelAtPeriodEnd = false;
        registerDomainEvent(MembershipCancellationUndoneEvent.from(this));
    }

    public void completeCancellation() {
        if (!this.cancelAtPeriodEnd) {
            throw new IllegalStateException("membership.error.cancel.notScheduled");
        }
        this.status = MembershipStatus.CANCELLED;
        this.cancelAtPeriodEnd = false;
        registerDomainEvent(MembershipCancelledEvent.from(this));
    }

    public void suspend() {
        if (this.status != MembershipStatus.ACTIVE) {
            throw new IllegalStateException("membership.error.suspend.notActive");
        }
        this.status = MembershipStatus.SUSPENDED;
        registerDomainEvent(MembershipSuspendedEvent.from(this));
    }

    public void requestDowngrade(RequestDowngradePlanCommand command) {
        if (this.status != MembershipStatus.ACTIVE) {
            throw new IllegalStateException("membership.error.downgrade.notActive");
        }
        if (command.newTier().toMoney().amount()
                .compareTo(this.membershipTier.toMoney().amount()) >= 0) {
            throw new IllegalStateException("membership.error.downgrade.notLowerTier");
        }
        this.pendingDowngradeTier = command.newTier();
        this.cancelAtPeriodEnd = false;
        registerDomainEvent(MembershipDowngradeRequestedEvent.from(this));
    }

    public void applyDowngrade() {
        if (this.pendingDowngradeTier == null) {
            throw new IllegalStateException("membership.error.downgrade.noPending");
        }
        if (this.status != MembershipStatus.ACTIVE) {
            throw new IllegalStateException("membership.error.downgrade.notActive");
        }
        this.membershipTier = this.pendingDowngradeTier;
        this.price = this.pendingDowngradeTier.toMoney();
        this.pendingDowngradeTier = null;
        var today = LocalDate.now();
        this.membershipPeriod = new MembershipPeriod(today, today.plusDays(30));
        registerDomainEvent(PlanDowngradedEvent.from(this));
    }

    public void upgradePlan(UpgradeMembershipPlanCommand command) {
        if (this.status != MembershipStatus.ACTIVE) {
            throw new IllegalStateException("membership.error.upgrade.notActive");
        }
        if (command.newMembershipTier().toMoney().amount()
                .compareTo(this.membershipTier.toMoney().amount()) <= 0) {
            throw new IllegalStateException("membership.error.upgrade.notHigherTier");
        }
        this.membershipTier = command.newMembershipTier();
        this.price = command.newMembershipTier().toMoney();
        var today = LocalDate.now();
        this.membershipPeriod = new MembershipPeriod(today, today.plusDays(30));
        this.cancelAtPeriodEnd = false;
        registerDomainEvent(PlanUpgradedEvent.from(this));
    }

    public void renew(RenewMembershipCommand command) {
        if (this.status != MembershipStatus.SUSPENDED) {
            throw new IllegalStateException("membership.error.renew.notSuspended");
        }
        this.membershipPeriod = new MembershipPeriod(command.startDate(), command.endDate());
        this.status = MembershipStatus.ACTIVE;
        this.cancelAtPeriodEnd = false;
        registerDomainEvent(GymMembershipRenewedEvent.from(this));
    }

    public void expire() {
        if (this.status != MembershipStatus.ACTIVE && this.status != MembershipStatus.SUSPENDED) {
            throw new IllegalStateException("membership.error.expire.invalidStatus");
        }
        this.status = MembershipStatus.EXPIRED;
        registerDomainEvent(MembershipExpiredEvent.from(this));
    }

    public void onCreated() {
        registerDomainEvent(MembershipCreatedEvent.from(this));
    }
}

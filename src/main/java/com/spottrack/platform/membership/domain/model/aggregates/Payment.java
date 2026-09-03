package com.spottrack.platform.membership.domain.model.aggregates;

import com.spottrack.platform.membership.domain.model.commands.InitiateBusinessPaymentCommand;
import com.spottrack.platform.membership.domain.model.commands.InitiateDebtPaymentCommand;
import com.spottrack.platform.membership.domain.model.commands.InitiateResubscriptionPaymentCommand;
import com.spottrack.platform.membership.domain.model.commands.InitiateUpgradePaymentCommand;
import com.spottrack.platform.membership.domain.model.commands.PayMembershipCommand;
import com.spottrack.platform.membership.domain.model.events.MembershipPayedEvent;
import com.spottrack.platform.membership.domain.model.events.PaymentConfirmedEvent;
import com.spottrack.platform.membership.domain.model.events.PaymentFailedEvent;
import com.spottrack.platform.membership.domain.model.valueobjects.MembershipTier;
import com.spottrack.platform.membership.domain.model.valueobjects.PaymentId;
import com.spottrack.platform.membership.domain.model.valueobjects.PaymentPurpose;
import com.spottrack.platform.membership.domain.model.valueobjects.PaymentStatus;
import com.spottrack.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import com.spottrack.platform.shared.domain.model.valueobjects.Money;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

public class Payment extends AbstractDomainAggregateRoot<Payment> {

    @Getter
    @Setter
    private Long id;

    @Getter
    private PaymentId paymentId;

    @Getter
    private Long userId;

    @Getter
    private UUID pendingRegistrationId;

    @Getter
    private UUID membershipId;

    @Getter
    private MembershipTier membershipTier;

    @Getter
    private Money amount;

    @Getter
    private PaymentStatus status;

    @Getter
    private String gatewayTransactionId;

    @Getter
    private PaymentPurpose paymentPurpose;

    public Payment(Long id, PaymentId paymentId, Long userId, UUID pendingRegistrationId,
                   UUID membershipId, MembershipTier membershipTier, Money amount,
                   PaymentStatus status, String gatewayTransactionId, PaymentPurpose paymentPurpose) {
        this.id = id;
        this.paymentId = paymentId;
        this.userId = userId;
        this.pendingRegistrationId = pendingRegistrationId;
        this.membershipId = membershipId;
        this.membershipTier = membershipTier;
        this.amount = amount;
        this.status = status;
        this.gatewayTransactionId = gatewayTransactionId;
        this.paymentPurpose = paymentPurpose;
    }

    public Payment(PayMembershipCommand command) {
        this(null, new PaymentId(), command.userId(), null, null,
                command.membershipTier(), command.amount(), PaymentStatus.PENDING, null, PaymentPurpose.NEW_MEMBERSHIP);
    }

    public Payment(InitiateBusinessPaymentCommand command) {
        this(null, new PaymentId(), null, command.pendingRegistrationId(), null,
                command.membershipTier(), command.amount(), PaymentStatus.PENDING, null, PaymentPurpose.BUSINESS_REGISTRATION);
    }

    public Payment(InitiateDebtPaymentCommand command) {
        this(null, new PaymentId(), null, null, command.membershipId(),
                command.membershipTier(), command.amount(), PaymentStatus.PENDING, null, PaymentPurpose.DEBT_RENEWAL);
    }

    public Payment(InitiateUpgradePaymentCommand command) {
        this(null, new PaymentId(), null, null, command.membershipId(),
                command.newMembershipTier(), command.amount(), PaymentStatus.PENDING, null, PaymentPurpose.PLAN_UPGRADE);
    }

    public Payment(InitiateResubscriptionPaymentCommand command) {
        this(null, new PaymentId(), command.userId(), null, null,
                command.membershipTier(), command.amount(), PaymentStatus.PENDING, null, PaymentPurpose.RESUBSCRIPTION);
    }

    public void confirm(String gatewayTransactionId) {
        if (this.status != PaymentStatus.PENDING) {
            throw new IllegalStateException("membership.error.payment.confirm.notPending");
        }
        this.gatewayTransactionId = gatewayTransactionId;
        this.status = PaymentStatus.CONFIRMED;
        registerDomainEvent(PaymentConfirmedEvent.from(this));
    }

    public void fail() {
        if (this.status != PaymentStatus.PENDING) {
            throw new IllegalStateException("membership.error.payment.fail.notPending");
        }
        this.status = PaymentStatus.FAILED;
        registerDomainEvent(PaymentFailedEvent.from(this));
    }

    public void onCreated() {
        registerDomainEvent(MembershipPayedEvent.from(this));
    }
}

package com.spottrack.platform.membership.interfaces.rest;

import com.spottrack.platform.membership.application.commandservices.MembershipCommandService;
import com.spottrack.platform.membership.application.commandservices.PaymentCommandService;
import com.spottrack.platform.membership.application.queryservices.MembershipQueryService;
import com.spottrack.platform.membership.domain.model.commands.CancelMembershipCommand;
import com.spottrack.platform.membership.domain.model.commands.UndoCancellationCommand;
import com.spottrack.platform.membership.domain.model.commands.InitiateDebtPaymentCommand;
import com.spottrack.platform.membership.domain.model.commands.InitiateResubscriptionPaymentCommand;
import com.spottrack.platform.membership.domain.model.commands.InitiateUpgradePaymentCommand;
import com.spottrack.platform.membership.domain.model.commands.RequestDowngradePlanCommand;
import com.spottrack.platform.membership.domain.model.queries.GetMembershipByIdQuery;
import com.spottrack.platform.membership.domain.model.queries.GetMembershipsByClientIdQuery;
import com.spottrack.platform.membership.domain.model.queries.GetPrimaryMembershipByClientIdQuery;
import com.spottrack.platform.membership.domain.model.valueobjects.MembershipId;
import com.spottrack.platform.membership.domain.model.valueobjects.MembershipStatus;
import com.spottrack.platform.membership.domain.model.valueobjects.MembershipTier;
import com.spottrack.platform.membership.interfaces.rest.resources.CreateMembershipResource;
import com.spottrack.platform.membership.interfaces.rest.resources.MembershipResource;
import com.spottrack.platform.membership.interfaces.rest.resources.DowngradeMembershipPlanResource;
import com.spottrack.platform.membership.interfaces.rest.resources.ResubscribeMembershipResource;
import com.spottrack.platform.membership.interfaces.rest.resources.UpgradeMembershipPlanResource;
import com.spottrack.platform.membership.interfaces.rest.transform.CreateMembershipCommandFromResourceAssembler;
import com.spottrack.platform.membership.interfaces.rest.transform.MembershipResourceFromEntityAssembler;
import com.spottrack.platform.iam.interfaces.acl.IamContextFacade;
import com.spottrack.platform.shared.application.result.ApplicationError;
import com.spottrack.platform.shared.domain.model.valueobjects.Money;
import com.spottrack.platform.shared.interfaces.rest.transform.ErrorResponseAssembler;
import com.spottrack.platform.shared.interfaces.rest.transform.ResponseEntityAssembler;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/memberships")
public class MembershipController {

    private final MembershipCommandService membershipCommandService;
    private final MembershipQueryService membershipQueryService;
    private final PaymentCommandService paymentCommandService;
    private final IamContextFacade iamContextFacade;

    public MembershipController(
            MembershipCommandService membershipCommandService,
            MembershipQueryService membershipQueryService,
            PaymentCommandService paymentCommandService,
            IamContextFacade iamContextFacade) {
        this.membershipCommandService = membershipCommandService;
        this.membershipQueryService = membershipQueryService;
        this.paymentCommandService = paymentCommandService;
        this.iamContextFacade = iamContextFacade;
    }

    @PostMapping
    @Schema(description = "Create a new membership for the authenticated caller")
    public ResponseEntity<?> createMembership(Authentication authentication, @RequestBody @Valid CreateMembershipResource resource) {
        var clientId = resolveClientId(authentication);
        if (clientId == 0L) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("Client", authentication.getName()));
        }
        var command = CreateMembershipCommandFromResourceAssembler.toCommandFromResource(clientId, resource);
        var result = membershipCommandService.handle(command);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                MembershipResourceFromEntityAssembler::toResourceFromEntity,
                HttpStatus.CREATED
        );
    }

    @GetMapping("/me")
    @Schema(description = "Get the primary membership of the authenticated admin")
    public ResponseEntity<?> getMyMembership(Authentication authentication) {
        var clientId = resolveClientId(authentication);
        if (clientId == 0L) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("Client", authentication.getName()));
        }
        return membershipQueryService.handle(new GetPrimaryMembershipByClientIdQuery(clientId))
                .<ResponseEntity<?>>map(m -> ResponseEntity.ok(MembershipResourceFromEntityAssembler.toResourceFromEntity(m)))
                .orElseGet(() -> ErrorResponseAssembler.toErrorResponseFromApplicationError(
                        ApplicationError.notFound("Membership", "clientId:" + clientId)));
    }

    @PatchMapping("/{membershipId}/cancel")
    @Schema(description = "Request cancellation of a membership at the end of the current billing period")
    public ResponseEntity<?> cancelMembership(
            Authentication authentication,
            @PathVariable UUID membershipId) {
        var clientId = resolveClientId(authentication);
        if (clientId == 0L) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("Client", authentication.getName()));
        }
        var membership = membershipQueryService.handle(new GetMembershipByIdQuery(new MembershipId(membershipId)));
        if (membership.isEmpty()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("Membership", membershipId.toString()));
        }
        var ownershipError = checkOwnership(membership.get().getClientId(), clientId, membershipId.toString());
        if (ownershipError.isPresent()) return ownershipError.get();
        var command = new CancelMembershipCommand(new MembershipId(membershipId));
        var result = membershipCommandService.handle(command);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                MembershipResourceFromEntityAssembler::toResourceFromEntity,
                HttpStatus.OK
        );
    }

    @PatchMapping("/{membershipId}/undo-cancel")
    @Schema(description = "Undo a scheduled cancellation for an active membership with cancelAtPeriodEnd=true")
    public ResponseEntity<?> undoCancelMembership(
            Authentication authentication,
            @PathVariable UUID membershipId) {
        var clientId = resolveClientId(authentication);
        if (clientId == 0L) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("Client", authentication.getName()));
        }
        var membership = membershipQueryService.handle(new GetMembershipByIdQuery(new MembershipId(membershipId)));
        if (membership.isEmpty()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("Membership", membershipId.toString()));
        }
        var ownershipError = checkOwnership(membership.get().getClientId(), clientId, membershipId.toString());
        if (ownershipError.isPresent()) return ownershipError.get();
        var command = new UndoCancellationCommand(new MembershipId(membershipId));
        var result = membershipCommandService.handle(command);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                MembershipResourceFromEntityAssembler::toResourceFromEntity,
                HttpStatus.OK
        );
    }

    @PostMapping("/{membershipId}/pay-debt")
    @Schema(description = "Initiate a debt regularization payment for a suspended membership")
    public ResponseEntity<?> payDebt(
            Authentication authentication,
            @PathVariable UUID membershipId) {
        var clientId = resolveClientId(authentication);
        if (clientId == 0L) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("Client", authentication.getName()));
        }
        var membership = membershipQueryService.handle(new GetMembershipByIdQuery(new MembershipId(membershipId)));
        if (membership.isEmpty()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("Membership", membershipId.toString()));
        }
        var ownershipError = checkOwnership(membership.get().getClientId(), clientId, membershipId.toString());
        if (ownershipError.isPresent()) return ownershipError.get();
        if (membership.get().getStatus() != MembershipStatus.SUSPENDED) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.businessRuleViolation("Membership.payDebt",
                            "membership.error.payDebt.notSuspended"));
        }
        var tier = membership.get().getMembershipTier();
        var command = new InitiateDebtPaymentCommand(membershipId, tier, tier.toMoney());
        var result = paymentCommandService.handle(command);
        return switch (result) {
            case com.spottrack.platform.shared.application.result.Result.Success<String, ?> s ->
                    ResponseEntity.ok(Map.of("checkoutUrl", s.value()));
            case com.spottrack.platform.shared.application.result.Result.Failure<?, ApplicationError> f ->
                    ErrorResponseAssembler.toErrorResponseFromApplicationError(f.error());
        };
    }

    @PostMapping("/{membershipId}/downgrade-plan")
    @Schema(description = "Schedule a plan downgrade to take effect at the end of the current billing period")
    public ResponseEntity<?> downgradePlan(
            Authentication authentication,
            @PathVariable UUID membershipId,
            @RequestBody @Valid DowngradeMembershipPlanResource resource) {
        var clientId = resolveClientId(authentication);
        if (clientId == 0L) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("Client", authentication.getName()));
        }
        var membership = membershipQueryService.handle(new GetMembershipByIdQuery(new MembershipId(membershipId)));
        if (membership.isEmpty()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("Membership", membershipId.toString()));
        }
        var ownershipError = checkOwnership(membership.get().getClientId(), clientId, membershipId.toString());
        if (ownershipError.isPresent()) return ownershipError.get();
        if (membership.get().getStatus() != MembershipStatus.ACTIVE) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.businessRuleViolation("Membership.downgradePlan",
                            "membership.error.downgrade.notActive"));
        }
        MembershipTier newTier;
        try {
            newTier = MembershipTier.valueOf(resource.newMembershipTier());
        } catch (IllegalArgumentException e) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.validationError("MembershipTier", "membership.error.downgrade.invalidTier"));
        }
        if (newTier.toMoney().amount().compareTo(membership.get().getMembershipTier().toMoney().amount()) >= 0) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.businessRuleViolation("Membership.downgradePlan",
                            "membership.error.downgrade.notLowerTier"));
        }
        var command = new RequestDowngradePlanCommand(new MembershipId(membershipId), newTier);
        var result = membershipCommandService.handle(command);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                MembershipResourceFromEntityAssembler::toResourceFromEntity,
                HttpStatus.OK
        );
    }

    @PostMapping("/{membershipId}/upgrade-plan")
    @Schema(description = "Initiate a plan upgrade payment for an active membership")
    public ResponseEntity<?> upgradePlan(
            Authentication authentication,
            @PathVariable UUID membershipId,
            @RequestBody @Valid UpgradeMembershipPlanResource resource) {
        var clientId = resolveClientId(authentication);
        if (clientId == 0L) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("Client", authentication.getName()));
        }
        var membership = membershipQueryService.handle(new GetMembershipByIdQuery(new MembershipId(membershipId)));
        if (membership.isEmpty()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("Membership", membershipId.toString()));
        }
        var ownershipError = checkOwnership(membership.get().getClientId(), clientId, membershipId.toString());
        if (ownershipError.isPresent()) return ownershipError.get();
        if (membership.get().getStatus() != MembershipStatus.ACTIVE) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.businessRuleViolation("Membership.upgradePlan",
                            "membership.error.upgrade.notActive"));
        }
        MembershipTier newTier;
        try {
            newTier = MembershipTier.valueOf(resource.newMembershipTier());
        } catch (IllegalArgumentException e) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.validationError("MembershipTier", "membership.error.upgrade.invalidTier"));
        }
        if (newTier.toMoney().amount().compareTo(membership.get().getMembershipTier().toMoney().amount()) <= 0) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.businessRuleViolation("Membership.upgradePlan",
                            "membership.error.upgrade.notHigherTier"));
        }
        var currentTierMoney = membership.get().getMembershipTier().toMoney();
        var differenceAmount = newTier.toMoney().amount().subtract(currentTierMoney.amount());
        var command = new InitiateUpgradePaymentCommand(membershipId, newTier, new Money(differenceAmount, newTier.toMoney().currency()));
        var result = paymentCommandService.handle(command);
        return switch (result) {
            case com.spottrack.platform.shared.application.result.Result.Success<String, ?> s ->
                    ResponseEntity.ok(Map.of("checkoutUrl", s.value()));
            case com.spottrack.platform.shared.application.result.Result.Failure<?, ApplicationError> f ->
                    ErrorResponseAssembler.toErrorResponseFromApplicationError(f.error());
        };
    }

    @PostMapping("/resubscribe")
    @Schema(description = "Initiate a resubscription payment for a client with a CANCELLED or EXPIRED membership")
    public ResponseEntity<?> resubscribe(
            Authentication authentication,
            @RequestBody @Valid ResubscribeMembershipResource resource) {
        var clientId = resolveClientId(authentication);
        if (clientId == 0L) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("Client", authentication.getName()));
        }
        var memberships = membershipQueryService.handle(new GetMembershipsByClientIdQuery(clientId));
        boolean hasActive = memberships.stream().anyMatch(m -> m.getStatus() == MembershipStatus.ACTIVE);
        if (hasActive) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.businessRuleViolation("Membership.resubscribe",
                            "membership.error.resubscribe.alreadyActive"));
        }
        boolean hasSuspended = memberships.stream().anyMatch(m -> m.getStatus() == MembershipStatus.SUSPENDED);
        if (hasSuspended) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.businessRuleViolation("Membership.resubscribe",
                            "membership.error.resubscribe.suspended"));
        }
        MembershipTier tier;
        try {
            tier = MembershipTier.valueOf(resource.membershipTier());
        } catch (IllegalArgumentException e) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.validationError("MembershipTier", "membership.error.resubscribe.invalidTier"));
        }
        var command = new InitiateResubscriptionPaymentCommand(clientId, tier, tier.toMoney());
        var result = paymentCommandService.handle(command);
        return switch (result) {
            case com.spottrack.platform.shared.application.result.Result.Success<String, ?> s ->
                    ResponseEntity.ok(Map.of("checkoutUrl", s.value()));
            case com.spottrack.platform.shared.application.result.Result.Failure<?, ApplicationError> f ->
                    ErrorResponseAssembler.toErrorResponseFromApplicationError(f.error());
        };
    }

    private Long resolveClientId(Authentication authentication) {
        return iamContextFacade.fetchUserIdByUsername(authentication.getName()).orElse(0L);
    }

    private Optional<ResponseEntity<?>> checkOwnership(Long membershipClientId, Long callerClientId, String membershipId) {
        if (!membershipClientId.equals(callerClientId)) {
            return Optional.of(ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.forbidden("Membership", "membershipId:" + membershipId)));
        }
        return Optional.empty();
    }
}

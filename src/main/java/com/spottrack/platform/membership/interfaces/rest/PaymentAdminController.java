package com.spottrack.platform.membership.interfaces.rest;

import com.spottrack.platform.membership.domain.model.events.PaymentConfirmedEvent;
import com.spottrack.platform.membership.domain.model.valueobjects.PaymentId;
import com.spottrack.platform.membership.domain.model.valueobjects.PaymentStatus;
import com.spottrack.platform.membership.domain.repositories.PaymentRepository;
import com.spottrack.platform.shared.application.result.ApplicationError;
import com.spottrack.platform.shared.interfaces.rest.transform.ErrorResponseAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/payments")
@Tag(name = "Payment Admin", description = "Internal admin operations for payment management")
public class PaymentAdminController {

    private final PaymentRepository paymentRepository;
    private final ApplicationEventPublisher eventPublisher;

    public PaymentAdminController(PaymentRepository paymentRepository,
                                  ApplicationEventPublisher eventPublisher) {
        this.paymentRepository = paymentRepository;
        this.eventPublisher = eventPublisher;
    }

    @PostMapping("/{paymentId}/reprocess")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Reprocess a confirmed payment",
            description = "Re-publishes the PaymentConfirmedEvent for a CONFIRMED payment whose downstream " +
                    "provisioning (account, profiles, membership) failed. Safe to call multiple times — " +
                    "each downstream step is idempotent."
    )
    public ResponseEntity<?> reprocessPayment(@PathVariable UUID paymentId) {
        var payment = paymentRepository.findByPaymentId(new PaymentId(paymentId));
        if (payment.isEmpty()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("Payment", paymentId.toString()));
        }

        var p = payment.get();
        if (p.getStatus() != PaymentStatus.CONFIRMED) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.businessRuleViolation(
                            "Payment.reprocess",
                            "Only CONFIRMED payments can be reprocessed. Current status: " + p.getStatus()));
        }

        eventPublisher.publishEvent(PaymentConfirmedEvent.from(p));
        return ResponseEntity.ok(Map.of(
                "paymentId", paymentId,
                "status", "reprocess_triggered"
        ));
    }
}

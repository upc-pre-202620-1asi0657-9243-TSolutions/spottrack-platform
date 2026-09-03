package com.spottrack.platform.membership.interfaces.rest;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.spottrack.platform.membership.application.commandservices.PaymentCommandService;
import com.spottrack.platform.membership.domain.model.commands.ConfirmPaymentCommand;
import com.spottrack.platform.membership.domain.model.commands.FailPaymentCommand;
import com.spottrack.platform.membership.domain.model.valueobjects.PaymentId;
import com.spottrack.platform.shared.application.result.ApplicationError;
import com.spottrack.platform.shared.application.result.Result;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/webhooks")
@Tag(name = "Webhooks", description = "Stripe webhook event receiver")
public class StripeWebhookController {

    private static final ObjectMapper MAPPER = JsonMapper.builder().build();

    private final PaymentCommandService paymentCommandService;

    @Value("${stripe.webhook-secret}")
    private String webhookSecret;

    public StripeWebhookController(PaymentCommandService paymentCommandService) {
        this.paymentCommandService = paymentCommandService;
    }

    @PostMapping("/stripe")
    public ResponseEntity<Void> handleStripeEvent(
            @RequestBody byte[] payload,
            @RequestHeader("Stripe-Signature") String sigHeader
    ) {
        Event event;
        try {
            event = Webhook.constructEvent(
                    new String(payload, StandardCharsets.UTF_8), sigHeader, webhookSecret
            );
        } catch (SignatureVerificationException e) {
            log.warn("Stripe webhook signature verification failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }

        switch (event.getType()) {
            case "checkout.session.completed" -> handleSessionCompleted(event);
            case "checkout.session.expired" -> handleSessionExpired(event);
            default -> log.debug("Unhandled Stripe event type: {}", event.getType());
        }

        return ResponseEntity.ok().build();
    }

    /**
     * Extracts [sessionId, paymentId] from a Stripe event.
     * Falls back to raw JSON parsing when the SDK cannot deserialize the event due to an
     * API version mismatch (e.g. Stripe CLI sends 2026-04-22.dahlia but SDK knows an older version).
     */
    private String[] resolveSessionData(Event event) {
        var deserializer = event.getDataObjectDeserializer();

        if (deserializer.getObject().isPresent()) {
            var session = (Session) deserializer.getObject().get();
            var paymentIdStr = session.getMetadata() != null ? session.getMetadata().get("paymentId") : null;
            return new String[]{session.getId(), paymentIdStr};
        }

        var rawJson = deserializer.getRawJson();
        if (rawJson == null || rawJson.isBlank()) {
            log.error("No raw JSON available for Stripe event {}", event.getId());
            return null;
        }

        log.debug("SDK could not deserialize event {} — falling back to raw JSON parsing", event.getId());
        try {   
            var node = MAPPER.readTree(rawJson);
            var sessionId = node.path("id").asText(null);
            var paymentIdStr = node.path("metadata").path("paymentId").asText(null);
            if (sessionId == null || sessionId.isBlank()) {
                log.error("No session id found in raw JSON for event {}", event.getId());
                return null;
            }
            return new String[]{sessionId, paymentIdStr};
        } catch (Exception e) {
            log.error("Failed to parse raw Stripe session JSON for event {}", event.getId(), e);
            return null;
        }
    }

    private void handleSessionCompleted(Event event) {
        var sessionData = resolveSessionData(event);
        if (sessionData == null) {
            log.error("Could not resolve session data for Stripe event {}", event.getId());
            return;
        }

        var sessionId = sessionData[0];
        var paymentIdStr = sessionData[1];

        if (paymentIdStr == null) {
            log.error("Missing paymentId in metadata for Stripe session {}", sessionId);
            return;
        }

        try {
            var paymentId = new PaymentId(UUID.fromString(paymentIdStr));
            var command = new ConfirmPaymentCommand(paymentId, sessionId);
            var result = paymentCommandService.handle(command);

            if (result instanceof Result.Failure<?, ?> failure && failure.error() instanceof ApplicationError error) {
                log.error("Failed to confirm payment {}: {}", paymentIdStr, error.message());
            } else {
                log.info("Payment {} confirmed via Stripe session {}", paymentIdStr, sessionId);
            }
        } catch (IllegalArgumentException e) {
            log.error("Invalid paymentId UUID in Stripe session metadata: {}", paymentIdStr);
        }
    }

    private void handleSessionExpired(Event event) {
        var sessionData = resolveSessionData(event);
        if (sessionData == null) {
            log.error("Could not resolve session data for Stripe event {}", event.getId());
            return;
        }

        var sessionId = sessionData[0];
        var paymentIdStr = sessionData[1];

        if (paymentIdStr == null) {
            log.warn("Missing paymentId in metadata for expired Stripe session {}", sessionId);
            return;
        }

        try {
            var paymentId = new PaymentId(UUID.fromString(paymentIdStr));
            var command = new FailPaymentCommand(paymentId);
            var result = paymentCommandService.handle(command);

            if (result instanceof Result.Failure<?, ?> failure && failure.error() instanceof ApplicationError error) {
                log.error("Failed to mark payment {} as failed: {}", paymentIdStr, error.message());
            } else {
                log.info("Payment {} marked as failed due to expired Stripe session {}", paymentIdStr, sessionId);
            }
        } catch (IllegalArgumentException e) {
            log.error("Invalid paymentId UUID in expired Stripe session metadata: {}", paymentIdStr);
        }
    }
}

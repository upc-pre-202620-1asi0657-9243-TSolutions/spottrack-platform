package com.spottrack.platform.membership.interfaces.rest;


import com.spottrack.platform.membership.application.commandservices.PaymentCommandService;
import com.spottrack.platform.membership.interfaces.rest.resources.PayMembershipResource;
import com.spottrack.platform.membership.interfaces.rest.transform.PayMembershipCommandFromResourceAssembler;
import com.spottrack.platform.shared.interfaces.rest.transform.ResponseEntityAssembler;
import com.spottrack.platform.shared.interfaces.rest.resources.ErrorResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments")
@Tag(name = "Payments", description = "Payment management endpoints, uses STRIPE")
public class PaymentController {
    private final PaymentCommandService paymentCommandService;
    public PaymentController(PaymentCommandService paymentCommandService){
        this.paymentCommandService = paymentCommandService;
    }

    @Operation(
            summary = "Start a membership payment",
            description = "Records a pending payment for the given user and membership tier and returns the Stripe "
                    + "Checkout URL. The membership is created when Stripe confirms the payment through the webhook."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Stripe Checkout session created; the body contains checkoutUrl"),
            @ApiResponse(responseCode = "400", description = "Validation error (missing userId or unknown membership tier)",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid authentication token",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class))),
            @ApiResponse(responseCode = "500", description = "Stripe Checkout session could not be created",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class)))
    })
    @PostMapping
    public ResponseEntity<?> payMembership(@Valid @RequestBody PayMembershipResource resource){
        var command = PayMembershipCommandFromResourceAssembler.toCommandFromResource(resource);
        var result = paymentCommandService.handle(command);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result, url -> Map.of("checkoutUrl", url), HttpStatus.CREATED
        );
    }
}

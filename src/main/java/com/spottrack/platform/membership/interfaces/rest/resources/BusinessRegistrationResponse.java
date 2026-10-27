package com.spottrack.platform.membership.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Result of a business registration: where to pay and which pending registration it refers to")
public record BusinessRegistrationResponse(
        @Schema(description = "Stripe Checkout URL the visitor must open to pay", example = "https://checkout.stripe.com/c/pay/cs_test_a1b2c3")
        String checkoutUrl,
        @Schema(description = "Identifier of the pending registration", example = "3f6c1d1e-9b7a-4a52-8c0e-5d2f6a7b9c10")
        String pendingRegistrationId
) {}

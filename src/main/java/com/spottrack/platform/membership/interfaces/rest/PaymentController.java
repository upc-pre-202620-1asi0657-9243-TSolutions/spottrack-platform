package com.spottrack.platform.membership.interfaces.rest;


import com.spottrack.platform.membership.application.commandservices.PaymentCommandService;
import com.spottrack.platform.membership.interfaces.rest.resources.PayMembershipResource;
import com.spottrack.platform.membership.interfaces.rest.transform.PayMembershipCommandFromResourceAssembler;
import com.spottrack.platform.shared.interfaces.rest.transform.ResponseEntityAssembler;
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

    @PostMapping
    public ResponseEntity<?> payMembership(@Valid @RequestBody PayMembershipResource resource){
        var command = PayMembershipCommandFromResourceAssembler.toCommandFromResource(resource);
        var result = paymentCommandService.handle(command);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result, url -> Map.of("checkoutUrl", url), HttpStatus.CREATED
        );
    }
}

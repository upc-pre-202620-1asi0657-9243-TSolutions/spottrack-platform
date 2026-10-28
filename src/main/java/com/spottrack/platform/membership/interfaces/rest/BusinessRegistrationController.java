package com.spottrack.platform.membership.interfaces.rest;

import com.spottrack.platform.iam.domain.model.commands.SavePendingRegistrationCommand;
import com.spottrack.platform.iam.interfaces.acl.IamContextFacade;
import com.spottrack.platform.membership.application.commandservices.PaymentCommandService;
import com.spottrack.platform.membership.domain.model.commands.InitiateBusinessPaymentCommand;
import com.spottrack.platform.membership.domain.model.valueobjects.MembershipTier;
import com.spottrack.platform.membership.interfaces.rest.resources.BusinessRegistrationResource;
import com.spottrack.platform.membership.interfaces.rest.resources.BusinessRegistrationResponse;
import com.spottrack.platform.shared.interfaces.rest.transform.ResponseEntityAssembler;
import com.spottrack.platform.shared.interfaces.rest.resources.ErrorResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;

@RestController
@RequestMapping("/api/v1/register-business")
@Tag(name = "Business Registration", description = "Public endpoint to register a gym owner and initiate payment")
public class BusinessRegistrationController {

    private final IamContextFacade iamContextFacade;
    private final PaymentCommandService paymentCommandService;

    public BusinessRegistrationController(IamContextFacade iamContextFacade,
                                          PaymentCommandService paymentCommandService) {
        this.iamContextFacade = iamContextFacade;
        this.paymentCommandService = paymentCommandService;
    }

    @Operation(
            summary = "Register a business and start the initial payment",
            description = "Public endpoint. Stores a pending registration (valid for 30 minutes) with the owner and "
                    + "company data and returns a Stripe Checkout URL for the selected plan (BASIC, MID or PLATINUM; "
                    + "case-insensitive). The administrator account, profiles, membership and gym are only created "
                    + "once Stripe confirms the payment through the webhook."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Pending registration created and Stripe Checkout session returned",
                    content = @Content(schema = @Schema(implementation = BusinessRegistrationResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error (blank field or unknown membership tier)",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class))),
            @ApiResponse(responseCode = "409", description = "A registration for this email already exists",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class))),
            @ApiResponse(responseCode = "500", description = "Stripe Checkout session could not be created",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class)))
    })
    @PostMapping
    public ResponseEntity<?> registerBusiness(@Valid @RequestBody BusinessRegistrationResource resource) {
        var tier = MembershipTier.valueOf(resource.membershipTier().trim().toUpperCase(Locale.ROOT));

        var savePendingCommand = new SavePendingRegistrationCommand(
                resource.email(),
                resource.password(),
                resource.firstName(),
                resource.lastName(),
                resource.phoneNumber(),
                resource.dni(),
                resource.companyName(),
                resource.ruc(),
                resource.legalStructure(),
                resource.companyPhone(),
                resource.companyEmail(),
                resource.streetAddress(),
                resource.city(),
                resource.district(),
                tier.name()
        );

        var result = iamContextFacade.savePendingRegistration(savePendingCommand)
                .flatMap(pendingId -> {
                    var paymentCommand = new InitiateBusinessPaymentCommand(
                            pendingId,
                            tier,
                            tier.toMoney()
                    );
                    return paymentCommandService.handle(paymentCommand)
                            .map(checkoutUrl -> new BusinessRegistrationResponse(checkoutUrl, pendingId.toString()));
                });

        return ResponseEntityAssembler.toResponseEntityFromResult(result, response -> response, HttpStatus.CREATED);
    }
}

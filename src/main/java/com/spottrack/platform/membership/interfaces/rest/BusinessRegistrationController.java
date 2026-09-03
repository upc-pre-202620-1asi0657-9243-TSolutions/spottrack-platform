package com.spottrack.platform.membership.interfaces.rest;

import com.spottrack.platform.iam.domain.model.commands.SavePendingRegistrationCommand;
import com.spottrack.platform.iam.interfaces.acl.IamContextFacade;
import com.spottrack.platform.membership.application.commandservices.PaymentCommandService;
import com.spottrack.platform.membership.domain.model.commands.InitiateBusinessPaymentCommand;
import com.spottrack.platform.membership.domain.model.valueobjects.MembershipTier;
import com.spottrack.platform.membership.interfaces.rest.resources.BusinessRegistrationResource;
import com.spottrack.platform.membership.interfaces.rest.resources.BusinessRegistrationResponse;
import com.spottrack.platform.shared.interfaces.rest.transform.ResponseEntityAssembler;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @PostMapping
    public ResponseEntity<?> registerBusiness(@Valid @RequestBody BusinessRegistrationResource resource) {
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
                resource.membershipTier()
        );

        var tier = MembershipTier.valueOf(resource.membershipTier());

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

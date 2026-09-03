package com.spottrack.platform.analytics.interfaces.rest;

import com.spottrack.platform.analytics.application.commandservices.MaintenanceQuoteCommandService;
import com.spottrack.platform.analytics.application.queryservices.MaintenanceQuoteQueryService;
import com.spottrack.platform.analytics.domain.model.aggregates.MaintenanceQuote;
import com.spottrack.platform.analytics.domain.model.commands.RequestADetailedMaintenanceQuoteCommand;
import com.spottrack.platform.analytics.domain.model.commands.RequestMaintenanceCostCommand;
import com.spottrack.platform.analytics.domain.model.commands.RequestPreventiveCostCommand;
import com.spottrack.platform.analytics.domain.model.commands.RequestSparePartsCommand;
import com.spottrack.platform.analytics.domain.model.queries.GetAllMaintenanceQuotesQuery;
import com.spottrack.platform.analytics.interfaces.rest.resources.MaintenanceQuoteResource;
import com.spottrack.platform.analytics.interfaces.rest.transform.MaintenanceQuoteResourceFromEntityAssembler;
import com.spottrack.platform.gym.interfaces.acl.GymContextFacade;
import com.spottrack.platform.iam.interfaces.acl.IamContextFacade;
import com.spottrack.platform.shared.application.result.ApplicationError;
import com.spottrack.platform.shared.application.result.Result;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/api/v1/maintenance-quotes", produces = MediaType.APPLICATION_JSON_VALUE)
public class MaintenanceQuotesController {
    private final MaintenanceQuoteCommandService maintenanceQuoteCommandService;
    private final MaintenanceQuoteQueryService maintenanceQuoteQueryService;
    private final GymContextFacade gymContextFacade;
    private final IamContextFacade iamContextFacade;

    public MaintenanceQuotesController(MaintenanceQuoteCommandService maintenanceQuoteCommandService,
                                        MaintenanceQuoteQueryService maintenanceQuoteQueryService,
                                        GymContextFacade gymContextFacade,
                                        IamContextFacade iamContextFacade) {
        this.maintenanceQuoteCommandService = maintenanceQuoteCommandService;
        this.maintenanceQuoteQueryService = maintenanceQuoteQueryService;
        this.gymContextFacade = gymContextFacade;
        this.iamContextFacade = iamContextFacade;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MaintenanceQuoteResource> createQuote(@RequestBody RequestADetailedMaintenanceQuoteCommand command) {
        var quote = maintenanceQuoteCommandService.handle(command);
        return quote.map(value -> new ResponseEntity<>(MaintenanceQuoteResourceFromEntityAssembler.toResourceFromEntity(value), HttpStatus.CREATED))
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<MaintenanceQuoteResource> getAllMaintenanceQuotes() {
        return maintenanceQuoteQueryService.handle(new GetAllMaintenanceQuotesQuery()).stream()
                .map(MaintenanceQuoteResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('ADMIN')")
    public List<MaintenanceQuoteResource> getMyMaintenanceQuotes(Authentication authentication) {
        var adminUserId = iamContextFacade.fetchUserIdByUsername(authentication.getName()).orElse(0L);
        Set<String> myEquipmentIds = gymContextFacade.findEquipmentsByAdminUserId(adminUserId).stream()
                .map(equipment -> equipment.getId().uuid())
                .collect(Collectors.toSet());

        return maintenanceQuoteQueryService.handle(new GetAllMaintenanceQuotesQuery()).stream()
                .filter(quote -> myEquipmentIds.contains(quote.getEquipmentId()))
                .map(MaintenanceQuoteResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
    }

    @PatchMapping("/{id}/spare-parts")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateSpareParts(@PathVariable Long id, @RequestBody RequestSparePartsCommand command) {
        return toResponseEntity(maintenanceQuoteCommandService.handle(id, command));
    }

    @PatchMapping("/{id}/preventive-cost")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updatePreventiveCost(@PathVariable Long id, @RequestBody RequestPreventiveCostCommand command) {
        return toResponseEntity(maintenanceQuoteCommandService.handle(id, command));
    }

    @PatchMapping("/{id}/total-cost")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateTotalCost(@PathVariable Long id, @RequestBody RequestMaintenanceCostCommand command) {
        return toResponseEntity(maintenanceQuoteCommandService.handle(id, command));
    }

    private ResponseEntity<?> toResponseEntity(Result<MaintenanceQuote, ApplicationError> result) {
        return switch (result) {
            case Result.Success<MaintenanceQuote, ApplicationError> s ->
                    ResponseEntity.ok(MaintenanceQuoteResourceFromEntityAssembler.toResourceFromEntity(s.value()));
            case Result.Failure<MaintenanceQuote, ApplicationError> f ->
                    ResponseEntity.status(HttpStatus.NOT_FOUND).body(f.error());
        };
    }
}

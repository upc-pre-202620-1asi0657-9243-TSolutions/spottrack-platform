package com.spottrack.platform.analytics.interfaces.rest;

import com.spottrack.platform.analytics.application.commandservices.ROIProjectionCommandService;
import com.spottrack.platform.analytics.application.queryservices.ROIProjectionQueryService;
import com.spottrack.platform.analytics.domain.model.aggregates.ROIProjection;
import com.spottrack.platform.analytics.domain.model.commands.DetectHighDemandSlotCommand;
import com.spottrack.platform.analytics.domain.model.commands.DetectLowDemandSlotCommand;
import com.spottrack.platform.analytics.domain.model.commands.RecommendTransferCommand;
import com.spottrack.platform.analytics.domain.model.commands.RequestDowntimeCostCommand;
import com.spottrack.platform.analytics.domain.model.commands.RequestEarningsCommand;
import com.spottrack.platform.analytics.domain.model.commands.RequestRoiCommand;
import com.spottrack.platform.analytics.domain.model.queries.GetAllROIProjectionsQuery;
import com.spottrack.platform.analytics.interfaces.rest.resources.ROIProjectionResource;
import com.spottrack.platform.analytics.interfaces.rest.transform.ROIProjectionResourceFromEntityAssembler;
import com.spottrack.platform.shared.application.result.ApplicationError;
import com.spottrack.platform.shared.application.result.Result;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/roi-projections", produces = MediaType.APPLICATION_JSON_VALUE)
public class ROIProjectionsController {
    private final ROIProjectionCommandService roiProjectionCommandService;
    private final ROIProjectionQueryService roiProjectionQueryService;

    public ROIProjectionsController(ROIProjectionCommandService roiProjectionCommandService,
                                     ROIProjectionQueryService roiProjectionQueryService) {
        this.roiProjectionCommandService = roiProjectionCommandService;
        this.roiProjectionQueryService = roiProjectionQueryService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ROIProjectionResource> createROI(@RequestBody RequestRoiCommand command) {
        var roi = roiProjectionCommandService.handle(command);
        return roi.map(value -> new ResponseEntity<>(ROIProjectionResourceFromEntityAssembler.toResourceFromEntity(value), HttpStatus.CREATED))
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<ROIProjectionResource> getAllROIProjections() {
        return roiProjectionQueryService.handle(new GetAllROIProjectionsQuery()).stream()
                .map(ROIProjectionResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
    }

    @PatchMapping("/{id}/downtime-cost")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateDowntimeCost(@PathVariable Long id, @RequestBody RequestDowntimeCostCommand command) {
        return toResponseEntity(roiProjectionCommandService.handle(id, command));
    }

    @PatchMapping("/{id}/earnings")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateEarnings(@PathVariable Long id, @RequestBody RequestEarningsCommand command) {
        return toResponseEntity(roiProjectionCommandService.handle(id, command));
    }

    @PatchMapping("/{id}/detect-low-demand")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> detectLowDemand(@PathVariable Long id, @RequestBody DetectLowDemandSlotCommand command) {
        return toResponseEntity(roiProjectionCommandService.handle(id, command));
    }

    @PatchMapping("/{id}/detect-high-demand")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> detectHighDemand(@PathVariable Long id, @RequestBody DetectHighDemandSlotCommand command) {
        return toResponseEntity(roiProjectionCommandService.handle(id, command));
    }

    @PatchMapping("/{id}/recommend-transfer")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> recommendTransfer(@PathVariable Long id, @RequestBody RecommendTransferCommand command) {
        return toResponseEntity(roiProjectionCommandService.handle(id, command));
    }

    private ResponseEntity<?> toResponseEntity(Result<ROIProjection, ApplicationError> result) {
        return switch (result) {
            case Result.Success<ROIProjection, ApplicationError> s ->
                    ResponseEntity.ok(ROIProjectionResourceFromEntityAssembler.toResourceFromEntity(s.value()));
            case Result.Failure<ROIProjection, ApplicationError> f ->
                    ResponseEntity.status(HttpStatus.NOT_FOUND).body(f.error());
        };
    }
}

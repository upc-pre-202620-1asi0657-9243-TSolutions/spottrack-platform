package com.spottrack.platform.monitoring.interfaces.rest;

import com.spottrack.platform.monitoring.application.commandServices.AnomalyCommandService;
import com.spottrack.platform.monitoring.interfaces.rest.resources.AnomalyResource;
import com.spottrack.platform.monitoring.interfaces.rest.resources.ReportAnomalyCommandResource;
import com.spottrack.platform.monitoring.interfaces.rest.transform.AnomalyResourceFromEntity;
import com.spottrack.platform.monitoring.interfaces.rest.transform.ReportAnomalyCommandFromResource;
import com.spottrack.platform.shared.interfaces.rest.transform.ResponseEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/v1/anomalies", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Anomalies", description = "Anomaly reporting endpoints")
public class AnomalyController {
    private final AnomalyCommandService anomalyCommandService;
    public AnomalyController(AnomalyCommandService anomalyCommandService){
        this.anomalyCommandService = anomalyCommandService;
    }

    @PostMapping
    @Operation(summary = "Report an anomaly", description = "Reports a new equipment anomaly detected during a reservation.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Anomaly reported successfully",
                    content = @Content(schema = @Schema(implementation = AnomalyResource.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    public ResponseEntity<?> reportAnomaly(@RequestBody ReportAnomalyCommandResource resource){
        var command = ReportAnomalyCommandFromResource.toCommandFromResource(resource);
        var result = anomalyCommandService.handle(command);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                AnomalyResourceFromEntity::toResourceFromEntity,
                HttpStatus.CREATED
        );
    }
}

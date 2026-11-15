package com.spottrack.platform.monitoring.interfaces.rest;

import com.spottrack.platform.monitoring.application.commandServices.SensorPairingCommandService;
import com.spottrack.platform.monitoring.application.ports.QrCodeRenderer;
import com.spottrack.platform.monitoring.domain.model.commands.IssueSensorPairingCommand;
import com.spottrack.platform.monitoring.interfaces.rest.resources.SensorPairingResource;
import com.spottrack.platform.monitoring.interfaces.rest.transform.SensorPairingResourceFromIssued;
import com.spottrack.platform.shared.interfaces.rest.transform.ResponseEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.json.JsonMapper;

@RestController
@RequestMapping(value = "/api/v1/monitoring/motion-sensors", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "SensorPairing", description = "QR pairing of motion sensors with the MQTT brokers")
public class SensorPairingController {
    private final SensorPairingCommandService sensorPairingCommandService;
    private final QrCodeRenderer qrCodeRenderer;
    private final JsonMapper jsonMapper;

    public SensorPairingController(SensorPairingCommandService sensorPairingCommandService,
                                   QrCodeRenderer qrCodeRenderer, JsonMapper jsonMapper) {
        this.sensorPairingCommandService = sensorPairingCommandService;
        this.qrCodeRenderer = qrCodeRenderer;
        this.jsonMapper = jsonMapper;
    }

    @PostMapping("/{motionSensorId}/pairing")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Issue a QR pairing for a motion sensor",
            description = "Generates the MQTT credential of the sensor and returns the QR content (JSON) and its PNG. "
                    + "The secret is returned only once and is stored hashed. Regenerating invalidates the previous secret. "
                    + "The QR is valid for pairing.ttl while it has not been scanned (first heartbeat); afterwards the credential stays. "
                    + "The response is not cacheable.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pairing issued",
                    content = @Content(schema = @Schema(implementation = SensorPairingResource.class))),
            @ApiResponse(responseCode = "400", description = "Blank motion sensor id"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid token"),
            @ApiResponse(responseCode = "403", description = "The caller is not an ADMIN"),
            @ApiResponse(responseCode = "404", description = "Motion sensor not found"),
            @ApiResponse(responseCode = "422", description = "mqtt.public-endpoint or mqtt.ca-sha256 are not configured"),
            @ApiResponse(responseCode = "500", description = "The credential could not be provisioned on the brokers; nothing was persisted")
    })
    public ResponseEntity<?> issuePairing(@PathVariable String motionSensorId) {
        var result = sensorPairingCommandService.handle(new IssueSensorPairingCommand(motionSensorId));
        var response = ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                issued -> SensorPairingResourceFromIssued.toResourceFromIssued(issued, jsonMapper, qrCodeRenderer),
                HttpStatus.OK);
        return ResponseEntity.status(response.getStatusCode())
                .cacheControl(CacheControl.noStore())
                .body(response.getBody());
    }
}

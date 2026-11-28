package com.spottrack.platform.iam.interfaces.rest;

import com.spottrack.platform.iam.application.queryservices.RoleQueryService;
import com.spottrack.platform.iam.domain.model.queries.GetAllRolesQuery;
import com.spottrack.platform.iam.interfaces.rest.resources.RoleResource;
import com.spottrack.platform.iam.interfaces.rest.transform.RoleResourceFromEntityAssembler;
import com.spottrack.platform.shared.infrastructure.documentation.openapi.configuration.OpenApiConfiguration;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/roles")
@Tag(name = "Roles", description = "Catalogue of available roles")
public class RolesController {

    private final RoleQueryService roleQueryService;

    public RolesController(RoleQueryService roleQueryService) {
        this.roleQueryService = roleQueryService;
    }

    @Operation(summary = "List roles", description = "Returns every role an account can hold.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Roles found",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = RoleResource.class)))),
            @ApiResponse(responseCode = "401", ref = OpenApiConfiguration.RESPONSES_REF + "Unauthorized")
    })
    @GetMapping
    public ResponseEntity<List<RoleResource>> getAllRoles() {
        var roles = roleQueryService.handle(new GetAllRolesQuery());
        var resources = roles.stream()
                .map(RoleResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(resources);
    }
}

package com.spottrack.platform.shared.infrastructure.documentation.openapi.configuration;

import com.spottrack.platform.shared.interfaces.rest.resources.ErrorResource;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Configures the OpenAPI specification exposed by the platform.
 */
@Configuration
public class OpenApiConfiguration {
    /** Reference prefix for the reusable responses registered in {@link #errorResponses()}. */
    public static final String RESPONSES_REF = "#/components/responses/";

    private static final String JSON = "application/json";
    private static final String ERROR_RESOURCE_REF = "#/components/schemas/ErrorResource";

    // Properties
    @Value("${spring.application.name}")
    String applicationName;

    @Value("${documentation.application.description}")
    String applicationDescription;

    @Value("${documentation.application.version}")
    String applicationVersion;

    // Methods

    /**
     * Builds the OpenAPI document used by Swagger UI and client generation tools.
     *
     * @return configured OpenAPI descriptor
     */
    @Bean
    public OpenAPI learningPlatformOpenApi() {

        // General configuration
        var openApi = new OpenAPI();
        openApi
                .info(new Info()
                        .title(this.applicationName)
                        .description(this.applicationDescription)
                        .version(this.applicationVersion)
                        .contact(new Contact()
                                .name("SpotTrack Support")
                                .email("support@spottrack.com")
                                .url("https://spottrack.com/support"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .externalDocs(new ExternalDocumentation()
                        .description("SpotTrack Platform Documentation")
                        .url("https://spottrack.com/docs"));

        // Add server configurations
        openApi.servers(List.of(
                new Server().url("/").description("Current environment")
        ));

        // Add a security scheme
       final String securitySchemeName = "bearerAuth";

       var components = new Components()
               .addSecuritySchemes(securitySchemeName,
                       new SecurityScheme()
                               .name(securitySchemeName)
                               .type(SecurityScheme.Type.HTTP)
                               .scheme("bearer")
                               .bearerFormat("JWT")
                               .description("JWT Bearer token for API authentication"));

       // Reusable error schema and responses shared by every bounded context
       ModelConverters.getInstance().read(ErrorResource.class).forEach(components::addSchemas);
       errorResponses().forEach(components::addResponses);

       openApi.addSecurityItem(new SecurityRequirement()
                       .addList(securitySchemeName))
               .components(components);

        return openApi;
    }

    /**
     * Reusable error responses, referenced from controllers with
     * {@code @ApiResponse(responseCode = "404", ref = OpenApiConfiguration.RESPONSES_REF + "NotFound")}.
     * Every example matches what the API returns today.
     *
     * @return response components keyed by name
     */
    static Map<String, ApiResponse> errorResponses() {
        var responses = new LinkedHashMap<String, ApiResponse>();
        responses.put("BadRequest", errorResponse(
                "Invalid payload or parameters",
                "VALIDATION_ERROR", "Validation failed: request-body",
                "Field username: must be a well-formed email address"));
        responses.put("Unauthorized", new ApiResponse()
                .description("Missing, expired or invalid bearer token. Rejected by the security filter chain "
                        + "before reaching the controller. The response has no body."));
        responses.put("Forbidden", errorResponse(
                "The authenticated user does not have the required role",
                "FORBIDDEN", "Access denied to endpoint", "Insufficient role"));
        responses.put("NotFound", errorResponse(
                "The requested resource does not exist",
                "USER_NOT_FOUND", "USER not found: jane.doe@spottrack.com", null));
        responses.put("NotFoundEmpty", new ApiResponse()
                .description("The requested resource does not exist. The response has no body."));
        responses.put("Conflict", errorResponse(
                "The operation conflicts with the current state",
                "USER_CONFLICT", "Conflict with USER", "Username already exists: jane.doe@spottrack.com"));
        responses.put("UnprocessableEntity", errorResponse(
                "The request violates a business rule",
                "BUSINESS_RULE_VIOLATION", "Business rule violation: account.deactivated",
                "This account has been deactivated"));
        responses.put("MaintenanceForbidden", new ApiResponse()
                .description("The caller lacks the required role, or the ticket, equipment or technician "
                        + "belongs to another administrator's gym (or, for technicians, is not assigned to them).")
                .content(new Content().addMediaType(JSON, new MediaType()
                        .schema(new Schema<>().$ref(ERROR_RESOURCE_REF))
                        .addExamples("role", example("Missing role",
                                "FORBIDDEN", "Access denied to endpoint", "Insufficient role"))
                        .addExamples("ownership", example("Resource owned by another tenant",
                                "FORBIDDEN", "Access denied to Maintenance",
                                "ticketId:5f1c2b9e-8a7d-4c3b-9e21-0d6f4a7b8c90")))));
        responses.put("MaintenanceNotFound", new ApiResponse()
                .description("The referenced ticket, maintenance request, job or equipment does not exist "
                        + "(empty body), or the caller's own account could not be resolved (ErrorResource).")
                .content(new Content().addMediaType(JSON, new MediaType()
                        .schema(new Schema<>().$ref(ERROR_RESOURCE_REF))
                        .addExamples("resourceMissing", example("Resource rejected by the command service",
                                "TECHNICALTICKET_NOT_FOUND",
                                "TechnicalTicket not found: 5f1c2b9e-8a7d-4c3b-9e21-0d6f4a7b8c90", null))
                        .addExamples("callerMissing", example("Caller account not found",
                                "ADMIN_NOT_FOUND", "Admin not found: admin@spottrack.com", null)))));
        responses.put("MaintenanceFailure", new ApiResponse()
                .description("Invalid payload, or the operation failed in the application layer. These "
                        + "Maintenance endpoints return every application failure (validation, not found, "
                        + "conflict, unexpected) as 400; read `code` to tell them apart. "
                        + "`details` may be null.")
                .content(new Content().addMediaType(JSON, new MediaType()
                        .schema(new Schema<>().$ref(ERROR_RESOURCE_REF))
                        .addExamples("validation", example("Invalid field",
                                "VALIDATION_ERROR", "Validation failed: TechnicalTicket",
                                "priority must not be null"))
                        .addExamples("notFound", example("Referenced resource missing",
                                "MAINTENANCE_NOT_FOUND",
                                "Maintenance not found: 3a9d6e10-2b4c-4f8e-a1d7-6c5b9e0f1a23", null))
                        .addExamples("conflict", example("Ticket already exists",
                                "TECHNICALTICKET_CONFLICT", "Conflict with TechnicalTicket",
                                "a ticket already exists for maintenance 3a9d6e10-2b4c-4f8e-a1d7-6c5b9e0f1a23")))));
        responses.put("InternalServerError", errorResponse(
                "Unexpected server error",
                "UNEXPECTED_ERROR", "Unexpected error in global-exception-handler", "An unexpected error occurred"));
        return responses;
    }

    private static ApiResponse errorResponse(String description, String code, String message, String details) {
        return new ApiResponse()
                .description(description)
                .content(new Content().addMediaType(JSON, new MediaType()
                        .schema(new Schema<>().$ref(ERROR_RESOURCE_REF))
                        .addExamples("default", example(description, code, message, details))));
    }

    private static Example example(String summary, String code, String message, String details) {
        return new Example().summary(summary).value(details == null
                ? orderedMap("code", code, "message", message)
                : orderedMap("code", code, "message", message, "details", details));
    }

    private static Map<String, Object> orderedMap(Object... keyValues) {
        var map = new LinkedHashMap<String, Object>();
        for (int i = 0; i < keyValues.length; i += 2) {
            map.put((String) keyValues[i], keyValues[i + 1]);
        }
        return map;
    }
}
